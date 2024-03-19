package io.github.springstudent.bean;

/**
 * @author 周宁
 */
public class Zhou {

    private String car;

    private String tempora;

    private Integer times;

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getTempora() {
        return tempora;
    }

    public void setTempora(String tempora) {
        this.tempora = tempora;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    @Override
    public String toString() {
        return "Zhou{" +
                "car='" + car + '\'' +
                ", tempora='" + tempora + '\'' +
                ", times=" + times +
                '}';
    }
}
