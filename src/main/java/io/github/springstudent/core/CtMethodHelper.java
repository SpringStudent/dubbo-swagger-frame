package io.github.springstudent.core;

import com.alibaba.fastjson.JSON;
import io.github.springstudent.third.GenericReplaceBuilder;
import io.github.springstudent.third.util.JavassistUtil;
import io.github.springstudent.third.util.StringUtil;
import io.github.springstudent.tool.ClassHelper;
import io.github.springstudent.tool.Constants;
import io.github.springstudent.tool.OsUtil;
import javassist.*;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 周宁
 */
public class CtMethodHelper {
    /**
     * 方法
     */
    private Method method;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 返回值类型
     */
    private Class<?> rt;
    /**
     * 方法参数类型
     */
    private Class<?>[] pts;
    /**
     * 方法参数泛型
     */
    private Type[] tps;
    /**
     * 抛出异常类型
     */
    private Class<?>[] ets;
    /**
     * 类
     */
    private Class<?> clss;
    /**
     * $dubboController的constPool
     */
    private ConstPool constPool;

    private String joinInvokerArgs;

    CtMethodHelper(Method method, Class<?> clss, ConstPool constPool) {
        this.method = method;
        this.rt = method.getReturnType();
        this.pts = method.getParameterTypes();
        this.ets = method.getExceptionTypes();
        this.methodName = method.getName();
        this.clss = clss;
        this.constPool = constPool;
        this.tps = method.getGenericParameterTypes();
    }


    public String requestMappingPath(String requestPathPrefix) {
        StringBuilder mappingPath = new StringBuilder("/");
        if (StringUtils.isNotEmpty(requestPathPrefix)) {
            mappingPath.append(requestPathPrefix).append("/");
        }
        mappingPath.append(methodName);
        for (int i = 0; i < pts.length; i++) {
            if (ClassHelper.isPrimitive(pts[i])) {
                mappingPath.append("/{").append("arg").append(i).append("}");
            }
        }
        return mappingPath.toString();
    }

