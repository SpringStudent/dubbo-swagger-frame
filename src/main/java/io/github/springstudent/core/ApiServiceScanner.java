package io.github.springstudent.core;

import io.github.springstudent.third.GenericReplaceBuilder;
import io.github.springstudent.tool.ClassHelper;
import io.github.springstudent.tool.Constants;
import io.github.springstudent.tool.OsUtil;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author 周宁
 */
public class ApiServiceScanner implements EnvironmentAware, BeanFactoryPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private Environment environment;

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory metadataReaderFactory =
            new CachingMetadataReaderFactory(this.resourcePatternResolver);
    /**
     * 生成clss的目录地址,必须已经存在
     * 例如:io.github.controller
     */
    private String classPackage;
    /**
     * 组成的请求地址前缀
     */
    private String requestPathPrefix;
    /**
     * 参数合并配置
     */
    private boolean mergeParam;

    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;
        GenericReplaceBuilder.initGenericReplaceBuilder(classPackage);
    }

    public void setRequestPathPrefix(String requestPathPrefix) {
        this.requestPathPrefix = requestPathPrefix;
    }

    public void setMergeParam(boolean mergeParam) {
        this.mergeParam = mergeParam;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            try {
                String[] beanDefinitionNames = registry.getBeanDefinitionNames();
                BeanDefinition beanDefinition = null;
                String beanClassName = null;
                String interfaceName = null;
                Class<?> controllerClss = null;
                for (String beanDefinitionName : beanDefinitionNames) {
                    beanDefinition = registry.getBeanDefinition(beanDefinitionName);
                    beanClassName = beanDefinition.getBeanClassName();
                    if (!StringUtils.isEmpty(beanClassName)) {
                        if (beanClassName.equals(Constants.DUBBO_SERVICE_BEAN) || beanClassName.equals(Constants.DUBBO_SERVICE_BEAN2)) {
                            interfaceName = beanDefinition.getPropertyValues().get(Constants.DUBBO_INTERFACE).toString();
                            controllerClss = createController(interfaceName);
                            BeanDefinition candidate = null;
                            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                                    resolveBasePackage(classPackage) + "/" + controllerClss.getSimpleName() + ".class";
                            try {
                                Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
                                if (resources == null || resources.length == 0) {
                                    continue;
                                }
                                MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resources[0]);
                                candidate = new ScannedGenericBeanDefinition(metadataReader);
                                ((ScannedGenericBeanDefinition) candidate).setResource(resources[0]);
                                ((ScannedGenericBeanDefinition) candidate).setSource(resources[0]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
                            candidate.setScope(scopeMetadata.getScopeName());
                            String beanName = controllerClss.getSimpleName();
                            if (candidate instanceof AbstractBeanDefinition) {
                                candidate.setAutowireCandidate(true);
                            }
                            if (candidate instanceof AnnotatedBeanDefinition) {
                                AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
                            }
                            if (checkCandidate(beanName, candidate, registry)) {
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                                registerBeanDefinition(definitionHolder, registry);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition, BeanDefinitionRegistry registry) throws IllegalStateException {
        if (!registry.containsBeanDefinition(beanName)) {
            return true;
        } else {
            BeanDefinition existingDef = registry.getBeanDefinition(beanName);
            BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
            if (originatingDef != null) {
                existingDef = originatingDef;
            }
            if (this.isCompatible(beanDefinition, existingDef)) {
                return false;
            } else {
                throw new RuntimeException("Annotation-specified bean name '" + beanName + "' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " + "non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
            }
        }
    }

    protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
        return !(existingDefinition instanceof ScannedGenericBeanDefinition) || newDefinition.getSource().equals(existingDefinition.getSource()) || newDefinition.equals(existingDefinition);
    }

    private Class<?> createController(String interfaceName) throws IOException, CannotCompileException, NotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Class<?> interfaceClass = null;
        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return writerCompiler(interfaceClass);
    }

    public Class<?> writerCompiler(Class<?> clss) throws CannotCompileException, IOException, NotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ClassPool pool = new ClassPool();
        pool.appendClassPath(new LoaderClassPath(ClassHelper.getCallerClassLoader(getClass())));
        //导包
        String packageName = clss.getPackage().getName();
        pool.importPackage("org.springframework.web.bind.annotation");
        pool.importPackage("io.swagger.annotations");
        pool.importPackage("javax.annotation");
        pool.importPackage("java.util");
        pool.importPackage("com.alibaba.fastjson.JSON");
        pool.importPackage(OsUtil.importPackage(packageName));
        List<MethodInfo> methodInfos = new ClassHelper().methodInfos(clss);
        //TODO FIXME 导入包代码优化
        Set<String> importPackages = ClassHelper.importPackages(methodInfos);
        for (String ipc : importPackages) {
            pool.importPackage(ipc);
        }
        pool.importPackage(packageName + "." + clss.getSimpleName());
        //创建CtClass
        String controllerClassName = clss.getSimpleName() + Constants.DUBBO_CONTROLLER_SUFFIX;
        CtClass cls = pool.makeClass(classPackage + "." + controllerClassName);
        ClassFile classFile = cls.getClassFile();
        ConstPool constpool = classFile.getConstPool();
        //添加类的注解
        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation rcAnnotation = new Annotation("org.springframework.web.bind.annotation.RestController", constpool);
        Annotation rmAnnotation = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
        ArrayMemberValue amv = new ArrayMemberValue(constpool);
        amv.setValue(new StringMemberValue[]{new StringMemberValue(packageName + "." + clss.getSimpleName(), constpool)});
        rmAnnotation.addMemberValue("value", amv);
        Annotation apiAnnotation = new Annotation("io.swagger.annotations.Api", constpool);
        annotationsAttribute.addAnnotation(rcAnnotation);
        annotationsAttribute.addAnnotation(rmAnnotation);
        annotationsAttribute.addAnnotation(apiAnnotation);
        cls.getClassFile().addAttribute(annotationsAttribute);
        //添加成员变量
        StringBuilder fieldAppender = new StringBuilder("private ").append(clss.getSimpleName()).append(" ").append(OsUtil.lowerFirst(clss.getSimpleName())).append(";");
        CtField ctField = CtField.make(fieldAppender.toString(), cls);
        AnnotationsAttribute fieldAnnoAttrs = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation fieldAno = new Annotation("javax.annotation.Resource", constpool);
        fieldAnnoAttrs.setAnnotation(fieldAno);
        cls.addField(ctField);
        ctField.getFieldInfo().addAttribute(fieldAnnoAttrs);
        //方法生成
        java.lang.reflect.Method[] methods = clss.getDeclaredMethods();
        Map<String, Integer> methodNameTimes = new HashMap<>();
        for (java.lang.reflect.Method method : methods) {
            String methodName = method.getName();
            if (methodNameTimes.get(methodName) == null) {
                methodNameTimes.put(methodName, 1);
            } else {
                Integer methodTime = methodNameTimes.get(methodName) + 1;
                methodNameTimes.put(methodName, methodTime);
            }
            CtMethodHelper ctMethodHelper = new CtMethodHelper(method, clss, constpool,buildMethodName(methodNameTimes, methodName) , mergeParam,importPackages);
            //方法注解
            AnnotationsAttribute mthAnnoAttrs = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation mthAno = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
            ArrayMemberValue valueArr = new ArrayMemberValue(constpool);
            valueArr.setValue(new StringMemberValue[]{new StringMemberValue(ctMethodHelper.requestMappingPath(requestPathPrefix), constpool)});
            mthAno.addMemberValue("value", valueArr);
            ArrayMemberValue methodArr = new ArrayMemberValue(constpool);
            EnumMemberValue emv = new EnumMemberValue(constpool);
            emv.setType("org.springframework.web.bind.annotation.RequestMethod");
            emv.setValue("POST");
            methodArr.setValue(new EnumMemberValue[]{emv});
            mthAno.addMemberValue("method", methodArr);
            mthAnnoAttrs.setAnnotation(mthAno);
            CtMethod mthd = CtNewMethod.make(ctMethodHelper.methodBody(), cls);
            ParameterAnnotationsAttribute parameterAtrribute = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
            parameterAtrribute.setAnnotations(ctMethodHelper.methodParamAnnotation());
            cls.addMethod(mthd);
            mthd.getMethodInfo().addAttribute(parameterAtrribute);
            mthd.getMethodInfo().addAttribute(mthAnnoAttrs);
        }
        FileOutputStream fos = new FileOutputStream(new File(OsUtil.pathJoin(ClassHelper.getClassPath(), OsUtil.packagePath(classPackage), controllerClassName + ".class")));
        fos.write(cls.toBytecode());
        fos.close();
        return cls.toClass(ClassHelper.getCallerClassLoader(getClass()), ApiServiceScanner.class.getProtectionDomain());
    }

    private String buildMethodName(Map<String, Integer> methodNameTimes, String methodName) {
        int methodTime = methodNameTimes.get(methodName);
        if (methodTime > 1) {
            return methodName + (methodTime - 1)+Constants.REPEAT_METHOD_NAME_SUFFIX;
        } else {
            return methodName;
        }
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.environment.resolveRequiredPlaceholders(basePackage));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String psp = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(classPackage) + "**/*" + Constants.DUBBO_CONTROLLER_SUFFIX + ".class";
        String psp2 = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(classPackage) + "**/" + Constants.REPLACE_GENERIC_CLASS_PREFIX + "*.class";
        String psp3 = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(classPackage) + "**/" + Constants.REQUEST_BODY_PARAMS_WRAPER_CLASS_PREFIX + "*.class";
        List<String> packageSearchPaths = Arrays.asList(psp, psp2, psp3);
        try {
            for (String packageSearchPath : packageSearchPaths) {
                Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
                if (resources != null && resources.length > 0) {
                    for (Resource resource : resources) {
                        resource.getFile().delete();
                    }
                }
            }
        } catch (IOException e) {

        }
    }

}
