package io.github.springstudent;

import io.github.springstudent.tool.ClassUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ClassUtilTest {

    static class ArrayFieldHolder {
        private CustomType[] customTypes;
    }

    static class CustomType {
    }

    static class ClassGenericBoundHolder<T extends Serializable> {
    }

    static class MethodGenericBoundHolder {
        public <T extends List<String>> void process(T data) {
        }
    }

    @Test
    public void getDependencyPackages_shouldResolveArrayComponentPackage() {
        Set<String> packages = ClassUtil.getDependencyPackages(ArrayFieldHolder.class);
        Assert.assertTrue(packages.contains(CustomType.class.getPackage().getName()));
    }

    @Test
    public void getDependencyPackages_shouldResolveClassTypeVariableBoundPackage() {
        Set<String> packages = ClassUtil.getDependencyPackages(ClassGenericBoundHolder.class);
        Assert.assertTrue(packages.contains(Serializable.class.getPackage().getName()));
    }

    @Test
    public void getDependencyPackages_shouldResolveMethodTypeVariableBoundPackage() {
        Set<String> packages = ClassUtil.getDependencyPackages(MethodGenericBoundHolder.class);
        Assert.assertTrue(packages.contains(List.class.getPackage().getName()));
    }

    @Test
    public void getDependencyPackages_shouldHandleNullClass() {
        Set<String> packages = ClassUtil.getDependencyPackages(null);
        Assert.assertTrue(packages.isEmpty());
    }

}
