package io.github.springstudent.tool;

import java.io.File;

/**
 * @author 周宁
 */
public class OsUtil {

    public static String pathJoin(final String... pathElements) {
        final String path;
        if (pathElements == null || pathElements.length == 0) {
            path = File.separator;
        } else {
            final StringBuffer sb = new StringBuffer();
            for (final String pathElement : pathElements) {
                if (pathElement.length() > 0) {
                    sb.append(pathElement);
                    sb.append(File.separator);
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            path = sb.toString();
        }
        return (path);
    }

    public static String lowerFirst(String oldStr) {
        char[] chars = oldStr.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public static String packagePath(String packageName) {
        return packageName.replaceAll("\\.", "/");
    }

    public static String importPackage(String packageName) {
        String[] packPath = packageName.split("\\.");
        if (packPath.length < 3) {
            return packageName;
        } else {
            return packPath[0] + "." + packPath[1];
        }
    }

}
