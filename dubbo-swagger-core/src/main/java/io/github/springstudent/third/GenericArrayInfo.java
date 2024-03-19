package io.github.springstudent.third;

public class GenericArrayInfo {
    /**
     *
     * four types:1: string,generic name; 2,Class: concrete class; 3,GenericInfo: GenericInfo object; 4,GenericArrayInfo: GenericArrayInfo object
     */
    private Object info;

    public GenericArrayInfo() {
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }
}
