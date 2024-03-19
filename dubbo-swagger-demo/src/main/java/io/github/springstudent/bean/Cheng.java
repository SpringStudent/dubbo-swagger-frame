package io.github.springstudent.bean;

/**
 * @author 周宁
 */
public class Cheng {

    private Integer size;

    private Boolean isMan;

    private Double money;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getMan() {
        return isMan;
    }

    public void setMan(Boolean man) {
        isMan = man;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Cheng{" +
                "size=" + size +
                ", isMan=" + isMan +
                ", money=" + money +
                '}';
    }
}
