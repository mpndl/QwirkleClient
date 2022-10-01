package za.nmu.wrpv.qwirkle;

import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    public ArrayList<Tile> tiles = new ArrayList<>();
    public Name name;
    public int color;
    public int points = 0;
    public enum Name {
        PLAYER1, PLAYER2, PLAYER3, PLAYER4;
    }
}
