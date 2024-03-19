package io.github.springstudent.third.util;

import javassist.*;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JavassistUtil {

    private static ClassPool pool = ClassPool.getDefault();


    public static void addGetterForCtField(CtField ctField) throws NotFoundException, CannotCompileException {
        CtClass declaringCtClass = ctField.getDeclaringClass();

        CtMethod getter = new CtMethod(pool.get(ctField.getType().getName()),
                "get" + StringUtil.toCapitalizeCamelCase(ctField.getName()), null, declaringCtClass);
        getter.setModifiers(Modifier.PUBLIC); //访问权限
        getter.setBody("{ return this." + ctField.getName() + "; }");
        declaringCtClass.addMethod(getter);
    }

    public static void addSetterForCtField(CtField ctField) throws NotFoundException, CannotCompileException {
        CtClass declaringCtClass = ctField.getDeclaringClass();
        CtMethod setter = new CtMethod(CtClass.voidType, "set" + StringUtil.toCapitalizeCamelCase(ctField.getName()),
                new CtClass[]{pool.get(ctField.getType().getName())}, declaringCtClass);
        setter.setModifiers(Modifier.PUBLIC);
        setter.setBody("{this." + ctField.getName() + "=$1;}");
        declaringCtClass.addMethod(setter);
    }

    public static Annotation buildJavassitAnnotation(java.lang.annotation.Annotation annotationObject, ConstPool constpool) {
        Annotation newAnnotation = new Annotation(ReflectUtil.getAnnotation(annotationObject).getName(), constpool);
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotationObject);
        for (String annotationMethodName : annotationAttributes.keySet()) {

            Object value = annotationAttributes.get(annotationMethodName);
            if (null == value) {
                continue;
            }

            if (Objects.equals(AnnotationUtils.getDefaultValue(ReflectUtil.getAnnotation(annotationObject), annotationMethodName), value)) {
                continue;
            }

            setJavassitAnnotation(newAnnotation, annotationMethodName, value, constpool);
        }

        return newAnnotation;
    }

    public static void setJavassitAnnotation(Annotation newAnnotation, String annotationMethodName, Object value, ConstPool constpool) {

        if (ClassUtils.isAssignable(value.getClass(), String.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new StringMemberValue((String) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Boolean.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new BooleanMemberValue((Boolean) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Integer.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new IntegerMemberValue(constpool, (Integer) value));
        } else if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new LongMemberValue((Long) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new DoubleMemberValue((Double) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Float.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new FloatMemberValue((Float) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Enum.class)) {
            EnumMemberValue methodEnumberValue = new EnumMemberValue(constpool);
            methodEnumberValue.setType(value.getClass().getName());
            methodEnumberValue.setValue(value.toString());
            newAnnotation.addMemberValue(annotationMethodName, methodEnumberValue);
        } else if (ClassUtils.isAssignable(value.getClass(), Short.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new ShortMemberValue((Short) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Byte.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new ByteMemberValue((Byte) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Character.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new CharMemberValue((Character) value, constpool));
        } else if (ClassUtils.isAssignable(value.getClass(), Class.class)) {
            newAnnotation.addMemberValue(annotationMethodName, new ClassMemberValue(((Class) value).getName(), constpool));
        } else if (ArrayUtil.isArray(value)) {
            MemberValue[] memberValues = buildMemberValues(constpool, ArrayUtil.asList(value));
            if (null == memberValues) {
                return;
            }
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constpool);
            arrayMemberValue.setValue(memberValues);
            newAnnotation.addMemberValue(annotationMethodName, arrayMemberValue);
        } else if (ReflectUtil.isAnnotationObject(value)) {
            Annotation tempAnnotation = buildJavassitAnnotation((java.lang.annotation.Annotation) value, constpool);
            newAnnotation.addMemberValue(annotationMethodName, new AnnotationMemberValue(tempAnnotation, constpool));
        }

    }

    /**
     * @param constpool
     * @param values    all types of the values should be same
     * @return
     */
    private static MemberValue[] buildMemberValues(ConstPool constpool, List values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }

        MemberValue[] memberValues = new MemberValue[values.size()];

        int i = 0;
        for (Object value : values) {
            if (ClassUtils.isAssignable(value.getClass(), String.class)) {
                memberValues[i] = new StringMemberValue((String) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Boolean.class)) {
                memberValues[i] = new BooleanMemberValue((Boolean) value, constpool);
                } else if (ClassUtils.isAssignable(value.getClass(), Integer.class)) {
                    memberValues[i] = new IntegerMemberValue(constpool, (Integer) value);
                } else if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
                memberValues[i] = new LongMemberValue((Long) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
                memberValues[i] = new DoubleMemberValue((Double) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Float.class)) {
                memberValues[i] = new FloatMemberValue((Float) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Enum.class)) {
                EnumMemberValue methodEnumberValue = new EnumMemberValue(constpool);
                methodEnumberValue.setType(value.getClass().getName());
                methodEnumberValue.setValue(value.toString());
                memberValues[i] = methodEnumberValue;
            } else if (ClassUtils.isAssignable(value.getClass(), Short.class)) {
                memberValues[i] = new ShortMemberValue((Short) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Byte.class)) {
                memberValues[i] = new ByteMemberValue((Byte) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Character.class)) {
                memberValues[i] = new CharMemberValue((Character) value, constpool);
            } else if (ClassUtils.isAssignable(value.getClass(), Class.class)) {
                memberValues[i] = new ClassMemberValue(((Class) value).getName(), constpool);
            } else if (ReflectUtil.isAnnotationObject(value)) {
                Annotation tempAnnotation = buildJavassitAnnotation((java.lang.annotation.Annotation) value, constpool);
                memberValues[i] = new AnnotationMemberValue(tempAnnotation, constpool);
            }

            i++;
        }

        return memberValues;
    }


}
