package io.github.springstudent.core;

import io.github.springstudent.third.GenericReplaceBuilder;
import io.github.springstudent.third.util.StringUtil;
import io.github.springstudent.tool.ClassHelper;
import io.github.springstudent.tool.OsUtil;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
        if(StringUtils.isNotEmpty(requestPathPrefix)){
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

    public String methodBody() {
        StringBuilder methodBody = new StringBuilder("public ");
        methodBody.append(ClassHelper.getName(rt)).append(' ').append(methodName).append('(');
        StringBuilder methodImpl = new StringBuilder(OsUtil.lowerFirst(clss.getSimpleName()));
        methodImpl.append(".").append(methodName).append("(");
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
}
