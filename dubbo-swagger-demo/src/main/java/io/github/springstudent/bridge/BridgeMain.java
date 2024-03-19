package io.github.springstudent.bridge;

/**
 * @author zhouning
 * @date 2024/03/07 9:57
 */
public class BridgeMain {

    public static void main(String[] args) {
        DrawPrinter drawPrinter = new DrawPrinter();
        DrawScreen drawScreen = new DrawScreen();

        Shape circle = new Circular(drawPrinter);
        circle.draw();
        Rectangle rectangle = new Rectangle(drawScreen);
        rectangle.draw();
    }
}
