package io.github.springstudent.tool;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.generics.repository.AbstractRepository;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.tree.ClassSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

public class ReflectUtil {

    public static Method getDeclaredMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Field getDeclaredField(Class clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    public static Class<? extends Annotation> getAnnotation(Object o) {
        if (null == o) {
            return null;
        }
        Class proxyClass = o.getClass();
        if (proxyClass.getName().startsWith("com.sun.proxy.")) {
            Class<?>[] interfaces = proxyClass.getInterfaces();
            if (ArrayUtils.isEmpty(interfaces)) {
                return null;
            }
            for (Class interf : interfaces) {
                if (interf.isAnnotation()) {
                    return interf;
                }
            }
        }
        return null;
    }

    public static boolean isAnnotationObject(Object o) {
        if (null == getAnnotation(o)) {
            return false;
        }
        return true;
    }

    public static ClassRepository getClassRepository(Class clazz) {
        try {
            Method getGenericInfoMethod = Class.class.getDeclaredMethod("getGenericInfo");
            getGenericInfoMethod.setAccessible(true);
            return (ClassRepository) getGenericInfoMethod.invoke(clazz);
        } catch (Exception e) {
            throw new RuntimeException("Get ClassRepository failed for calss : " + clazz.getName(), e);
        }
    }

    public static TypeVariable<?>[] getTypeVariables(Class clazz) {

        ClassRepository classRepository = getClassRepository(clazz);
        if (null == classRepository) {
            return null;
        }
        return classRepository.getTypeParameters();
    }

    public static String[] getGenericTypeNames(Class clazz) {
        TypeVariable<?>[] typeVariables = getTypeVariables(clazz);

        if (null == typeVariables) {
            return null;
        }
        //获取定义的泛型占位符
        String[] genericTypeNames = new String[typeVariables.length];
        for (int i = 0; i < typeVariables.length; i++) {
            genericTypeNames[i] = typeVariables[i].getName();
        }

        return genericTypeNames;
    }

    public static ClassSignature getClassSignatureTree(ClassRepository classRepository) {
        try {
            if (null == classRepository) {
                return null;
            }
            Method getTreeMethod = AbstractRepository.class.getDeclaredMethod("getTree");
            getTreeMethod.setAccessible(true);
            Object o = getTreeMethod.invoke(classRepository);
            ClassSignature classSignature = (ClassSignature) o;
            return classSignature;
        } catch (Exception e) {
            throw new RuntimeException("Get classSignature failed for classRepository : " + classRepository.toString(),
                    e);
        }
    }

    public static Class getArrayClass(Class clazz) {
        String className = clazz.getName();
        if (StringUtils.startsWith(className, "[")) {
            className = "[" + className;
        } else {
            className = "[L" + className + ";";
        }
        try {
            return Class.forName(className, true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Get arrayClass failed for class : " + clazz.getName(), e);
        }
    }

}
