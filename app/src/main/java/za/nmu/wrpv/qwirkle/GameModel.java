package za.nmu.wrpv.qwirkle;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameModel {
    private Player cPlayer;
    private ArrayList<Tile> tiles = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private final int TCOUNT = 108;
    private final int PCOUNT;
    private final int HCOUNT = 6;
    private final String TAG = "game";
    private final int XLENGTH = 50;
    private final int YLENGTH = 50;
    private ArrayList<Tile> plays = new ArrayList<>();
    private int turns = 0;
    private Tile[][] board = new Tile[XLENGTH][YLENGTH];

    private enum Legality {
        LEGAL, ILLEGAL;
    }

    public GameModel(int pcount) {
        PCOUNT = pcount;
        initializeTiles();
        initializePlayers();
        initialDraw();
        initialPlayer();
    }

    private void initializeTiles() {
        ArrayList<Tile.Color> colors = new ArrayList<>(Arrays.asList(Tile.Color.BLUE, Tile.Color.GREEN, Tile.Color.ORANGE, Tile.Color.RED, Tile.Color.RED, Tile.Color.YELLOW));
        ArrayList<Tile.Shape> shapes = new ArrayList<>(Arrays.asList(Tile.Shape.CIRCLE, Tile.Shape.CLOVER, Tile.Shape.DIAMOND, Tile.Shape.EPSTAR, Tile.Shape.FPSTAR, Tile.Shape.SQUARE));
        int j = 0;
        int k = 0;
        for (int i = 0; i < TCOUNT; i++) {
            if(j > 5) {
                j = 0;
                k++;
            }
            if(k > 5)
                k = 0;

            Tile temp = new Tile();
            temp.color = colors.get(k);
            temp.shape = shapes.get(j);

            tiles.add(temp);
            j++;
        }
        Collections.shuffle(tiles);
    }

    private void initializePlayers() {
        ArrayList<Player.Name> names = new ArrayList<>(Arrays.asList(Player.Name.PLAYER1, Player.Name.PLAYER2, Player.Name.PLAYER3, Player.Name.PLAYER4));
        for (int i = 0; i < PCOUNT; i++) {
            Player temp = new Player();
            temp.name = names.get(i);
            players.add(temp);
        }
    }

    private void initialDraw() {
        for (Player player: players) {
            for (int i = 0; i < HCOUNT; i++) {
                Tile temp = tiles.remove(i);
                player.tiles.add(temp);
            }
        }
    }

    private void initialPlayer() {
        int cCount = -1;
        int sCount = -1;
        for (Player player: players) {
            int tempCCount = getPlayerHighestCCount(player);
            int tempSCount = getPlayerHighestSCount(player);

            if (tempCCount > cCount || tempSCount > sCount) {
                cCount = tempCCount;
                sCount = tempSCount;
                cPlayer = player;
            }
        }
    }

    private int getPlayerHighestCCount(Player player) {
        int red = 0;
        int orange = 0;
        int yellow = 0;
        int green = 0;
        int blue = 0;
        int purple = 0;
        for (Tile tile: player.tiles) {
            switch (tile.color) {
                case RED:
                    red++;
                    break;
                case ORANGE:
                    orange++;
                    break;
                case YELLOW:
                    yellow++;
                    break;
                case GREEN:
                    green++;
                    break;
                case BLUE:
                    blue++;
                    break;
                case PURPLE:
                    purple++;
            }
        }

        return Collections.max(Arrays.asList(red, orange, yellow, green, blue, purple));
    }

    private int getPlayerHighestSCount(Player player) {
        int clover = 0;
        int fpstar = 0;
        int epstar = 0;
        int square = 0;
        int circle = 0;
        int diamond = 0;
        for (Tile tile: player.tiles) {
            switch (tile.shape) {
                case CLOVER:
                    clover++;
                    break;
                case FPSTAR:
                    fpstar++;
                    break;
                case EPSTAR:
                    epstar++;
                    break;
                case SQUARE:
                    square++;
                    break;
                case CIRCLE:
                    circle++;
                    break;
                case DIAMOND:
                    diamond++;
            }
        }

        return Collections.max(Arrays.asList(clover, fpstar, epstar, square, circle, diamond));
    }

    public void draw(Tile... ts) {
        int hcount = HCOUNT;
        int tslength = ts.length;
        if(tiles.size() > 0) {
            if(ts.length == 0) {
                if(tiles.size() < 6) hcount = tiles.size();
                for (int i = 0; i < hcount; i++) {
                    if(cPlayer.tiles.size() < 6)
                        cPlayer.tiles.add(tiles.remove(i));
                }
            }
            else {
                for (int i = 0; i < tslength; i++) {
                    boolean removed =  cPlayer.tiles.remove(ts[i]);
                    if(removed) tiles.add(ts[i]);
                }
                Collections.shuffle(tiles);
                for (int i = 0; i < tslength; i++) {
                    cPlayer.tiles.add(tiles.remove(i));
                }
            }
        }
    }

    public Legality play(int xpos, int ypos, Tile tile) {
        if (board[xpos][ypos] != null) return Legality.ILLEGAL;
        if (turns != 0)
            if(board[xpos - 1][ypos] == null && board[xpos + 1][ypos] == null && board[xpos][ypos - 1] == null && board[xpos][ypos + 1] == null)
                return Legality.ILLEGAL;
            else {
                // Shape
                if (board[xpos - 1][ypos].shape != board[xpos][ypos].shape)
                    return Legality.ILLEGAL;
                if (board[xpos + 1][ypos].shape != board[xpos][ypos].shape)
                    return Legality.ILLEGAL;
                if (board[xpos][ypos - 1].shape != board[xpos][ypos].shape)
                    return Legality.ILLEGAL;
                if (board[xpos][ypos + 1].shape != board[xpos][ypos].shape)
                    return Legality.ILLEGAL;
                // Color
                if (board[xpos - 1][ypos].color != board[xpos][ypos].color)
                    return Legality.ILLEGAL;
                if (board[xpos + 1][ypos].color != board[xpos][ypos].color)
                    return Legality.ILLEGAL;
                if (board[xpos][ypos - 1].color != board[xpos][ypos].color)
                    return Legality.ILLEGAL;
                if (board[xpos][ypos + 1].color != board[xpos][ypos].color)
                    return Legality.ILLEGAL;
            }
        if(tile != null) {
            tile.xPos = xpos;
            tile.yPos = ypos;
            board[xpos][ypos] = tile;
            plays.add(tile);
            turns++;
        }
        return Legality.LEGAL;
    }

    private void assignPoints() {
        for (Tile tile: plays) {

        }

        // reinitialize
        plays = new ArrayList<>();
    }

    private void rAssignPoints(int xpos, int ypos) {
        if (play(xpos, ypos, null) == Legality.LEGAL) {
            
        }
    }

    public int getCPlayerTileCount() {
        return cPlayer.tiles.size();
    }

    public int getYLENGTH() {
        return YLENGTH;
    }

    public int getXLENGTH() {
        return XLENGTH;
    }
}
