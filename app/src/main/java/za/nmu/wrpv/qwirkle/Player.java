package za.nmu.wrpv.qwirkle;

import java.util.ArrayList;

public class Player {
    public ArrayList<Tile> tiles = new ArrayList<>();
    public Name name;
    public int points = 0;
    public enum Name {
        PLAYER1, PLAYER2, PLAYER3, PLAYER4;
    }
}
