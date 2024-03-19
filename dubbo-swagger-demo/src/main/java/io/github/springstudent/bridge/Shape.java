package io.github.springstudent.bridge;

/**
 * @author zhouning
 * @date 2024/03/07 9:55
 */
public abstract class Shape {

    protected DrawApi drawApi;

    public Shape(DrawApi drawApi){
        this.drawApi = drawApi;
    }

    abstract protected void draw();
}
