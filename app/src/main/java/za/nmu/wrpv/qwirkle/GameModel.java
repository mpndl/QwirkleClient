package za.nmu.wrpv.qwirkle;

import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class GameModel implements Serializable {
    public static Player currentPlayer;
    public static Player player;
    public static List<Tile> bag = new ArrayList<>();
    public static List<Player> players;
    private static final Stack<Tile> paths2 = new Stack<>();
    private static List<Tile> qwirkleMonitor = new ArrayList<>();
    public static final int XLENGTH = 50;
    public static final int YLENGTH = 50;
    public static List<Tile> places = new ArrayList<>();
    private static int placedCount = 0;
    private static int points = 0;
    private static Tile[][] board = new Tile[XLENGTH][YLENGTH];
    private static Tile[][] tempBoard = null;
    private static List<Tile> ts = new ArrayList<>();
    private static boolean placing = false;

    public static MessagesAdapter adapter;

    public enum Legality {
        LEGAL, ILLEGAL;
    }

    public static int geBagCount() {
        return bag.size();
    }

    public static int playerCount() {
        return players.size();
    }

    public static void addMessage(PlayerMessage playerMessage) {
        adapter.add(playerMessage);
    }

    public static boolean isTurn() {
        return player.name == currentPlayer.name;
    }

    private static int getPlayerHighestCCount(List<Tile> playerTiles) {
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

    private static int getPlayerHighestSCount(List<Tile> playerTiles) {
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

    public static void draw(boolean played, List<Tile> playerTiles) {
        int HCOUNT = 6;
        int hcount = HCOUNT;
        if(bag.size() > 0) {
            if(played) {
                if(bag.size() < HCOUNT) hcount = bag.size();
                for (int i = 0; i < hcount; i++) {
                    if(currentPlayer.tiles.size() < HCOUNT) {
                        currentPlayer.tiles.add(bag.remove(i));
                        hcount--;
                    }
                }
                if(isBonus())
                    currentPlayer.points = currentPlayer.points + 6;
            }
            else {
                currentPlayer.tiles.addAll(places);
                if (playerTiles == null) {
                    bag.addAll(currentPlayer.tiles);
                    hcount = HCOUNT;
                    currentPlayer.tiles.removeAll((ArrayList<Tile>)((ArrayList<Tile>) currentPlayer.tiles).clone());
                }
                else {
                    bag.addAll(playerTiles);
                    hcount = playerTiles.size();
                    currentPlayer.tiles.removeAll((ArrayList<Tile>)((ArrayList)playerTiles).clone());
                }
                Collections.shuffle(bag);
                for (int i = 0; i < hcount; i++) {
                    Tile tile = bag.remove(i);
                    currentPlayer.tiles.add(tile);
                }
            }
            placing = false;
        }
    }

    public static void turn() {
        int i = 0;
        for (; i < players.size(); i++) {
            if(currentPlayer.name == players.get(i).name) {
                i++;
                if(i >= players.size()) i = 0;
                currentPlayer = players.get(i);
                tempBoard = null;
                return;
            }
        }
    }

    public static Legality place(int xpos, int ypos, Tile tile) {
        if(!placing) {
            places = new ArrayList<>();
        }
        if(legal(xpos, ypos, tile) == Legality.LEGAL) {
            if(!placing) {
                placing = true;
            }

            currentPlayer.tiles.remove(tile);

            tile.xPos = xpos;
            tile.yPos = ypos;

            places.add(tile);

            tempBoard[xpos][ypos] = tile;

            return Legality.LEGAL;
        }
        else {
            Toast.makeText(ServerHandler.activity, "ILLEGAL MOVE", Toast.LENGTH_SHORT).show();
            return Legality.ILLEGAL;
        }
    }

    public static void recover() {
        if(tempBoard != null) {
            board = copy(tempBoard);
        }
    }

    public static Player getWinner() {
        int max = Integer.MIN_VALUE;
        Player winner = null;
        for (Player player: players) {
            if (player.points > max)
                winner = player;
        }
        return winner;
    }

    private static Tile[][] copy(Tile[][] src) {
        Tile[][] temp = new Tile[src.length][src[0].length];
        for(int i=0; i<src.length; i++) {
            System.arraycopy(src[i], 0, temp[i], 0, src[i].length);
        }
        return temp;
    }

    public static void backup() {
        tempBoard = copy(board);
    }

    public static List<Tile> play() {
        if(places.size() > 0) {
            placing = false;
            placedCount = placedCount + places.size();
            assignPoints();
            if (geBagCount() > 0)
                draw(true, null);
            return bag;
        }
        return null;
    }

    private static Legality legal(int xpos, int ypos, Tile tile) {
        if (tempBoard == null) backup();

        if(places.size() == 0 && placedCount == 0) {
            return Legality.LEGAL;
        }
        else if (allSidesNull(xpos, ypos)) {
            return Legality.ILLEGAL;
        }

        if (illegalOrientation(xpos, ypos))
            return Legality.ILLEGAL;

        if (!adjEquivalent(xpos, ypos, tile, places))
            return Legality.ILLEGAL;

        if (nullInBetween(xpos, ypos, places)) {
            return Legality.ILLEGAL;
        }

        if (next(xpos, ypos, tile))
            return Legality.LEGAL;

        return Legality.ILLEGAL;
    }

    private static boolean allSidesNull(int xpos, int ypos) {
        return nul(xpos + 1, ypos) && nul(xpos - 1, ypos) && nul(xpos, ypos + 1) && nul(xpos, ypos - 1);
    }

    private static boolean nullInBetween(int xpos, int ypos, List<Tile> ts) {
        Tile nullTile = nullTile(ts, tempBoard);

        if (nullTile != null) {

            Tile tile = new Tile();
            tile.xPos = xpos;
            tile.yPos = ypos;

            List<Tile> tempTs = new ArrayList<>(ts);
            tempTs.add(tile);

            Tile tempNullTile = new Tile();
            tempNullTile.xPos = nullTile.xPos;
            tempNullTile.yPos = nullTile.yPos;
            int[] orientation = orientation(tempTs);

            if (orientation[0] == 1) {
                if (tempNullTile.xPos > xpos) {
                    while (tempNullTile.xPos > xpos) {
                        if (tempBoard[tempNullTile.xPos][tempNullTile.yPos] == null)
                            return true;
                        tempNullTile.xPos--;
                    }
                }
                else {
                    while (tempNullTile.xPos < xpos) {
                        if (tempBoard[tempNullTile.xPos][tempNullTile.yPos] == null)
                            return true;
                        tempNullTile.xPos++;
                    }
                }
            } else if (orientation[1] == 1) {
                if (tempNullTile.yPos > ypos) {
                    while (tempNullTile.yPos > ypos) {
                        if (tempBoard[tempNullTile.xPos][tempNullTile.yPos] == null)
                            return true;
                        tempNullTile.yPos--;
                    }
                }
                else {
                    while (tempNullTile.yPos < ypos) {
                        if (tempBoard[tempNullTile.xPos][tempNullTile.yPos] == null)
                            return true;
                        tempNullTile.yPos++;
                    }
                }
            }
        }
        return false;
    }

    private static boolean illegalOrientation(int xpos, int ypos) {
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

    private static boolean next(int xpos, int ypos,Tile tile) {
        if(!identical(xpos, ypos, -1, 0, tile) && !identical(xpos, ypos, 1, 0, tile) &&
                !identical(xpos, ypos, 0, -1, tile) && !identical(xpos, ypos, 0, 1, tile)) {
            if(equivalent(xpos - 1, ypos, tile) && equivalent(xpos + 1, ypos, tile)
                    && equivalent(xpos, ypos - 1, tile) && equivalent(xpos, ypos + 1, tile)) {
                List<Tile> tsLeft = getAdjTiles(xpos, ypos, -1, 0);
                ts = new ArrayList<>();
                List<Tile> tsRight = getAdjTiles(xpos, ypos, 1, 0);
                ts = new ArrayList<>();
                List<Tile> tsTop = getAdjTiles(xpos, ypos, 0, -1);
                ts = new ArrayList<>();
                List<Tile> tsBottom = getAdjTiles(xpos, ypos, 0, 1);

                return (adjEquivalent(xpos, ypos, tile, tsRight) && adjEquivalent(xpos, ypos, tile, tsLeft)
                        && adjEquivalent(xpos, ypos, tile, tsTop) && adjEquivalent(xpos, ypos, tile, tsBottom));
            }
        }
        return false;
    }

    private static boolean adjEquivalent(int xpos, int ypos, Tile tile, List<Tile> ts) {
        if (ts.size() == 0) {
            return true;
        }
        else if (ts.size() == 1) {
            Tile tile2 = ts.get(0);
            if (xpos == tile2.xPos || ypos == tile2.yPos) {
                if (tile2.color.equals(tile.color))
                    return true;
                else return tile2.shape.equals(tile.shape);
            }
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


    private static List<Tile> getAdjTiles(int xpos, int ypos, int xdir, int ydir) {
        if (!nul(xpos + xdir, ypos + ydir)) {
            ts.add(tempBoard[xpos + xdir][ypos + ydir]);

            return getAdjTiles(xpos + xdir, ypos + ydir, xdir, ydir);
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
        return (List<Tile>) ((ArrayList)ts).clone();
    }

    private static boolean identical(int xpos, int ypos, int xdir, int ydir, Tile tile1) {
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

    private static boolean equivalent(int xpos, int ypos, Tile tile1) {
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

    private static boolean nul(int xpos, int ypos) {
        if(withinBounds(xpos, ypos)) {
            Tile tile = tempBoard[xpos][ypos];
            return tile == null;
        }
        return true;
    }

    private static boolean withinBounds(int xpos, int ypos) {
        return xpos >= 0 && ypos >= 0 && xpos < XLENGTH && ypos < YLENGTH;
    }

    private static void assignPoints() {
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
            currentPlayer.points = currentPlayer.points + 6;
        currentPlayer.points = currentPlayer.points + points;
        // reinitialize
        places = new ArrayList<>();
        qwirkleMonitor = new ArrayList<>();
        points = 0;
    }

    private static void calculate(int xpos, int ypos, int xdir, int ydir, Tile[][] board, int[] orientation) {
        if (!nul(xpos, ypos)) {
            getWithPaths(board[xpos][ypos], orientation(places));
            if (!qwirkleMonitor.contains(board[xpos][ypos]))
                qwirkleMonitor.add(board[xpos][ypos]);
            points++;
            calculate(xpos + xdir, ypos + ydir, xdir, ydir, board, orientation);
            if (paths2.contains(board[xpos][ypos])) {
                if (places.size() > 1)
                    points++;
                if (orientation[1] == 1) {
                    if (!nul(xpos - 1, ypos)) {
                        calculate(xpos - 1, ypos, -1, 0, board, new int[]{1, 0});
                    }
                    else if (!nul(xpos + 1, ypos)) {
                        calculate(xpos + 1, ypos, +1, 0, board, new int[]{1, 0});
                    }
                }
                else if (orientation[0] == 1) {
                    if (!nul(xpos, ypos - 1)) {
                        calculate(xpos, ypos - 1, 0, -1, board, new int[]{0, 1});
                    }
                    else if (!nul(xpos, ypos + 1)) {
                        calculate(xpos, ypos + 1, 0, +1, board, new int[]{0, 1});
                    }
                }
            }
        }
    }

    private static boolean isQwirkle() {
        return getPlayerHighestCCount(qwirkleMonitor) == 6 || getPlayerHighestSCount(qwirkleMonitor) == 6;
    }

    private static boolean isBonus() {
        return bag.size() == 0;
    }

    private static void getWithPaths(Tile tile, int[] orientation) {
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

    private static int[] orientation(List<Tile> places) {
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

    private static Tile nullTile(List<Tile> places, Tile[][] board) {
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
