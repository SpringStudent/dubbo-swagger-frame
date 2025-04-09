package io.github.springstudent.tool;

import io.github.springstudent.core.ApiServiceScanner;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author 周宁
 */
public class ClassUtil {
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

    public static Set<Class<?>> getReferencedClasses(Class<?> clazz) {
        Set<Class<?>> result = new HashSet<>();
        // 1. 类本身的泛型父类和接口
        addType(clazz.getGenericSuperclass(), result);
        for (Type iface : clazz.getGenericInterfaces()) {
            addType(iface, result);
        }
        // 2. 类上的注解
        for (java.lang.annotation.Annotation annotation : clazz.getAnnotations()) {
            result.add(annotation.annotationType());
        }
        // 3. 字段类型和注解
        for (Field field : clazz.getDeclaredFields()) {
            addType(field.getGenericType(), result);
            for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
                result.add(annotation.annotationType());
            }
        }
        // 4. 方法返回值、参数、异常、注解
        for (Method method : clazz.getDeclaredMethods()) {
            for (TypeVariable<Method> typeParam : method.getTypeParameters()) {
                for (Type bound : typeParam.getBounds()) {
                    addType(bound, result);
                }
            }
            addType(method.getGenericReturnType(), result);
            for (Type paramType : method.getGenericParameterTypes()) {
                addType(paramType, result);
            }
            for (Type exceptionType : method.getGenericExceptionTypes()) {
                addType(exceptionType, result);
            }

            for (java.lang.annotation.Annotation annotation : method.getAnnotations()) {
                result.add(annotation.annotationType());
            }

            for (java.lang.annotation.Annotation[] paramAnnos : method.getParameterAnnotations()) {
                for (java.lang.annotation.Annotation a : paramAnnos) {
                    result.add(a.annotationType());
                }
            }
        }

        // 5. 构造函数
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            for (Type paramType : constructor.getGenericParameterTypes()) {
                addType(paramType, result);
            }
            for (Type exceptionType : constructor.getGenericExceptionTypes()) {
                addType(exceptionType, result);
            }
            for (Annotation annotation : constructor.getAnnotations()) {
                result.add(annotation.annotationType());
            }
        }

        // 6. 内部类
        for (Class<?> inner : clazz.getDeclaredClasses()) {
            result.add(inner);
        }

        return result;
    }

    private static void addType(Type type, Set<Class<?>> result) {
        if (type instanceof Class<?>) {
            Class<?> cls = (Class<?>) type;
            if (!cls.isPrimitive()) {
                result.add(cls);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            addType(pt.getRawType(), result);
            for (Type arg : pt.getActualTypeArguments()) {
                addType(arg, result);
            }
        } else if (type instanceof GenericArrayType) {
            addType(((GenericArrayType) type).getGenericComponentType(), result);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            for (Type upper : wt.getUpperBounds()) addType(upper, result);
            for (Type lower : wt.getLowerBounds()) addType(lower, result);
        }
    }

    public static Set<String> getDependencyPackages(Class<?> clazz) {
        Set<String> packages = new HashSet<>();
        Set<Class<?>> dependencies = getReferencedClasses(clazz);
        for (Class<?> dependency : dependencies) {
            if (dependency.getPackage() != null) {
                packages.add(dependency.getPackage().getName());
            }
        }
        return packages;
    }

    private static ConcurrentHashMap<String, AtomicInteger> classNameMap = new ConcurrentHashMap<String, AtomicInteger>();

    public static final String NEW_SUB_CLASS_NAME_PRE = ApiServiceScanner.REPLACE_GENERIC_CLASS_PREFIX;

    public static String buildReplaceClassName(String simpleClassName) {
        return buildNewClassName(NEW_SUB_CLASS_NAME_PRE + simpleClassName);
    }

    public static String buildNewClassName(String orignFullClassName) {
        classNameMap.putIfAbsent(orignFullClassName, new AtomicInteger(0));
        int count = classNameMap.get(orignFullClassName).incrementAndGet();

        String newClassName = orignFullClassName;

        if (StringUtils.isNumeric(StringUtils.substring(newClassName, newClassName.length() - 1))) {
            newClassName += "B";
        }

        if (count > 1) {
            newClassName = orignFullClassName + (count - 1);
        }

        return newClassName;
    }
}