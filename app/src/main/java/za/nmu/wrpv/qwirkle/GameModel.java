package za.nmu.wrpv.qwirkle;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameModel {
    public Player cPlayer;
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
    public Tile[][] board = new Tile[XLENGTH][YLENGTH];

    public enum Legality {
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

    public void draw(boolean turn , Tile... ts) {
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
        if(turn)
            turn();
    }

    private void turn() {
        int i = 0;
        for (; i < players.size(); i++) {
            if(cPlayer.name == players.get(i).name) {
                i++;
                if(i >= players.size()) i = 0;
                cPlayer = players.get(i);
                return;
            }
        }
    }

    public Legality play(int xpos, int ypos, Tile tile) {
        if (board[xpos][ypos] != null) return Legality.ILLEGAL;
        if(legal(xpos, ypos, tile) == Legality.ILLEGAL) return Legality.ILLEGAL;
        if(tile != null) {
            tile.xPos = xpos;
            tile.yPos = ypos;
            cPlayer.tiles.remove(tile);
            draw(false, tile);
            board[xpos][ypos] = tile;
            plays.add(tile);
            turns++;
            turn();
        }
        return Legality.LEGAL;
    }

    private Legality legal(int xpos, int ypos, Tile tile) {
        if (turns != 0) {
            if (nul(xpos - 1, ypos) && nul(xpos + 1, ypos) && nul(xpos, ypos - 1) && nul(xpos, ypos + 1)) {
                return Legality.ILLEGAL;
            } else {
                if (equivalent(xpos - 1, ypos, tile))
                    return Legality.LEGAL;
                if (equivalent(xpos + 1, ypos, tile))
                    return Legality.LEGAL;
                if (equivalent(xpos, ypos - 1, tile))
                    return Legality.LEGAL;
                if (equivalent(xpos, ypos + 1, tile))
                    return Legality.LEGAL;
            }
        }
        if(turns == 0)
            return Legality.LEGAL;
        return Legality.ILLEGAL;
    }

    private boolean equivalent(int xpos, int ypos, Tile tile1) {
        if(xpos >= 0 && ypos >= 0 && xpos < XLENGTH && ypos < YLENGTH) {
            Tile tile2 = board[xpos][ypos];
            if(tile1 != null && tile2 != null) {
                //Log.i(TAG, tile2.color + ", " + tile2.shape + ", " + tile1.color + ", " + tile1.shape);
                if (tile1.shape.equals(tile2.shape))
                    return true;
                else return tile1.color.equals(tile2.color);
            }
            return false;
        }
        return false;
    }

    private boolean nul(int xpos, int ypos) {
        if(xpos >= 0 && ypos >= 0) {
            Tile tile = board[xpos][ypos];
            return tile == null;
        }
        return true;
    }

    private void assignPoints() {
        for (Tile tile: plays) {
            rAssignPoints(tile.xPos, tile.yPos, tile);
        }
        // reinitialize
        plays = new ArrayList<>();
    }

    private void rAssignPoints(int xpos, int ypos, Tile tile) {
        if (legal(xpos, ypos, tile) == Legality.LEGAL) {
            rAssignPoints(xpos - 1, ypos, tile);
            cPlayer.points++;
            rAssignPoints(xpos + 1, ypos, tile);
            cPlayer.points++;
            rAssignPoints(xpos, ypos - 1, tile);
            cPlayer.points++;
            rAssignPoints(xpos, ypos + 1, tile);
            cPlayer.points++;
        }
    }
}
