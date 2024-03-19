package io.github.springstudent.bean;

/**
 * @author 周宁
 * @Date 2019-12-04 18:06
 */
public class Generic<T> {

    private String name;

    private T t;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "Generic{" +
                "name='" + name + '\'' +
                ", t=" + t +
                '}';
    }
}
