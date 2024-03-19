package io.github.springstudent.bridge;

/**
 * @author zhouning
 * @date 2024/03/07 9:57
 */
public class Rectangle extends Shape{
    public Rectangle(DrawApi drawApi) {
        super(drawApi);
    }

    @Override
    protected void draw() {
        this.drawApi.drawShape("矩形");
    }
}
