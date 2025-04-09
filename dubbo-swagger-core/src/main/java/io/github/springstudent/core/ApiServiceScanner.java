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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 周宁
 */
public class ApiServiceScanner implements BeanFactoryPostProcessor {

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
    /**
     * 指定那些apis产生controller
     */
    private List<String> includeApis;

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

    public void setIncludeApis(List<String> includeApis) {
        this.includeApis = includeApis;
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
                            if (beanDefinition.getPropertyValues().get(Constants.DUBBO_INTERFACE) != null) {
                                interfaceName = beanDefinition.getPropertyValues().get(Constants.DUBBO_INTERFACE).toString();
                            } else {
                                Class tmpClss = (Class) beanDefinition.getPropertyValues().get(Constants.DUBBO_INTERFACE2);
                                interfaceName = tmpClss.getCanonicalName();
                            }
                            if (!apiIncluded(interfaceName)) {
                                continue;
                            }
                            controllerClss = createController(interfaceName);
                            registerDynamicControllerClass(controllerClss, registry);
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    private void registerDynamicControllerClass(Class<?> controllerClss, BeanDefinitionRegistry registry) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(controllerClss);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        String beanName = controllerClss.getSimpleName();
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private boolean apiIncluded(String interfaceName) {
        if (includeApis != null && includeApis.size() > 0) {
            for (String includeApi : includeApis) {
                if (interfaceName.endsWith(includeApi)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
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
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(ClassHelper.getCallerClassLoader(getClass())));
        //导包
        String packageName = clss.getPackage().getName();
        pool.importPackage("org.springframework.web.bind.annotation");
//        pool.importPackage("io.swagger.annotations");
        pool.importPackage("javax.annotation");
        pool.importPackage("java.util");
        pool.importPackage("com.alibaba.fastjson.JSON");
        pool.importPackage(OsUtil.importPackage(packageName));
        Set<String> importPackages = ClassHelper.getDependencyPackages(clss);
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
//        Annotation apiAnnotation = new Annotation("io.swagger.annotations.Api", constpool);
        annotationsAttribute.addAnnotation(rcAnnotation);
        annotationsAttribute.addAnnotation(rmAnnotation);
//        annotationsAttribute.addAnnotation(apiAnnotation);
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
            CtMethodHelper ctMethodHelper = new CtMethodHelper(method, clss, constpool, buildMethodName(methodNameTimes, methodName), mergeParam, importPackages);
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
        return pool.toClass(cls, ClassHelper.getCallerClassLoader(getClass()), ApiServiceScanner.class.getProtectionDomain());
    }

    private String buildMethodName(Map<String, Integer> methodNameTimes, String methodName) {
        int methodTime = methodNameTimes.get(methodName);
        if (methodTime > 1) {
            return methodName + (methodTime - 1) + Constants.REPEAT_METHOD_NAME_SUFFIX;
        } else {
            return methodName;
        }
    }
}
