package io.github.springstudent.third.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil extends StringUtils {
    private static final char SEPARATOR = '_';

    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;

        char first = s.charAt(0);
        if (SEPARATOR != first) {
            sb.append(Character.toLowerCase(first));
        } else {
            upperCase = true;
        }

        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);

            if (upperCase && c != SEPARATOR) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else if (c == SEPARATOR) {
                upperCase = true;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
