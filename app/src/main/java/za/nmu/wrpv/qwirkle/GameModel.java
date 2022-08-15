package za.nmu.wrpv.qwirkle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

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
    public ArrayList<Tile> places = new ArrayList<>();
    private int turns = 0;
    private int tempTurns = -1;
    private int points = 0;
    public Tile[][] board = new Tile[XLENGTH][YLENGTH];
    public Tile[][] tempBoard = null;
    private boolean placing = false;
    public boolean backedup = false;
    private TextView cPlayerTilesView;
    private MainActivity mainActivity;

    public enum Legality {
        LEGAL, ILLEGAL;
    }

    public GameModel(int pcount, TextView cPlayerTilesView, MainActivity mainActivity) {
        this.cPlayerTilesView = cPlayerTilesView;
        this.mainActivity = mainActivity;
        PCOUNT = pcount;
        initializeTiles();
        initializePlayers();
        initialDraw();
        initialPlayer();
        showCPlayerTiles();
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

    public void showCPlayerTiles() {
        int i = 0;
        cPlayerTilesView.setText("");
        for (Tile tile: cPlayer.tiles) {
            cPlayerTilesView.setText(cPlayerTilesView.getText().toString() + "(" + tile.color + "," + tile.shape + ") ("+ i +"), ");
            i++;
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
                for (Tile t : ts) {
                    boolean removed = cPlayer.tiles.remove(t);
                    if (removed) tiles.add(t);
                }
                Collections.shuffle(tiles);
                for (int i = 0; i < tslength; i++) {
                    cPlayer.tiles.add(tiles.remove(i));
                }
            }
            if(placing)
                turns = tempTurns;
            placing = false;
        }
    }

    public void draw(ArrayList<Tile> ts) {
        int hcount = HCOUNT;
        int tslength = ts.size();
        if(tiles.size() > 0) {
            if(ts.size() == 0) {
                if(tiles.size() < 6) hcount = tiles.size();
                for (int i = 0; i < hcount; i++) {
                    if(cPlayer.tiles.size() < 6)
                        cPlayer.tiles.add(tiles.remove(i));
                }
            }
            else {
                for (Tile t : ts) {
                    boolean removed = cPlayer.tiles.remove(t);
                    if (removed) tiles.add(t);
                }
                Collections.shuffle(tiles);
                for (int i = 0; i < tslength; i++) {
                    cPlayer.tiles.add(tiles.remove(i));
                }
            }
            placing = false;
        }
    }

    public void turn() {
        int i = 0;
        for (; i < players.size(); i++) {
            if(cPlayer.name == players.get(i).name) {
                i++;
                if(i >= players.size()) i = 0;
                cPlayer = players.get(i);
                return;
            }
        }
        turns++;
        showCPlayerTiles();
    }

    public void place(int xpos, int ypos, Tile tile) {
        Log.i(TAG, "place: " + tile.color + "," + tile.shape);
        if(legal(xpos, ypos, tile) == Legality.LEGAL) {
            if(!placing)
                placing = true;
            tile.xPos = xpos;
            tile.yPos = ypos;
            places.add(tile);
            cPlayer.tiles.remove(tile);
            tempBoard[tile.xPos][tile.yPos] = tile;
        }
        else Toast.makeText(mainActivity, Legality.ILLEGAL + "", Toast.LENGTH_SHORT).show();
        turns++;
    }

    public void recover() {
        if(tempBoard != null) {
            board = copy(tempBoard);
        }
    }

    private Tile[][] copy(Tile[][] src) {
        Tile[][] temp = new Tile[src.length][src[0].length];
        for(int i=0; i<src.length; i++) {
            System.arraycopy(src[i], 0, temp[i], 0, src[i].length);
        }
        return temp;
    }

    public void backup() {
        tempBoard = copy(board);
        tempTurns = turns;
    }

    public ArrayList<Tile> play() {
        if(places.size() > 0) {
            placing = false;
            ArrayList<Tile> tiles = places;
            assignPoints();
            for (Tile tile: tiles) {
                draw(tile);
            }
            //score();
            return tiles;
        }
        return null;
    }

    private Legality legal(int xpos, int ypos, Tile tile) {
        if (tempBoard[xpos][ypos] != null) return Legality.ILLEGAL;
        if(turns == 0)
            return Legality.LEGAL;
        if (!duplicate(xpos, ypos, tile))
            return Legality.LEGAL;
        return Legality.ILLEGAL;
    }

    private boolean duplicate(int xpos, int ypos,Tile tile) {
        if(equivalent(xpos - 1, ypos, tile) || !equivalent(xpos + 1, ypos, tile) ||
                equivalent(xpos, ypos - 1, tile) || !equivalent(xpos, ypos + 1, tile)) {
            ArrayList<Tile> tempTiles = new ArrayList<>();
            for (Tile[] value : tempBoard) {
                for (int j = 0; j < tempBoard.length; j++) {
                    if (value[j] != null)
                        tempTiles.add(value[j]);
                }
            }
            if(tempTiles.size() == 1) {
                Tile tile2 = tempTiles.get(0);
                if(tile2.color.equals(tile.color))
                    return false;
                else if(tile2.shape.equals(tile.shape))
                    return false;
            }
            else if (tempTiles.size() >= 2) {
                Tile tile1 = tempTiles.get(tempTiles.size() - 2);
                Tile tile2 = tempTiles.get(tempTiles.size() - 1);
                if(tile1.shape.equals(tile2.shape)) {
                    if (tile1.yPos == tile2.yPos) {
                        if (ypos == tile1.yPos) {
                            if(tile.shape.equals(tile1.shape))
                                return false;
                        }
                        else {
                            if(xpos == tile1.xPos) {
                                if(tile.color.equals(tile1.color))
                                    return false;
                            }
                            else if(xpos == tile2.xPos) {
                                if (tile.color.equals(tile2.color))
                                    return false;
                            }
                        }
                    } else if (tile1.xPos == tile2.xPos) {
                        if(xpos == tile1.xPos) {
                            if (tile.shape.equals(tile1.shape))
                                return false;
                        }
                        else {
                            if(ypos == tile1.yPos) {
                                if (tile.color.equals(tile1.color))
                                    return false;
                            }
                            else if (ypos == tile2.yPos) {
                                if (tile.color.equals(tile2.color))
                                    return false;
                            }
                        }
                    }
                }
                else if(tile1.color.equals(tile2.color)) {
                    if (tile1.yPos == tile2.yPos) {
                        if (ypos == tile1.yPos) {
                            if(tile.color.equals(tile1.color))
                                return false;
                        }
                        else {
                            if(xpos == tile1.xPos) {
                                if(tile.shape.equals(tile1.shape))
                                    return false;
                            }
                            else if(xpos == tile2.xPos) {
                                if (tile.shape.equals(tile2.shape))
                                    return false;
                            }
                        }
                    } else if (tile1.xPos == tile2.xPos) {
                        if(xpos == tile1.xPos) {
                            if (tile.color.equals(tile1.color))
                                return false;
                        }
                        else {
                            if(ypos == tile1.yPos) {
                                if (tile.shape.equals(tile1.shape))
                                    return false;
                            }
                            else if (ypos == tile2.yPos) {
                                if (tile.shape.equals(tile2.shape))
                                    return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean equivalent(int xpos, int ypos, Tile tile1) {
        if(xpos >= 0 && ypos >= 0 && xpos < XLENGTH && ypos < YLENGTH) {
            Tile tile2 = tempBoard[xpos][ypos];
            if(tile1 != null && tile2 != null) {
                if(!(tile1.shape.equals(tile2.shape) && tile1.color.equals(tile2.color))) {
                    if (tile1.shape.equals(tile2.shape)) {
                        return true;
                    } else {
                        return tile1.color.equals(tile2.color);
                    }
                }
            }
        }
        return false;
    }

    private boolean nul(int xpos, int ypos) {
        if(withinBounds(xpos, ypos)) {
            Tile tile = tempBoard[xpos][ypos];
            return tile == null;
        }
        return true;
    }

    private boolean withinBounds(int xpos, int ypos) {
        return xpos >= 0 && ypos >= 0 && xpos < XLENGTH && ypos < YLENGTH;
    }

    private void assignPoints() {
        /*for (Tile tile: places) {
            calculate(tile.xPos, tile.yPos, 0, 0);
            // points--;
            Log.i(TAG,  cPlayer.name+ " EARNED: >>>>> " + points);
            Log.i(TAG, "assignPoints: ---------------------------------------");
            cPlayer.points = cPlayer.points + points;
            points = 0;
        }*/
        // reinitialize
        places = new ArrayList<>();
    }

    private void score() {
        Log.i(TAG, "---------------------------------------");
        for (Player player: players) {
            Log.i(TAG, player.name + ": " + player.points);
        }
        Log.i(TAG, "---------------------------------------");
    }
}
