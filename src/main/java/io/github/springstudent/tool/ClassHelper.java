package io.github.springstudent.tool;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;


/**
 * @author 周宁
 */
public class ClassHelper {
    private static String classPath = "";
    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    static {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        try {
            java.net.URL url = loader.getResource("");
            File f = new File(url.toURI());
            classPath = f.getAbsolutePath();
            classPath = URLDecoder.decode(classPath, "utf-8");
            if (classPath.contains(".jar!")) {
                classPath = System.getProperty("user.dir");
                addCurrentWorkingDir2Classpath(classPath);
            }

        } catch (Exception e) {
            classPath = System.getProperty("user.dir");
            addCurrentWorkingDir2Classpath(classPath);
        }
    }

    private static void addCurrentWorkingDir2Classpath(String path2Added) {
        URLClassLoader urlClassLoader;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{new File(path2Added).toURI().toURL()},
                    loader);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClassPath() {
        return classPath;
    }

    public static ClassLoader getLoader() {
        return loader;
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    public static String getName(Class<?> c) {
        if (!c.isArray()) {
            return c.getName();
        } else {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            } while (c.isArray());
            return c.getName() + sb.toString();
        }
    }

    public static ClassLoader getCallerClassLoader(Class<?> caller) {
        return caller.getClassLoader();
    }

    public static Set<String> importPackages(List<MethodInfo> methodInfos) {
        List<String> result = new ArrayList<>();
        for (MethodInfo methodInfo : methodInfos) {
            final List attributes = methodInfo.getAttributes();
            if(attributes!=null&&attributes.size()>0){
                String temp = null;
                for (Object attribute : attributes) {
                    if (attribute instanceof SignatureAttribute) {
                        SignatureAttribute signatureAttribute = (SignatureAttribute) attribute;
                        final byte info = signatureAttribute.get()[1];
                        temp = signatureAttribute.getConstPool().getUtf8Info(Byte.toUnsignedInt(info));
                        break;
                    } else if (attribute instanceof AnnotationsAttribute) {
                        AnnotationsAttribute annotationAttributes = (AnnotationsAttribute) attribute;
                        final Annotation[] annotations = annotationAttributes.getAnnotations();
                        for (Annotation annotation : annotations) {
                            result.add(annotation.getTypeName());
                            for (Object memberName : annotation.getMemberNames()) {
                                final MemberValue memberValue = annotation.getMemberValue((String) memberName);
                                if (memberValue instanceof EnumMemberValue) {
                                    EnumMemberValue value = (EnumMemberValue) memberValue;
                                    result.add(value.getType());
                                }
                            }
                        }
                    } else {
                        temp = methodInfo.getDescriptor();
                    }
                }
                result.add(temp);
            }else{
                result.add(methodInfo.getDescriptor());
            }
        }

        Set<String> importPackages = new HashSet<>();
        for (String ms : result) {
            if (!StringUtils.isEmpty(ms)) {
                ms = ms.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(">", ";").replaceAll("<", ";");
                String[] typeArr = ms.split(";");
                for (String type : typeArr) {
                    if (!StringUtils.isEmpty(type) && !type.equals("V")) {
                        importPackages.add(type.substring(type.indexOf("L") + 1).replaceAll("/", "."));
                    }
                }
            }
        }
        return importPackages;
    }


    public List<MethodInfo> methodInfos(Class<?> clss) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(ClassHelper.getCallerClassLoader(getClass())));
        CtClass cc = pool.get(clss.getName());
        ClassFile classFile = cc.getClassFile();
        List<MethodInfo> methodInfos = new ArrayList();
        for (Object method : classFile.getMethods()) {
            if (method instanceof MethodInfo) {
                MethodInfo methodInfo = (MethodInfo) method;
                methodInfos.add(methodInfo);
            }
        }
        return methodInfos;
    }
}