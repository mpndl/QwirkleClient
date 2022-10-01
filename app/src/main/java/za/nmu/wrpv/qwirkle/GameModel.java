package za.nmu.wrpv.qwirkle;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class GameModel implements Serializable {
    public Player cPlayer;
    private ArrayList<Tile> tiles = new ArrayList<>();
    public ArrayList<Player> players = new ArrayList<>();
    private Stack<Tile> paths2 = new Stack<>();
    private ArrayList<Tile> qwirkle = new ArrayList<>();
    private final int TCOUNT = 108;
    private final int PCOUNT;
    public final int HCOUNT = 6;
    private final String TAG = "game";
    public final int XLENGTH = 50;
    public final int YLENGTH = 50;
    public ArrayList<Tile> places = new ArrayList<>();
    private int turns = 0;
    private int tempTurns = 0;
    private int placesCount = 0;
    private int points = 0;
    public Tile[][] board = new Tile[XLENGTH][YLENGTH];
    public Tile[][] tempBoard = null;
    ArrayList<Tile> ts = new ArrayList<>();
    public boolean placing = false;
    private final MainActivity mainActivity;

    public List<PlayerMessage> playerMessages = new ArrayList<>();
    private int orderCount = 0;

    public enum Legality {
        LEGAL, ILLEGAL;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public GameModel(int pcount, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        PCOUNT = pcount;
        initializeTiles();
        initializePlayers();
        initialDraw();
        initialPlayer();
    }

    public int geBagCount() {
        return tiles.size();
    }

    private void initializeTiles() {
        ArrayList<Tile.Color> colors = new ArrayList<>(Arrays.asList(Tile.Color.BLUE, Tile.Color.GREEN, Tile.Color.ORANGE, Tile.Color.RED, Tile.Color.PURPLE, Tile.Color.YELLOW));
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initializePlayers() {
        ArrayList<Player.Name> names = new ArrayList<>(Arrays.asList(Player.Name.PLAYER1, Player.Name.PLAYER2, Player.Name.PLAYER3, Player.Name.PLAYER4));
        ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(Color.argb(.3f, 1f, 0f, 0f),
                Color.argb(.3f, 0f, 1f, 0f), Color.argb(.3f, 0f, 1f, 1f),
                Color.argb(.3f, 1f, 1f, 0f)));
        for (int i = 0; i < PCOUNT; i++) {
            Player temp = new Player();
            temp.name = names.get(i);
            temp.color = colors.get(i);
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

    public int playerCount() {
        return players.size();
    }

    private void initialPlayer() {
        int cCount = -1;
        int sCount = -1;
        for (Player player: players) {
            int tempCCount = getPlayerHighestCCount(player.tiles);
            int tempSCount = getPlayerHighestSCount(player.tiles);

            if (tempCCount > cCount || tempSCount > sCount) {
                cCount = tempCCount;
                sCount = tempSCount;
                cPlayer = player;
            }
        }
    }

    public void insertPlayerMessage(PlayerMessage playerMessage) {
        playerMessages.add(playerMessage);
    }

    private int getPlayerHighestCCount(ArrayList<Tile> playerTiles) {
        int red = 0;
        int orange = 0;
        int yellow = 0;
        int green = 0;
        int blue = 0;
        int purple = 0;
        for (Tile tile: playerTiles) {
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

    private int getPlayerHighestSCount(ArrayList<Tile> playerTiles) {
        int clover = 0;
        int fpstar = 0;
        int epstar = 0;
        int square = 0;
        int circle = 0;
        int diamond = 0;
        for (Tile tile: playerTiles) {
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

    public void draw(ArrayList<Tile> ts) {
        int hcount = HCOUNT;
        if(tiles.size() > 0) {
            if(ts == null) {
                if(tiles.size() < 6) hcount = tiles.size();
                for (int i = 0; i < hcount; i++) {
                    if(cPlayer.tiles.size() < 6) {
                        cPlayer.tiles.add(tiles.remove(i));
                    }
                }
            }
            else {
                for (Tile t : (ArrayList<Tile>) ts.clone()) {
                    cPlayer.tiles.remove(t);
                    tiles.add(t);
                }
                Collections.shuffle(tiles);
                for (int i = 0; i < ts.size(); i++) {
                    Tile newTile = tiles.remove(i);
                    cPlayer.tiles.add(newTile);
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
    }

    public Legality place(int xpos, int ypos, Tile tile) {
        if(legal(xpos, ypos, tile) == Legality.LEGAL) {
            if(!placing)
                placing = true;
            tile.xPos = xpos;
            tile.yPos = ypos;
            places.add(tile);
            cPlayer.tiles.remove(tile);
            tempBoard[tile.xPos][tile.yPos] = tile;
            placesCount++;
            tempTurns++;
            return Legality.LEGAL;
        }
        else {
            Toast.makeText(mainActivity, "ILLEGAL MOVE", Toast.LENGTH_SHORT).show();
            return Legality.ILLEGAL;
        }
    }

    public void recover() {
        if(tempBoard != null) {
            board = copy(tempBoard);
            turns = tempTurns;
            placesCount = 0;
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
            assignPoints();
            draw(null);
            return tiles;
        }
        return null;
    }

    private Legality legal(int xpos, int ypos, Tile tile) {
        if (tempBoard == null) backup();
        if (tempBoard[xpos][ypos] != null) {
            return Legality.ILLEGAL;
        }
        if(tempTurns == 0 || placesCount == 0)
            return Legality.LEGAL;
        else if (allSidesNull(xpos, ypos))
            return Legality.ILLEGAL;
        if (illegalOrientation(xpos, ypos))
            return Legality.ILLEGAL;
        if (next(xpos, ypos, tile))
            return Legality.LEGAL;
        return Legality.ILLEGAL;
    }

    private boolean allSidesNull(int xpos, int ypos) {
        return nul(xpos + 1, ypos) && nul(xpos - 1, ypos) && nul(xpos, ypos + 1) && nul(xpos, ypos - 1);
    }

    private boolean illegalOrientation(int xpos, int ypos) {
        if (places.size() > 1) {
            Tile tile1 = places.get(0);
            Tile tile2 = places.get(1);
            if (tile1.xPos == tile2.xPos) {
                return xpos != tile2.xPos;
            }
            else if (tile1.yPos == tile2.yPos) {
                return ypos != tile2.yPos;
            }
        }
        return false;
    }

    private boolean next(int xpos, int ypos,Tile tile) {
        if(!identical(xpos, ypos, -1, 0, tile) && !identical(xpos, ypos, 1, 0, tile) &&
                !identical(xpos, ypos, 0, -1, tile) && !identical(xpos, ypos, 0, 1, tile)) {
            if(equivalent(xpos - 1, ypos, tile) && equivalent(xpos + 1, ypos, tile)
                    && equivalent(xpos, ypos - 1, tile) && equivalent(xpos, ypos + 1, tile)) {
                ArrayList<Tile> tsLeft = getAdjcTiles(xpos, ypos, -1, 0);
                ts = new ArrayList<>();
                ArrayList<Tile> tsRight = getAdjcTiles(xpos, ypos, 1, 0);
                ts = new ArrayList<>();
                ArrayList<Tile> tsTop = getAdjcTiles(xpos, ypos, 0, -1);
                ts = new ArrayList<>();
                ArrayList<Tile> tsBottom = getAdjcTiles(xpos, ypos, 0, 1);

                return (adjcEquivalent(xpos, ypos, tile, tsRight) && adjcEquivalent(xpos, ypos, tile, tsLeft)
                        && adjcEquivalent(xpos, ypos, tile, tsTop) && adjcEquivalent(xpos, ypos, tile, tsBottom));
            }
        }
        return false;
    }

    private boolean adjcEquivalent(int xpos, int ypos, Tile tile, ArrayList<Tile> ts) {
        if (ts.size() == 0) {
            return true;
        }
        else if (ts.size() == 1) {
            Tile tile2 = ts.get(0);
            if(tile2.color.equals(tile.color))
                return true;
            else return tile2.shape.equals(tile.shape);
        }
        else {
            ts.size();
            Tile tile1 = ts.get(0);
            Tile tile2 = ts.get(1);
            if(tile1.shape.equals(tile2.shape)) {
                if (tile1.yPos == tile2.yPos) {
                    if (ypos == tile1.yPos) {
                        return tile.shape.equals(tile1.shape);
                    }
                    else {
                        if(xpos == tile1.xPos) {
                            return tile.color.equals(tile1.color);
                        }
                        else if(xpos == tile2.xPos) {
                            return tile.color.equals(tile2.color);
                        }
                    }
                } else if (tile1.xPos == tile2.xPos) {
                    if(xpos == tile1.xPos) {
                        return tile.shape.equals(tile1.shape);
                    }
                    else {
                        if(ypos == tile1.yPos) {
                            return tile.color.equals(tile1.color);
                        }
                        else if (ypos == tile2.yPos) {
                            return tile.color.equals(tile2.color);
                        }
                    }
                }
            }
            else if(tile1.color.equals(tile2.color)) {
                if (tile1.yPos == tile2.yPos) {
                    if (ypos == tile1.yPos) {
                        return tile.color.equals(tile1.color);
                    }
                    else {
                        if(xpos == tile1.xPos) {
                            return tile.shape.equals(tile1.shape);
                        }
                        else if(xpos == tile2.xPos) {
                            return tile.shape.equals(tile2.shape);
                        }
                    }
                } else if (tile1.xPos == tile2.xPos) {
                    if(xpos == tile1.xPos) {
                        return tile.color.equals(tile1.color);
                    }
                    else {
                        if(ypos == tile1.yPos) {
                            return tile.shape.equals(tile1.shape);
                        }
                        else if(ypos == tile2.yPos) {
                            return tile.shape.equals(tile2.shape);
                        }
                    }
                }
            }
        }
        return false;
    }


    private ArrayList<Tile> getAdjcTiles(int xpos, int ypos, int xdir, int ydir) {
        if (!nul(xpos + xdir, ypos + ydir)) {
            ts.add(tempBoard[xpos + xdir][ypos + ydir]);

            return getAdjcTiles(xpos + xdir, ypos + ydir, xdir, ydir);
        }

        if (ts.size() == 1) {
            Tile temp = ts.get(0);
            if (xdir != 0) {
                if (withinBounds(temp.xPos, temp.yPos - 1) && tempBoard[temp.xPos][temp.yPos - 1] != null) {
                    ts.add(tempBoard[temp.xPos][temp.yPos - 1]);
                }
                else if (withinBounds(temp.xPos, temp.yPos + 1) && tempBoard[temp.xPos][temp.yPos + 1] != null)
                    ts.add(tempBoard[temp.xPos][temp.yPos + 1]);
            }
            else {
                if (withinBounds(temp.xPos - 1, temp.yPos) && tempBoard[temp.xPos - 1][temp.yPos] != null) {
                    ts.add(tempBoard[temp.xPos - 1][temp.yPos]);
                }
                else if (withinBounds(temp.xPos + 1, temp.yPos) && tempBoard[temp.xPos + 1][temp.yPos] != null)
                    ts.add(tempBoard[temp.xPos + 1][temp.yPos]);
            }

        }
        return (ArrayList<Tile>) ts.clone();
    }

    private boolean identical(int xpos, int ypos, int xdir, int ydir, Tile tile1) {
        if(!nul(xpos + xdir, ypos + ydir)) {
            Tile tile2 = tempBoard[xpos + xdir][ypos + ydir];
            if(tile1 != null && tile2 != null) {
                if (tile1.shape.equals(tile2.shape) && tile1.color.equals(tile2.color)) {
                    return true;
                }
                else {
                    return identical(xpos + xdir, ypos + ydir, xdir, ydir, tile1);
                }
            }
            else return false;
        }
        return false;
    }

    private boolean equivalent(int xpos, int ypos, Tile tile1) {
        if(xpos >= 0 && ypos >= 0 && xpos < XLENGTH && ypos < YLENGTH) {
            Tile tile2 = tempBoard[xpos][ypos];
            if(tile1 != null && tile2 != null) {
                if (tile1.shape.equals(tile2.shape))
                    return true;
                else return tile1.color.equals(tile2.color);
            }
            else return true;
        }
        return true;
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
        int[] orientation = orientation(places);
        Tile tile = nullTile(places, tempBoard);
        //Log.i(TAG, "nullTile: " + tile);
        if (orientation[0] == 1) {
            if (tile != null) {
                if (!nul(tile.xPos + 1, tile.yPos))
                    calculate(tile.xPos, tile.yPos, + 1, 0, tempBoard, orientation);
                else
                    calculate(tile.xPos, tile.yPos, - 1, 0, tempBoard, orientation);
            }
        }
        else if (orientation[1] == 1) {
            if (tile != null) {
                if (!nul(tile.xPos, tile.yPos + 1)) {
                    calculate(tile.xPos, tile.yPos, 0, + 1, tempBoard, orientation);
                }
                else {
                    calculate(tile.xPos, tile.yPos, 0, - 1, tempBoard, orientation);
                }
            }
        }
        if (isQwirkle())
            cPlayer.points = cPlayer.points + 6;
        if (isBonus())
            cPlayer.points = cPlayer.points + 6;
        cPlayer.points = cPlayer.points + points;
        Log.i(TAG, "assignPoints: " + points);
        Log.i(TAG, "assignPoints: ---------------------------------------");
        // reinitialize
        places = new ArrayList<>();
        qwirkle = new ArrayList<>();
        points = 0;
    }

    private void calculate(int xpos, int ypos, int xdir, int ydir, Tile[][] board, int[] orientation) {
        if (!nul(xpos, ypos)) {
            Log.i(TAG, "calculate: " + board[xpos][ypos] + " -> " + points);
            getWithPaths(board[xpos][ypos], orientation(places));
            if (!qwirkle.contains(board[xpos][ypos]))
                qwirkle.add(board[xpos][ypos]);
            points++;
            calculate(xpos + xdir, ypos + ydir, xdir, ydir, board, orientation);
            if (paths2.contains(board[xpos][ypos])) {
                if (orientation[1] == 1) {
                    if (!nul(xpos - 1, ypos)) {
                        points++;
                        calculate(xpos - 1, ypos, -1, 0, board, new int[]{1, 0});
                    }
                    else if (!nul(xpos + 1, ypos)) {
                        points++;
                        calculate(xpos + 1, ypos, +1, 0, board, new int[]{1, 0});
                    }
                }
                else if (orientation[0] == 1) {
                    if (!nul(xpos, ypos - 1)) {
                        points++;
                        calculate(xpos, ypos - 1, -1, 0, board, new int[]{0, 1});
                    }
                    else if (!nul(xpos, ypos + 1)) {
                        points++;
                        calculate(xpos, ypos + 1, +1, 0, board, new int[]{0, 1});
                    }
                }
            }
        }
    }

    private boolean isQwirkle() {
        return getPlayerHighestCCount(qwirkle) == 6 || getPlayerHighestSCount(qwirkle) == 6;
    }

    private boolean isBonus() {
        return tiles.size() == 0;
    }

    private void getWithPaths(Tile tile, int[] orientation) {
        if (places.contains(tile) && !paths2.contains(tile)) {
            // Check y
            if (orientation[1] == 1) {
                int x = 0;
                if (!nul(tile.xPos + 1, tile.yPos)) {
                    paths2.add(tile);
                    x++;
                }

                if (!nul(tile.xPos - 1, tile.yPos)) {
                    if (!(x > 0))
                        paths2.add(tile);
                }
            }
            // Check x
            else if (orientation[0] == 1) {
                int y = 0;
                if (!nul(tile.xPos, tile.yPos + 1)) {
                    paths2.add(tile);
                    y++;
                }

                if (!nul(tile.xPos, tile.yPos - 1)) {
                    if (!(y > 0))
                        paths2.add(tile);
                }
            }
        }
    }

    private int[] orientation(ArrayList<Tile> places) {
        if (places.size() >= 1) {
            if (places.size() == 1)
                return new int[]{0, 1};
            if (places.get(0).yPos == places.get(1).yPos) {
                return new int[]{1, 0};
            }
            else {
                return new int[]{0, 1};
            }
        }
        return new int[] {0, 0};
    }

    private Tile nullTile(ArrayList<Tile> places, Tile[][] board) {
        int[] orientation = orientation(places);
        if (orientation[0] == 1) {
            Tile curTile = places.get(0);
            if (nul(curTile.xPos + 1, curTile.yPos) || nul(curTile.xPos - 1, curTile.yPos))
                return curTile;
            while (!nul(curTile.xPos + 1, curTile.yPos) && board[curTile.xPos + 1][curTile.yPos] != null) {
                curTile = board[curTile.xPos + 1][curTile.yPos];
            }
            return curTile;
        }
        else if (orientation[1] == 1){
            Tile curTile = places.get(0);
            if (nul(curTile.xPos, curTile.yPos + 1) || nul(curTile.xPos, curTile.yPos - 1))
                return curTile;
            while (!nul(curTile.xPos, curTile.yPos + 1) && board[curTile.xPos][curTile.yPos + 1] != null) {
                curTile = board[curTile.xPos][curTile.yPos + 1];
            }
            return curTile;
        }
        return null;
    }
}
