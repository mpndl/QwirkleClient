package za.nmu.wrpv.qwirkle;

import android.widget.ImageButton;

import androidx.annotation.NonNull;

import java.util.Locale;

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

    @NonNull
    @Override
    public String toString() {
        if (color != null)
            return color.toString().toLowerCase() + "_" + shape.toString().toLowerCase();
        else return "blank";
    }
}
