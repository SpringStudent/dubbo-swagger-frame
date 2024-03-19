package io.github.springstudent.bean;

import java.util.Arrays;
import java.util.List;

/**
 * @author 周宁
 * @Date 2019-12-09 18:49
 */
public class Complex {

    private List<Generic<Zhou>> generics;

    private Cheng[] chengs;

    private Generic<Zhou[]> zhouGeneric;

    private Generic generic;

    private List<Cheng> chengs2;

    private List<Zhou> zhous;

    public List<Generic<Zhou>> getGenerics() {
        return generics;
    }

    public void setGenerics(List<Generic<Zhou>> generics) {
        this.generics = generics;
    }

    public Cheng[] getChengs() {
        return chengs;
    }

    public void setChengs(Cheng[] chengs) {
        this.chengs = chengs;
    }

    public Generic<Zhou[]> getZhouGeneric() {
        return zhouGeneric;
    }

    public void setZhouGeneric(Generic<Zhou[]> zhouGeneric) {
        this.zhouGeneric = zhouGeneric;
    }

    public Generic getGeneric() {
        return generic;
    }

    public void setGeneric(Generic generic) {
        this.generic = generic;
    }

    public List<Cheng> getChengs2() {
        return chengs2;
    }

    public void setChengs2(List<Cheng> chengs2) {
        this.chengs2 = chengs2;
    }

    public List<Zhou> getZhous() {
        return zhous;
    }

    public void setZhous(List<Zhou> zhous) {
        this.zhous = zhous;
    }

    @Override
    public String toString() {
        return "Complex{" +
                "generics=" + generics +
                ", chengs=" + Arrays.toString(chengs) +
                ", zhouGeneric=" + zhouGeneric +
                ", generic=" + generic +
                ", chengs2=" + chengs2 +
                ", zhous=" + zhous +
                '}';
    }
}
