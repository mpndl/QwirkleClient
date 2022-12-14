package za.nmu.wrpv.qwirkle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameModel implements Serializable {
    public static int gameID = -1;
    public static List<PlayerMessage> messages = new ArrayList<>();
    public static Player currentPlayer;
    public static Player prevCurrentPlayer;
    public static Player player;
    public static String playerName = "";
    public static List<Tile> bag = new ArrayList<>();
    public static List<Player> players;
    public static final int XLENGTH = 50;
    public static final int YLENGTH = 50;
    public static List<Tile> places = new ArrayList<>();
    private static int points = 0;
    public static Tile[][] board = new Tile[XLENGTH][YLENGTH];
    public static Tile[][] tempBoard = null;
    private static List<Tile> ts = new ArrayList<>();
    public static boolean placing = false;
    public static boolean ended = false;
    public static List<Tile> visitedTiles = new ArrayList<>();
    public static List<Tile> placed = new ArrayList<>();
    public static List<Tile> placedCopy = new ArrayList<>();

    public static void updatePlayerTiles(Player player) {
        for (Player p: players) {
            if (p.name == player.name) {
                p.tiles = player.tiles;
                return;
            }
        }
    }

    public static void addPlayer(Player player) {
        players.add(player);
    }

    public static void removePlayer(Player player) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (player.name == p.name) {
                players.remove(i);
                return;
            }
        }
    }

    public static int getPlayerIndex(Player player) {
        return players.indexOf(player);
    }

    public static Player getPlayer(String name) {
        return (Player) players.stream().filter(player -> player.name.toString().equals(name)).toArray()[0];
    }

    public static boolean gameEnded() {
        if (player.name == currentPlayer.name) {
            if (player.tiles.size() == 0) {
                ended = true;
                return true;
            }
        }
        return false;
    }

    public static int getBagCount() {
        return bag.size();
    }

    public static boolean isTurn() {
        if (players.size() > 1) return player.name == currentPlayer.name;
        return false;
    }

    public static void undo(List<Tile> playerTiles) {
        player.tiles.addAll(playerTiles);
        tempBoard = null;
        placing = false;
    }

    public static void draw(boolean played, List<Tile> playerTiles) {
        int HCOUNT = 6;
        int hcount = HCOUNT;
        if(bag.size() > 0) {
            if(played) {
                hcount = hcount - currentPlayer.tiles.size();
                if(bag.size() < hcount) hcount = bag.size();
                for (int i = 0; i < hcount; i++) {
                    if(currentPlayer.tiles.size() < HCOUNT) {
                        currentPlayer.tiles.add(bag.remove(bag.size() - 1));
                    }
                }
                if(isBonus())
                    currentPlayer.points = currentPlayer.points + 6;
            }
            else {
                if (playerTiles == null) {
                    bag.addAll(currentPlayer.tiles);
                    hcount = HCOUNT;
                    currentPlayer.tiles.removeAll((ArrayList<Tile>)((ArrayList<Tile>) currentPlayer.tiles).clone());
                }
                else {
                    Set<Tile> set = new HashSet<>(playerTiles);
                    List<Tile> uniqueTiles = new ArrayList<>(set);

                    bag.addAll(uniqueTiles);
                    hcount = uniqueTiles.size();
                    currentPlayer.tiles.removeAll((ArrayList<Tile>)((ArrayList)uniqueTiles).clone());
                }
                Collections.shuffle(bag);
                for (int i = 0; i < hcount; i++) {
                    Tile tile = bag.remove(bag.size() - 1);
                    currentPlayer.tiles.add(tile);
                }
            }
            placing = false;
        }
    }

    public static void setNewCurrentPlayer(int index) {
        try {
            if (index != -1) currentPlayer = players.get(index);
            else currentPlayer = players.get(0);
        }catch (IndexOutOfBoundsException ignored) {}
    }

    public static List<int[]> validPlaces(Tile tile) {
        List<int[]> validPlaces = new ArrayList<>();
        List<Tile> placed2 = new ArrayList<>(placed);
        placed2.addAll(places);

        Tile[][] temp2Board = copy(tempBoard);
        if (places.size() == 0) backup();
        for (Tile placedTile : placed2) {
            int[] left = new int[]{placedTile.xPos - 1, placedTile.yPos, (placedTile.yPos) * XLENGTH + (placedTile.xPos - 1)};
            int[] right = new int[]{placedTile.xPos + 1, placedTile.yPos, (placedTile.yPos) * XLENGTH + (placedTile.xPos + 1)};
            int[] top = new int[]{placedTile.xPos, placedTile.yPos - 1, (placedTile.yPos - 1) * XLENGTH + (placedTile.xPos)};
            int[] bottom = new int[]{placedTile.xPos, placedTile.yPos + 1, (placedTile.yPos + 1) * XLENGTH + (placedTile.xPos)};

            /*System.out.println("left = " + left[0] + ", " + left[1]);
            System.out.println("right = " + right[0] + ", " + right[1]);
            System.out.println("top = " + top[0] + ", " + top[1]);
            System.out.println("bottom = " + bottom[0] + ", " + bottom[1]);*/

            Legality lLegal = legal(left[0], left[1], tile);
            Legality rLegal = legal(right[0], right[1], tile);
            Legality tLegal = legal(top[0], top[1], tile);
            Legality bLegal = legal(bottom[0], bottom[1], tile);

            if (lLegal.equals(Legality.LEGAL)) validPlaces.add(left);
            if (rLegal.equals(Legality.LEGAL)) validPlaces.add(right);
            if (tLegal.equals(Legality.LEGAL)) validPlaces.add(top);
            if (bLegal.equals(Legality.LEGAL)) validPlaces.add(bottom);
        }
        tempBoard = temp2Board;
        return validPlaces;
    }

    public static Player clonePlayer(Player player) {
        Player temp = new Player();
        temp.name = player.name;
        temp.color = player.color;
        temp.points = player.points;
        temp.tiles = cloneTiles(player.tiles);
        return temp;
    }

    public static void addPlayerSorted(Player player) {
        int index = Collections.binarySearch(players, player, Comparator.comparing(p -> p.name.toString()));
        if (index < 0) {
            index = -index -1;
            players.add(index, player);
        }
    }

    public static List<Player> clonePlayers(List<Player> players) {
        List<Player> playersCopy = new ArrayList<>();
        for (Player player: players) {
            playersCopy.add(clonePlayer(player));
        }
        return playersCopy;
    }

    public static void updatePlayerScore(Player player) {
        for (Player p: players) {
            if (p.name.toString().equals(player.name.toString())) {
                p.points = player.points;
                return;
            }
        }
    }

    public static Legality place(int xpos, int ypos, Tile tile) {
        if(!placing) {
            places = new ArrayList<>();
            visitedTiles.clear();
        }
        Legality legality = legal(xpos, ypos, tile);
        if(legality == Legality.LEGAL) {
            if(!placing) {
                placing = true;
            }

            player.tiles.remove(tile);

            tile.xPos = xpos;
            tile.yPos = ypos;

            places.add(tile);

            tempBoard[xpos][ypos] = GameModel.cloneTile(tile);


            return Legality.LEGAL;
        }
        else {
            return legality;
        }
    }

    public static void recover() {
        if(tempBoard != null) {
            board = copy(tempBoard);
        }
    }

    private static Tile[][] copy(Tile[][] src) {
        if (src == null) return null;
        Tile[][] temp = new Tile[src.length][src[0].length];
        for(int i=0; i<src.length; i++) {
            System.arraycopy(src[i], 0, temp[i], 0, src[i].length);
        }
        return temp;
    }

    public static void backup() {
        tempBoard = copy(board);
    }

    public static void play() {
        if(places.size() > 0) {
            placing = false;
            placedCopy = cloneTiles(placed);
            placed.addAll(cloneTiles(places));
            assignPoints();
            if (getBagCount() > 0) draw(true, places);
        }
    }

    private static Legality legal(int xpos, int ypos, Tile tile) {
        if (tempBoard == null) backup();

        /*System.out.println("====================================================================");
        System.out.println("--------------------- left = " + tempBoard[xpos - 1][ypos]);
        System.out.println("--------------------- right = " + tempBoard[xpos + 1][ypos]);
        System.out.println("--------------------- up = " + tempBoard[xpos][ypos + 1]);
        System.out.println("--------------------- down = " + tempBoard[xpos][ypos - 1]);
        System.out.println("====================================================================");*/

        if(places.size() == 0 && placed.size() == 0) {
            return Legality.LEGAL;
        }
        else if (tempBoard[xpos][ypos] != null) {
            return Legality.TILE_EXISTS;
        }
        else if (allSidesNull(xpos, ypos)) {
            //System.out.println("--------------------------------------- allSidesNull(xpos, ypos)");
            return Legality.LONER_TILE;
        }

        if (illegalOrientation(xpos, ypos)) {
            //System.out.println("--------------------------------------- (illegalOrientation(xpos, ypos)");
            return Legality.ORIENTATION;
        }

        if (!adjEquivalent(xpos, ypos, tile, places)) {
            //System.out.println("--------------------------------------- !adjEquivalent(xpos, ypos, tile, places)");
            return Legality.PLACED_TILES_NOT_EQUIVALENT;
        }

        if (nullInBetween(xpos, ypos, places)) {
            //System.out.println("--------------------------------------- nullInBetween(xpos, ypos, places)");
            return Legality.NULL_IN_BETWEEN;
        }

        Legality legality = next(xpos, ypos, tile);
        if (legality != Legality.LEGAL) {
            //System.out.println("--------------------------------------- !next(xpos, ypos, tile)");
            return legality;
        }
        return Legality.LEGAL;
    }

    public enum Legality {
        LEGAL, ORIENTATION, TILE_EXISTS, LONER_TILE, NULL_IN_BETWEEN, PLACED_TILES_NOT_EQUIVALENT,
        PLACED_TILES_NOT_EQUIVALENT_WITH_ADJACENT_TILES, PLACED_TILE_EXISTS_ON_ADJACENT_LINE,
        PLACED_TILE_FORMING_IDENTICAL_ATTRIBUTE_LINE_TO_ADJACENT_TILE_LINE
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

    private static Legality next(int xpos, int ypos,Tile tile) {
        if(!identical(xpos, ypos, -1, 0, tile) && !identical(xpos, ypos, 1, 0, tile) &&
                !identical(xpos, ypos, 0, -1, tile) && !identical(xpos, ypos, 0, 1, tile)) {
            if(equivalent(xpos - 1, ypos, tile) && equivalent(xpos + 1, ypos, tile)
                    && equivalent(xpos, ypos - 1, tile) && equivalent(xpos, ypos + 1, tile)) {
                //System.out.println("----------------------------------------------- NEXT START ------------------------------------");
                ts = new ArrayList<>();
                List<Tile> tsLeft = getAdjTiles(xpos, ypos, -1, 0);
                //System.out.println("---------------------------------- LEFT");
                //tsLeft.forEach(System.out::println);
                ts = new ArrayList<>();
                List<Tile> tsRight = getAdjTiles(xpos, ypos, 1, 0);
                //System.out.println("---------------------------------- RIGHT");
                //tsRight.forEach(System.out::println);
                ts = new ArrayList<>();
                List<Tile> tsTop = getAdjTiles(xpos, ypos, 0, -1);
                //System.out.println("----------------------------------- TOP");
                //tsTop.forEach(System.out::println);
                ts = new ArrayList<>();
                List<Tile> tsBottom = getAdjTiles(xpos, ypos, 0, 1);
                //System.out.println("------------------------------------ BOTTOM");
                //tsBottom.forEach(System.out::println);

                boolean retLeft = adjEquivalent(xpos, ypos, tile, tsLeft);
                //System.out.println("--------------------------- RET LEFT = " + retLeft);
                boolean retRight = adjEquivalent(xpos, ypos, tile, tsRight);
                //System.out.println("--------------------------- RET RIGHT = " + retRight);
                boolean retTop = adjEquivalent(xpos, ypos, tile, tsTop);
                //System.out.println("--------------------------- RET TOP = " + retTop);
                boolean retBottom = adjEquivalent(xpos, ypos, tile, tsBottom);
                //System.out.println("--------------------------- RET BOTTOM = " + retBottom);

                if (retRight && retLeft && retTop && retBottom) return Legality.LEGAL;
                else return Legality.PLACED_TILES_NOT_EQUIVALENT_WITH_ADJACENT_TILES;
            } else return Legality.PLACED_TILE_FORMING_IDENTICAL_ATTRIBUTE_LINE_TO_ADJACENT_TILE_LINE;
        }else return Legality.PLACED_TILE_EXISTS_ON_ADJACENT_LINE;
    }

    public static List<Tile> cloneTiles(List<Tile> tiles) {
        List<Tile> temp = new ArrayList<>();
        for (Tile tile : tiles) {
            temp.add(cloneTile(tile));
        }
        return temp;
    }

    public static Tile cloneTile(Tile tile) {
        Tile tempTile = new Tile();
        tempTile.xPos = tile.xPos;
        tempTile.yPos = tile.yPos;
        tempTile.color = tile.color;
        tempTile.shape = tile.shape;
        tempTile.index = tile.index;
        return tempTile;
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
            /*System.out.println("tile1.shape.equals(tile2.shape) -> " + tile1.shape + " == " + tile2.shape);
            System.out.println("ypos == tile1.yPos -> " + ypos + " == " + tile1.yPos);
            System.out.println("tile.shape.equals(tile1.shape) -> " + tile.shape + " == " + tile1.shape);*/
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
            //System.out.println("xpos + xdir = " + (xpos + xdir) + ", ypos + ydir = " + (ypos + ydir) + " -> " + tempBoard[xpos + xdir][ypos + ydir]);
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
        //System.out.println("-------------------------- ASSIGN POINTS START ---------------------");
        //System.out.println("BEFORE POINTS = " + currentPlayer.points);
        int[] orientation = orientation(places);
        Tile nullTile = nullTile(places, tempBoard);
        //if (nullTile != null)
            //System.out.println("NULL TILE = " + nullTile + " -> xpos, ypos = " + nullTile.xPos + ", " + nullTile.yPos);
        if (orientation[0] == 1) {
            //System.out.println("---------------------------- HORIZONTALLY ORIENTED");
            if (nullTile != null) {
                if (!nul(nullTile.xPos + 1, nullTile.yPos)) {
                    calculate(nullTile.xPos, nullTile.yPos, +1, 0, tempBoard);
                }
                else {
                    calculate(nullTile.xPos, nullTile.yPos, -1, 0, tempBoard);
                }
            }
        }
        else if (orientation[1] == 1) {
            //System.out.println("---------------------------- VERTICALLY ORIENTED");
            if (nullTile != null) {
                if (!nul(nullTile.xPos, nullTile.yPos + 1)) {
                    calculate(nullTile.xPos, nullTile.yPos, 0, + 1, tempBoard);
                }
                else {
                    calculate(nullTile.xPos, nullTile.yPos, 0, - 1, tempBoard);
                }
            }
        }
        if (qwirkleCount() > 0)
            currentPlayer.points = currentPlayer.points + (6 * qwirkleCount());

        if (points == 0) currentPlayer.points++;
        currentPlayer.points = currentPlayer.points + points;
        //System.out.println("EARNED POINTS = " + points);
        //System.out.println("TOTAL POINTS = " + currentPlayer.points);
        // reinitialize
        points = 0;
        //System.out.println("-------------------------- ASSIGN POINTS END ---------------------");
    }


    private static void calculate(int xpos, int ypos, int xdir, int ydir, Tile[][] board) {
        int gxpos = xpos;
        int gypos = ypos;

        Tile genTile = board[gxpos][gypos];
        while (genTile != null) {
            visitedTiles.add(genTile);
            points++;
            //System.out.println("GENTILE = " + genTile + " -> points = " + points);

            if (contains(places, genTile)) {
                boolean hasPath = false;
                //System.out.println("\tGENTILE IN PLACES");
                int uxpos = gxpos + ydir;
                int uypos = gypos + xdir;
                Tile uTile = board[uxpos][uypos];
                while (uTile != null) {
                    visitedTiles.add(uTile);
                    hasPath = true;
                    points++;
                    //System.out.println("\t\tUTILE = " + uTile + " -> points = " + points);

                    uxpos += ydir;
                    uypos += xdir;
                    if (!nul(uxpos, uypos)) uTile = board[uxpos][uypos];
                    else uTile = null;
                }

                int dxpos = gxpos - ydir;
                int dypos = gypos - xdir;
                Tile dTile = board[dxpos][dypos];
                while (dTile != null) {
                    visitedTiles.add(dTile);
                    hasPath = true;
                    points++;
                    //System.out.println("\t\tDTILE = " + dTile + " -> points = " + points);

                    dxpos -= ydir;
                    dypos -= xdir;
                    if (!nul(dxpos, dypos)) dTile = board[dxpos][dypos];
                    else dTile = null;
                }
                if (hasPath) points++;
            }
            gxpos+=xdir;
            gypos+=ydir;
            if (!nul(gxpos, gypos)) genTile = board[gxpos][gypos];
            else genTile = null;
        }
    }

    public static int qwirkleCount() {
        //System.out.println("IS QWIRKLE COMPUTATION TILES = " + visitedTiles.size());
        Map<String, Integer> xy = new HashMap<>();
        for (Tile tile: visitedTiles) {
           // System.out.println("----------------------------------- START " + tile.index);
           // System.out.println("For xpos = " + tile.xPos + " AND ypos = " + tile.yPos);

            Object x = xy.putIfAbsent(tile.xPos + "x", 0);
            Object y = xy.putIfAbsent(tile.yPos + "y", 0);

            /*System.out.println("x = " + x);
            System.out.println("y = " + y);
            System.out.println("------------------------------------- END " + tile.index);*/
        }

        for (Tile tile: visitedTiles) {
            xy.computeIfPresent(tile.xPos + "x", (key, value) -> ++value);
            xy.computeIfPresent(tile.yPos + "y", (key, value) -> ++value);
        }

        Collection<Integer> values = xy.values();
        int qCount = 0;
        for (int v: values) {
            //System.out.println("IS QWIRKLE -> v = " + v);
            if (v == 6) {
                qCount++;
            }
        }
        return qCount;
    }

    private static boolean isBonus() {
        return bag.size() == 0;
    }

    private static int[] orientation(List<Tile> places) {
        List<Tile> placesCopy = cloneTiles(places);
        if (placesCopy.size() >= 1) {
            if (placesCopy.size() == 1) {
                Tile tile = placesCopy.get(0);
                if (!nul(tile.xPos + 1, tile.yPos))
                    placesCopy.add(tempBoard[tile.xPos + 1][tile.yPos]);
                else if (!nul(tile.xPos - 1, tile.yPos))
                    placesCopy.add(tempBoard[tile.xPos - 1][tile.yPos]);
                else if (!nul(tile.xPos, tile.yPos + 1))
                    placesCopy.add(tempBoard[tile.xPos][tile.yPos + 1]);
                else if (!nul(tile.xPos, tile.yPos - 1))
                    placesCopy.add(tempBoard[tile.xPos][tile.yPos - 1]);
            }
            if (placesCopy.size() >= 2) {
                if (placesCopy.get(0).yPos == placesCopy.get(1).yPos) {
                    return new int[]{1, 0};
                } else {
                    return new int[]{0, 1};
                }
            }
        }
        return new int[] {0, 0};
    }

    private static boolean contains(List<Tile> tiles, Tile tile) {
        return tiles.stream().anyMatch(t -> t.index == tile.index);
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
