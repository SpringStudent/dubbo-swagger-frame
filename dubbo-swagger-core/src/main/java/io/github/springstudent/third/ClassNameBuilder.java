package io.github.springstudent.third;

import io.github.springstudent.tool.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 周宁
 */
public class ClassNameBuilder {

    private static ConcurrentHashMap<String, AtomicInteger> classNameMap = new ConcurrentHashMap<String, AtomicInteger>();

    public static final String NEW_SUB_CLASS_NAME_PRE = Constants.REPLACE_GENERIC_CLASS_PREFIX;

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
