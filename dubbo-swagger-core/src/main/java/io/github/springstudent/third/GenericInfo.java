package io.github.springstudent.third;
import java.util.List;

public class GenericInfo {
    private Class clazz;
    /**
     *
     * four types:1: string,generic name; 2,Class: concrete class (including array class); 3,GenericInfo: GenericInfo object; 4,GenericArrayInfo: GenericArrayInfo object
     */
    private List features;

    public GenericInfo() {
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public List getFeatures() {
        return features;
    }

    public void setFeatures(List features) {
        this.features = features;
    }
}


