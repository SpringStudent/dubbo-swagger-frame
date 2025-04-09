package io.github.springstudent.third;

import io.github.springstudent.tool.JavassistUtil;
import io.github.springstudent.tool.ReflectUtil;
import io.github.springstudent.tool.ClassUtil;
import javassist.Modifier;
import javassist.*;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.tree.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GenericReplaceBuilder {
    private static final ClassPool pool = ClassPool.getDefault();
    private static ConcurrentHashMap<String, Class> classMap = new ConcurrentHashMap<String, Class>();
    private static String classPackage;

    public static void initGenericReplaceBuilder(String classPackage) {
        GenericReplaceBuilder.classPackage = classPackage;
    }

    public static String getClassPackage() {
        return classPackage;
    }

    public static Class buildReplaceClass(Type type, Map<String, Class> genericClassMap) {
        if (null == type) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            GenericInfo genericInfo = getGenericInfo((ParameterizedType) type);
            return buildGenericClass(genericInfo, genericClassMap);
        } else if (type instanceof GenericArrayType) {
            GenericArrayInfo genericArrayInfo = getGenericArrayInfo((GenericArrayType) type);
            return buildGenericArrayClass(genericArrayInfo, genericClassMap);
        } else if (type instanceof Class) {
            return (Class) type;
        } else {
            return Object.class;
        }
    }

    public static Class buildGenericArrayClass(GenericArrayInfo genericArrayInfo, Map<String, Class> genericClassMap) {
        Object info = genericArrayInfo.getInfo();
        if (null == info) {
            return null;
        }
        Class clazz = null;
        if (info instanceof String) {
            clazz = null == genericClassMap ? null : genericClassMap.get((String) info);
            clazz = null == clazz ? Object.class : clazz;
        } else if (info instanceof GenericInfo) {
            clazz = buildGenericClass((GenericInfo) info, genericClassMap);
        } else if (info instanceof GenericArrayInfo) {
            clazz = buildGenericArrayClass((GenericArrayInfo) info, genericClassMap);
        } else if (info instanceof Class) {
            clazz = (Class) info;
        } else {
            clazz = Object.class;
        }
        return ReflectUtil.getArrayClass(clazz);
    }

    public static Class buildGenericClass(GenericInfo genericInfo, Map<String, Class> genericClassMap) {
        Class oldReturnClass = genericInfo.getClazz();
        if (ClassUtils.isAssignable(oldReturnClass, Map.class)) {
            return Map.class;
        }
        Map<String, Class> newGenericClassMap = getGenericClassMap(genericClassMap, genericInfo);
        if (ClassUtils.isAssignable(oldReturnClass, Collection.class)) {
            if (1 == newGenericClassMap.size()) {
                Class clazz = null;
                for (String key : newGenericClassMap.keySet()) {
                    clazz = newGenericClassMap.get(key);
                    break;
                }
                return ReflectUtil.getArrayClass(clazz);
            }
        }
        return buildClass(oldReturnClass, newGenericClassMap);
    }

    public static Class buildClass(Class genericClass, Map<String, Class> genericClassMap) {
        try {
            String featureName = getFeatureName(genericClass, genericClassMap);
            if (classMap.containsKey(featureName)) {
                return classMap.get(featureName);
            }
            String newReplaceClassName = ClassUtil.buildReplaceClassName(genericClass.getSimpleName());
            CtClass newReturnCtClass = pool.makeClass(classPackage + "." + newReplaceClassName);
            Map<String, Tuple2<Class, Method>> fieldTuples = buildNewPropertyInfos(genericClass);
            buildfieldTuples(fieldTuples, genericClassMap, genericClass);
            for (String fieldName : fieldTuples.keySet()) {
                Class fieldClass = fieldTuples.get(fieldName).getFst();
                buildFieldInfo(fieldName, fieldClass, newReturnCtClass);
            }
            Class newReplaceClass = pool.toClass(newReturnCtClass, ClassUtil.getCallerClassLoader(GenericReplaceBuilder.class), GenericReplaceBuilder.class.getProtectionDomain());
            classMap.put(featureName, newReplaceClass);
            return newReplaceClass;
        } catch (Exception e) {
            throw new RuntimeException("Build class failed for " + genericClass.getName(), e);
        }
    }

    private static String getFeatureName(Class genericClass, Map<String, Class> genericClassMap) {
        String featureName = genericClass.getName() + "|";
        if (CollectionUtils.isEmpty(genericClassMap)) {
            return featureName;
        }
        for (String key : genericClassMap.keySet()) {
            Class clazz = genericClassMap.get(key);
            String className = null == clazz ? null : clazz.getName();
            featureName += key + ":" + className + "|";
        }

        return featureName;
    }

    private static void buildFieldInfo(String fieldName, Class fieldClass, CtClass newReturnCtClass) {
        try {
            if (null == fieldClass) {
                fieldClass = Object.class;
            }
            pool.insertClassPath(new ClassClassPath(fieldClass));
            CtField ctField = new CtField(pool.get(fieldClass.getName()), fieldName, newReturnCtClass);
            ctField.setModifiers(Modifier.PRIVATE);
            newReturnCtClass.addField(ctField);


            JavassistUtil.addGetterForCtField(ctField);
            JavassistUtil.addSetterForCtField(ctField);
        } catch (Exception e) {
            throw new RuntimeException("Build field info failed, field is " + fieldName, e);
        }

    }

    private static Map<String, Tuple2<Class, Method>> buildNewPropertyInfos(Class oldReturnClass) {
        BeanUtilsBean beanUtils = BeanUtilsBean.getInstance();
        PropertyUtilsBean propertyUtils = beanUtils.getPropertyUtils();
        PropertyDescriptor[] propertyDescriptors = propertyUtils.getPropertyDescriptors(oldReturnClass);

        Map<String, Tuple2<Class, Method>> tupleMap = new HashMap<String, Tuple2<Class, Method>>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

            String name = propertyDescriptor.getName();
            Method readMethod = propertyDescriptor.getReadMethod();

            if (StringUtils.equals(name, "class") || null == readMethod) {
                continue;
            }

            Type type = readMethod.getGenericReturnType();

            if (type instanceof Class) {
                tupleMap.put(name, new Tuple2<Class, Method>((Class) type, readMethod));
            } else {
                tupleMap.put(name, new Tuple2<Class, Method>(null, readMethod));
            }
        }

        return tupleMap;
    }

    private static Map<String, Class> getGenericClassMap(Map<String, Class> oldGenericClassMap, GenericInfo genericInfo) {

        try {
            Class oldReturnClass = genericInfo.getClazz();

            Map<String, Class> newGenericClassMap = new HashMap<String, Class>();
            String[] genericTypeNames = ReflectUtil.getGenericTypeNames(oldReturnClass);
            if (ArrayUtils.isEmpty(genericTypeNames)) {
                return newGenericClassMap;
            }

            List features = genericInfo.getFeatures();
            int i = 0;
            for (Object feature : features) {
                if (feature instanceof String) {
                    Class clazz = null == oldGenericClassMap ? null : oldGenericClassMap.get((String) feature);
                    clazz = null == clazz ? Object.class : clazz;
                    newGenericClassMap.put(genericTypeNames[i], clazz);
                } else if (feature instanceof Class) {
                    newGenericClassMap.put(genericTypeNames[i], (Class) feature);
                } else if (feature instanceof GenericInfo) {
                    newGenericClassMap.put(genericTypeNames[i], buildGenericClass((GenericInfo) feature, oldGenericClassMap));
                } else if (feature instanceof GenericArrayInfo) {
                    newGenericClassMap.put(genericTypeNames[i], buildGenericArrayClass((GenericArrayInfo) feature, oldGenericClassMap));
                } else {
                    newGenericClassMap.put(genericTypeNames[i], Object.class);
                }

                i++;
            }
            return newGenericClassMap;

        } catch (Exception e) {
            throw new RuntimeException("Get genericClassMap failed.", e);
        }
    }

    public static GenericInfo getGenericInfo(ParameterizedType parameterizedType) {
        GenericInfo genericInfo = new GenericInfo();
        genericInfo.setClazz((Class) parameterizedType.getRawType());
        List features = new ArrayList<>();
        Type[] types = parameterizedType.getActualTypeArguments();
        for (Type type : types) {
            Object feature = getGenericInfoFeature(type);
            features.add(feature);
        }
        genericInfo.setFeatures(features);
        return genericInfo;
    }

    private static Object getGenericInfoFeature(Type type) {
        if (type instanceof TypeVariable) {
            return ((TypeVariable) type).getName();
        } else if (type instanceof ParameterizedType) {
            return getGenericInfo((ParameterizedType) type);
        } else if (type instanceof Class) {
            return type;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return getGenericArrayInfo(genericArrayType);
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] upperBounds = wildcardType.getUpperBounds();
            Type[] lowerBounds = wildcardType.getLowerBounds();

            Object result = Object.class;
            if (ArrayUtils.isEmpty(lowerBounds) && ArrayUtils.isNotEmpty(upperBounds)) {
                Type upperBound = upperBounds[0];
                result = getGenericInfoFeature(upperBound);
            } else if (ArrayUtils.isNotEmpty(lowerBounds)) {
                result = Object.class;
            }
            return result;
        } else {
            return Object.class;
        }
    }

    public static GenericArrayInfo getGenericArrayInfo(GenericArrayType genericArrayType) {
        Type type = genericArrayType.getGenericComponentType();
        GenericArrayInfo genericArrayInfo = new GenericArrayInfo();
        Object info = getGenericInfoFeature(type);
        genericArrayInfo.setInfo(info);
        return genericArrayInfo;
    }

    private static void buildfieldTuples(Map<String, Tuple2<Class, Method>> fieldTuples, Map<String, Class> genericClassMap, Class oldReturnClass) {

        if (!CollectionUtils.isEmpty(fieldTuples)) {
            for (String fieldName : fieldTuples.keySet()) {
                Tuple2<Class, Method> tuple3 = fieldTuples.get(fieldName);
                Method readMethod = ReflectUtil.getDeclaredMethod(oldReturnClass, tuple3.getSnd().getName());
                Field field = ReflectUtil.getDeclaredField(oldReturnClass, fieldName);
                if (null == readMethod && null == field) {
                    continue;
                }
                if (null == tuple3.getFst() && null != readMethod) {
                    Type genericReturnType = readMethod.getGenericReturnType();
                    if (genericReturnType instanceof ParameterizedType) {
                        ParameterizedType tempParameterizedType = (ParameterizedType) genericReturnType;
                        GenericInfo genericInfo = getGenericInfo(tempParameterizedType);
                        tuple3.setFst(buildGenericClass(genericInfo, genericClassMap));
                    } else if (genericReturnType instanceof GenericArrayType) {
                        GenericArrayInfo genericArrayInfo = getGenericArrayInfo((GenericArrayType) genericReturnType);
                        tuple3.setFst(buildGenericArrayClass(genericArrayInfo, genericClassMap));
                    } else if (genericReturnType instanceof TypeVariable) {
                        String name = ((TypeVariable) genericReturnType).getName();
                        Class clazz = null == genericClassMap ? Object.class : genericClassMap.get(name);
                        clazz = null == clazz ? Object.class : clazz;
                        tuple3.setFst(clazz);
                    } else if (genericReturnType instanceof Class) {
                        tuple3.setFst((Class) genericReturnType);
                    }
                }
            }
        }

        Class superClass = oldReturnClass.getSuperclass();
        if (null != superClass && !superClass.equals(Object.class)) {
            Map<String, Class> superGenericClassMap = getSuperGenericClassMap(oldReturnClass, genericClassMap);
            buildfieldTuples(fieldTuples, superGenericClassMap, superClass);
        }

        List<Tuple2<Class, List<SimpleClassTypeSignature>>> tuple2s = getSimpleClassTypeSignatureTuplesForInterfaces(oldReturnClass);
        if (!CollectionUtils.isEmpty(tuple2s)) {
            for (Tuple2<Class, List<SimpleClassTypeSignature>> tuple2 : tuple2s) {
                Class interfaze = tuple2.getFst();
                List<SimpleClassTypeSignature> simpleClassTypeSignatures = tuple2.getSnd();
                Map<String, Class> interfaceGenericClassMap = getInterfaceGenericClassMap(interfaze, simpleClassTypeSignatures, genericClassMap);
                buildfieldTuples(fieldTuples, interfaceGenericClassMap, interfaze);
            }
        }
    }

    private static Map<String, Class> getSuperGenericClassMap(Class subClass, Map<String, Class> subGenericClassMap) {
        List<SimpleClassTypeSignature> simpleClassTypeSignatures = getSimpleClassTypeSignaturesForSuper(subClass);
        Class superClass = subClass.getSuperclass();
        Map<String, Class> superGenericClassMap = new HashMap<>();

        if (null == superClass) {
            return superGenericClassMap;
        }

        if (!CollectionUtils.isEmpty(simpleClassTypeSignatures)) {
            for (SimpleClassTypeSignature simpleClassTypeSignature : simpleClassTypeSignatures) {
                if (simpleClassTypeSignature.getName().equals(superClass.getName())) {
                    TypeArgument[] typeArguments = simpleClassTypeSignature.getTypeArguments();
                    if (ArrayUtils.isNotEmpty(typeArguments)) {
                        Class[] clazzes = getClasses(typeArguments, subGenericClassMap);
                        String[] genericTypeNames = ReflectUtil.getGenericTypeNames(superClass);
                        superGenericClassMap = getGenericClassMap(genericTypeNames, clazzes);
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return superGenericClassMap;
    }

    private static Map<String, Class> getInterfaceGenericClassMap(Class interfaze, List<SimpleClassTypeSignature> simpleClassTypeSignatures, Map<String, Class> subGenericClassMap) {
        Map<String, Class> interfaceGenericClassMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(simpleClassTypeSignatures)) {
            for (SimpleClassTypeSignature simpleClassTypeSignature : simpleClassTypeSignatures) {
                if (simpleClassTypeSignature.getName().equals(interfaze.getName())) {
                    TypeArgument[] typeArguments = simpleClassTypeSignature.getTypeArguments();
                    if (ArrayUtils.isNotEmpty(typeArguments)) {
                        Class[] clazzes = getClasses(typeArguments, subGenericClassMap);
                        String[] genericTypeNames = ReflectUtil.getGenericTypeNames(interfaze);
                        interfaceGenericClassMap = getGenericClassMap(genericTypeNames, clazzes);
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return interfaceGenericClassMap;
    }

    private static List<SimpleClassTypeSignature> getSimpleClassTypeSignaturesForSuper(Class clazz) {
        ClassRepository classRepository = ReflectUtil.getClassRepository(clazz);
        if (null == classRepository) {
            return null;
        }
        ClassSignature classSignature = ReflectUtil.getClassSignatureTree(classRepository);
        ClassTypeSignature classTypeSignature = classSignature.getSuperclass();
        List<SimpleClassTypeSignature> simpleClassTypeSignatures = classTypeSignature.getPath();
        return simpleClassTypeSignatures;
    }

    private static List<Tuple2<Class, List<SimpleClassTypeSignature>>> getSimpleClassTypeSignatureTuplesForInterfaces(Class clazz) {
        ClassRepository classRepository = ReflectUtil.getClassRepository(clazz);
        if (null == classRepository) {
            return null;
        }
        ClassSignature classSignature = ReflectUtil.getClassSignatureTree(classRepository);
        Class[] interfazes = clazz.getInterfaces();
        if (ArrayUtils.isEmpty(interfazes)) {
            return null;
        } else {
            List<Tuple2<Class, List<SimpleClassTypeSignature>>> tuple2s = new ArrayList<>();
            ClassTypeSignature[] classTypeSignatures = classSignature.getSuperInterfaces();
            for (int i = 0; i < interfazes.length; i++) {
                tuple2s.add(new Tuple2<>(interfazes[i], classTypeSignatures[i].getPath()));
            }
            return tuple2s;
        }
    }

    private static Class[] getClasses(TypeArgument[] typeArguments, Map<String, Class> genericClassMap) {
        Class[] clazzes = new Class[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {

            if (typeArguments[i] instanceof TypeVariableSignature) {
                TypeVariableSignature typeVariableSignature = (TypeVariableSignature) typeArguments[i];
                String identifier = typeVariableSignature.getIdentifier();
                clazzes[i] = genericClassMap.get(identifier);
            } else if ((typeArguments[i] instanceof ClassTypeSignature)) {
                Object info = getInfo((ClassTypeSignature) typeArguments[i]);

                if (info instanceof Class) {
                    clazzes[i] = (Class) info;
                } else if (info instanceof GenericInfo) {
                    clazzes[i] = buildGenericClass((GenericInfo) info, genericClassMap);
                }
            } else if ((typeArguments[i] instanceof ArrayTypeSignature)) {
                //数组类型
                ArrayTypeSignature arrayTypeSignature = (ArrayTypeSignature) typeArguments[i];
                GenericArrayInfo genericArrayInfo = getGenericArrayInfo(arrayTypeSignature);

                clazzes[i] = buildGenericArrayClass(genericArrayInfo, genericClassMap);
            } else if ((typeArguments[i] instanceof SimpleClassTypeSignature)) {
                clazzes[i] = Object.class;
            } else {
                clazzes[i] = Object.class;
            }
        }
        return clazzes;
    }

    public static GenericInfo getGenericInfo(SimpleClassTypeSignature simpleClassTypeSignature) {
        try {
            String className = simpleClassTypeSignature.getName();
            Class clazz = Class.forName(className);
            TypeArgument[] typeArgs = simpleClassTypeSignature.getTypeArguments();
            GenericInfo genericInfo = new GenericInfo();
            genericInfo.setClazz(clazz);
            List features = new ArrayList<>();

            for (TypeArgument typeArgument : typeArgs) {

                if (typeArgument instanceof ArrayTypeSignature) {
                    GenericArrayInfo genericArrayInfo = getGenericArrayInfo((ArrayTypeSignature) typeArgument);
                    features.add(genericArrayInfo);
                } else if (typeArgument instanceof ClassTypeSignature) {
                    Object info = getInfo((ClassTypeSignature) typeArgument);
                    features.add(info);
                } else if (typeArgument instanceof TypeVariableSignature) {
                    TypeVariableSignature typeVariableSignature = (TypeVariableSignature) typeArgument;
                    String identifier = typeVariableSignature.getIdentifier();
                    features.add(identifier);
                } else {
                    features.add(Object.class);
                }
            }

            genericInfo.setFeatures(features);
            return genericInfo;
        } catch (Exception e) {
            throw new RuntimeException("Get genericInfo failed.", e);
        }

    }

    public static GenericArrayInfo getGenericArrayInfo(ArrayTypeSignature arrayTypeSignature) {
        TypeSignature typeSignature = arrayTypeSignature.getComponentType();
        GenericArrayInfo genericArrayInfo = new GenericArrayInfo();
        if (typeSignature instanceof ArrayTypeSignature) {
            genericArrayInfo.setInfo(getGenericArrayInfo((ArrayTypeSignature) typeSignature));
        } else if (typeSignature instanceof TypeVariableSignature) {
            genericArrayInfo.setInfo(((TypeVariableSignature) typeSignature).getIdentifier());
        } else if (typeSignature instanceof ClassTypeSignature) {
            Object info = getInfo((ClassTypeSignature) typeSignature);
            genericArrayInfo.setInfo(info);
        } else {
            genericArrayInfo.setInfo(Object.class);
        }
        return genericArrayInfo;
    }

    private static Object getInfo(ClassTypeSignature classTypeSignature) {
        try {
            List<SimpleClassTypeSignature> tempSimpleClassTypeSignatures = classTypeSignature.getPath();
            SimpleClassTypeSignature tempSimpleClassTypeSignature = tempSimpleClassTypeSignatures.get(0);
            String className = tempSimpleClassTypeSignature.getName();
            Class clazz = Class.forName(className);
            TypeArgument[] typeArgs = tempSimpleClassTypeSignature.getTypeArguments();
            if (ArrayUtils.isEmpty(typeArgs)) {
                return clazz;
            } else {
                return getGenericInfo(tempSimpleClassTypeSignature);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Class> getGenericClassMap(String[] genericTypeNames, Class[] clazzes) {
        Map<String, Class> genericClassMap = new HashMap<String, Class>();
        if (ArrayUtils.isEmpty(genericTypeNames)) {
            return genericClassMap;
        }
        for (int j = 0; j < genericTypeNames.length; j++) {
            genericClassMap.put(genericTypeNames[j], clazzes[j]);
        }
        return genericClassMap;
    }
}