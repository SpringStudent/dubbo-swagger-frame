package io.github.springstudent.bridge;

/**
 * @author zhouning
 * @date 2024/03/07 9:56
 */
public class Circular extends Shape{
    public Circular(DrawApi drawApi) {
        super(drawApi);
    }

    @Override
    protected void draw() {
        this.drawApi.drawShape("圆形");
    }
}
