package za.nmu.wrpv.qwirkle;

public class Tile {
    public int xPos;
    public int yPos;
    public Shape shape;
    public Color color;

    public enum Shape{
        CLOVER, FPSTAR, EPSTAR, SQUARE, CIRCLE, DIAMOND;
    }

    public enum Color {
        RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE;
    }
}
