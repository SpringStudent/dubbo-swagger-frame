package io.github.springstudent.bridge;

/**
 * @author zhouning
 * @date 2024/03/07 9:54
 */
public class DrawPrinter implements DrawApi{
    @Override
    public void drawShape(String shapeType) {
        System.out.println("打印机上绘制: " + shapeType);
    }
}
