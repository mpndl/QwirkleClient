package za.nmu.wrpv.qwirkle;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Tile implements Serializable {
    private static final long serialVersionUID = 80L;
    public int index;
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

    @NonNull
    @Override
    public String toString() {
        if (color != null)
            return color.toString().toLowerCase() + "_" + shape.toString().toLowerCase();
        else return "blank";
    }
}