    public String methodBody(boolean mergeParam) throws NotFoundException, CannotCompileException, IOException, IllegalAccessException, InstantiationException {
        doMergeParam(mergeParam);
        StringBuilder methodBody = new StringBuilder("public ");
        methodBody.append(ClassHelper.getName(rt)).append(' ').append(methodName).append('(');
        StringBuilder methodImpl = new StringBuilder(OsUtil.lowerFirst(clss.getSimpleName()));
        methodImpl.append(".").append(methodName).append("(");
        //如果进行了参数合并且参数合并了
        if (mergeParam && tps.length != pts.length) {
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    methodBody.append(',');
                }
                methodBody.append(ClassHelper.getName(pts[i]));
                methodBody.append(" arg").append(i);
                if(AbstractRequestBodyParamsWrapper.class.isAssignableFrom(pts[i])){
                    methodImpl.append(joinInvokerArgs);
                }
            }

        } else {
            for (int i = 0; i < tps.length; i++) {
                if (i > 0) {
                    methodBody.append(',');
                    methodImpl.append(",");
                }
                Class replaceClss = (GenericReplaceBuilder.buildReplaceClass(tps[i], null));
                methodBody.append(ClassHelper.getName(replaceClss));
                methodBody.append(" arg").append(i);
                if (tps[i] != replaceClss) {
                    methodImpl.append("(").append((pts[i].getSimpleName())).append(")").append("JSON.parseObject(").append("JSON.toJSONString(arg").append(i).append(")").append(",").append(pts[i].getSimpleName() + ".class").append(")");
                } else {
                    methodImpl.append("arg").append(i);
                }
            }

        }
        methodBody.append(")");
        methodImpl.append(");");
        if (ets != null && ets.length > 0) {
            methodBody.append(" throws ");
            for (int i = 0; i < ets.length; i++) {
                if (i > 0) {
                    methodBody.append(',');
                }
                methodBody.append(ClassHelper.getName(ets[i]));
            }
        }
        methodBody.append("{");
        if (!Void.TYPE.equals(rt)) {
            methodBody.append(" return ");
        }
        methodBody.append(methodImpl);
        methodBody.append("}");
        return methodBody.toString();

    }


    private void doMergeParam(boolean mergeParam) throws NotFoundException, CannotCompileException, IOException, InstantiationException, IllegalAccessException {
        if (mergeParam) {
            Class<?>[] newPts = {};
            List<MergeParamInfo> mergeParamInfos = new ArrayList<>();
            for (int i = 0; i < pts.length; i++) {
                if (!ClassHelper.isPrimitive(pts[i])) {
                    mergeParamInfos.add(new MergeParamInfo(i, pts[i], GenericReplaceBuilder.buildReplaceClass(tps[i], null)));
                } else {
                    newPts = ArrayUtils.add(newPts, pts[i]);
                }
            }
            if (mergeParamInfos.size() > 1) {
                Class<? extends AbstractRequestBodyParamsWrapper> requestBodyParamsWrapper = buildRequestBodyParamsWrapperClass(mergeParamInfos);
                newPts = ArrayUtils.add(newPts, requestBodyParamsWrapper);
                pts = newPts;
            }

        }
    }

    private static AtomicLong requestBodyParamsWrapperClssInx = new AtomicLong(0);

    private Class<? extends AbstractRequestBodyParamsWrapper> buildRequestBodyParamsWrapperClass(List<MergeParamInfo> mergeParamInfos) throws NotFoundException, CannotCompileException, IOException, IllegalAccessException, InstantiationException {
        ClassPool pool = ClassPool.getDefault();
        String requestBodyParamsWrapperClssName = Constants.REQUEST_BODY_PARAMS_WRAPER_CLASS_PREFIX + AbstractRequestBodyParamsWrapper.class.getSimpleName() + requestBodyParamsWrapperClssInx.getAndIncrement();
        CtClass requestBodyParamsWrapperCtClass = pool.makeClass(GenericReplaceBuilder.getClassPackage() + "." + requestBodyParamsWrapperClssName
                , pool.get("io.github.springstudent.core.AbstractRequestBodyParamsWrapper"));
        CtField ctField = null;
        StringBuilder fieldAppender = null;
        String[] invokerArgs = new String[tps.length];
        int newArgLen = tps.length-mergeParamInfos.size();
        StringBuilder invokerArgAppender = null;
        for (int i = 0; i < mergeParamInfos.size(); i++) {
            MergeParamInfo mergeParamInfo = mergeParamInfos.get(i);
            if(mergeParamInfo.getReplaceClss().getPackage()!=null){
                pool.importPackage(mergeParamInfo.getReplaceClss().getPackage().getName() + "." + mergeParamInfo.getReplaceClss().getSimpleName());
            }
            if(mergeParamInfo.getOriginClss().getPackage()!=null){
                pool.importPackage(mergeParamInfo.getOriginClss().getPackage().getName() + "." + mergeParamInfo.getOriginClss().getSimpleName());
            }
            fieldAppender = new StringBuilder().append("private ").append(mergeParamInfo.getReplaceClss().getSimpleName()).append(" ")
                    .append(OsUtil.lowerFirst(mergeParamInfo.getOriginClss().getSimpleName())).append(";");
            ctField = CtField.make(fieldAppender.toString(), requestBodyParamsWrapperCtClass);
            requestBodyParamsWrapperCtClass.addField(ctField);
            JavassistUtil.addGetterForCtField(ctField);
            JavassistUtil.addSetterForCtField(ctField);
            invokerArgAppender = new StringBuilder("(").append(mergeParamInfo.getOriginClss().getSimpleName()).append(")")
                    .append("JSON.parseObject(").append("JSON.toJSONString(").append("arg").append(newArgLen)
                    .append(".get").append(mergeParamInfo.getOriginClss().getSimpleName()).append("()")
                    .append(")").append(",").append(mergeParamInfo.getOriginClss().getSimpleName() + ".class").append(")");
            invokerArgs[mergeParamInfo.getPtsInx()] = invokerArgAppender.toString();
        }
        for (int i = 0; i < invokerArgs.length; i++) {
            if (StringUtils.isEmpty(invokerArgs[i])) {
                invokerArgs[i] = "arg" + i;
            }
        }

        joinInvokerArgs = StringUtil.join(invokerArgs,',');

        FileOutputStream fos = new FileOutputStream(new File(OsUtil.pathJoin(ClassHelper.getClassPath(), OsUtil.packagePath(GenericReplaceBuilder.getClassPackage()), requestBodyParamsWrapperClssName + ".class")));
        fos.write(requestBodyParamsWrapperCtClass.toBytecode());
        fos.close();
        return requestBodyParamsWrapperCtClass.toClass(ClassHelper.getCallerClassLoader(getClass()), CtMethodHelper.class.getProtectionDomain());
    }


    public Annotation[][] methodParamAnnotation() {
        Annotation[][] annotations = new Annotation[pts.length][1];
        for (int i = 0; i < pts.length; i++) {
            Annotation paramAnnot = null;
            if (ClassHelper.isPrimitive(pts[i])) {
                paramAnnot = new Annotation("org.springframework.web.bind.annotation.PathVariable", constPool);
                paramAnnot.addMemberValue("name", new StringMemberValue("arg" + i, constPool));
            } else {
                paramAnnot = new Annotation("org.springframework.web.bind.annotation.RequestBody", constPool);
            }
            annotations[i][0] = paramAnnot;
        }
        return annotations;
    }

    static class MergeParamInfo {

        private int ptsInx;

        private Class<?> originClss;

        private Class<?> replaceClss;

        public int getPtsInx() {
            return ptsInx;
        }

        public void setPtsInx(int ptsInx) {
            this.ptsInx = ptsInx;
        }

        public Class<?> getReplaceClss() {
            return replaceClss;
        }

        public void setReplaceClss(Class<?> replaceClss) {
            this.replaceClss = replaceClss;
        }

        public Class<?> getOriginClss() {
            return originClss;
        }

        public void setOriginClss(Class<?> originClss) {
            this.originClss = originClss;
        }

        public MergeParamInfo(int ptsInx, Class<?> orginClss, Class<?> replaceClss) {
            this.ptsInx = ptsInx;
            this.originClss = orginClss;
            this.replaceClss = replaceClss;
        }
    }

}
