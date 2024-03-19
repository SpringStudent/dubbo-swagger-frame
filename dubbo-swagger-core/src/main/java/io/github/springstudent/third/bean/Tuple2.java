package io.github.springstudent.third.bean;

import java.io.Serializable;

public class Tuple2<A, B> implements Serializable {
    private static final long serialVersionUID = 4251682107552677117L;

    private A fst;
    private B snd;

    public Tuple2(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Tuple2)) { return false; }

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>)o;

        if (fst != null ? !fst.equals(tuple2.fst) : tuple2.fst != null) { return false; }
        return snd != null ? snd.equals(tuple2.snd) : tuple2.snd == null;

    }

    @Override
    public int hashCode() {
        int result = fst != null ? fst.hashCode() : 0;
        result = 31 * result + (snd != null ? snd.hashCode() : 0);
        return result;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public A getFst() {
        return fst;
    }

    public void setFst(A fst) {
        this.fst = fst;
    }

    public B getSnd() {
        return snd;
    }

    public void setSnd(B snd) {
        this.snd = snd;
    }
}
