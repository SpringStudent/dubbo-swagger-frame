package io.github.springstudent.bean;

import java.util.Map;

public class MyBoy {

    private Cheng cheng;

    private Zhou zhou;

    private Pair<Complex, Map<String,Object>> pair;

    public Cheng getCheng() {
        return cheng;
    }

    public void setCheng(Cheng cheng) {
        this.cheng = cheng;
    }

    public Zhou getZhou() {
        return zhou;
    }

    public void setZhou(Zhou zhou) {
        this.zhou = zhou;
    }

    public Pair<Complex, Map<String, Object>> getPair() {
        return pair;
    }

    public void setPair(Pair<Complex, Map<String, Object>> pair) {
        this.pair = pair;
    }
}
