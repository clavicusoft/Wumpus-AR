package com.clavicusoft.wumpus.AR;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.beyondar.android.world.GeoObject;
import com.clavicusoft.wumpus.Database.AdminSQLite;
import com.clavicusoft.wumpus.Maze.CaveContent;
import com.clavicusoft.wumpus.Maze.Graph;
import com.clavicusoft.wumpus.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class Game_Data {

    private int game_ID;
    private Graph graph;

    private CaveContent[] caveContents;
    private Context game_Context;
    private int currentCave;

    public int getGame_ID() {
        return game_ID;
    }

    public Graph getGraph() {
        return graph;
    }

    public int getCurrentCave() {
        return currentCave;
    }

    public void setCurrentCave(int cave){
        this.currentCave = cave;
    }

    /**
     * Creates a new Game_Data object.
     *
     * @param context Game's context.
     * @param game_ID ID of the current game.
     * @param currentCave Current cave number.
     */
    public Game_Data (Context context, int game_ID, int currentCave) {
        this.game_ID = game_ID;
        this.game_Context = context;
        this.currentCave = currentCave;
        setInitialData();
    }

    /**
     * Initializes the data.
     */
    public void setInitialData () {
        int graph_ID = getGraphID();
        if (graph_ID != -1) {
            Boolean graph_Created = createGraph(graph_ID);
            if (graph_Created) {
                setCaveContent();
            }
        }
    }

    /**
     * Gets the graph_ID from the current game.
     *
     * @return Graph's DB ID.
     */
    public int getGraphID() {
        AdminSQLite admin = new AdminSQLite(game_Context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT graph_id FROM GAME WHERE id = " +
                String.valueOf(game_ID) + ";", null);

        int result = -1;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    /**
     * Creates a Graph with the graph_ID information.
     *
     * @param graph_ID Graph's DB ID.
     * @return True if the graph was creates | False if the Graph was not created.
     */
    public Boolean createGraph(int graph_ID) {
        Boolean result = false;

        AdminSQLite admin = new AdminSQLite(game_Context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cell = db.rawQuery("SELECT GRAPH.relations, GRAPH.number_of_caves FROM GRAPH " +
                "WHERE GRAPH.id = " + graph_ID + ";", null);
        if (cell.moveToFirst()) {
            graph = new Graph(cell.getInt(1));
            graph.stringToArray(cell.getString(0));
            result = true;
        }
        cell.close();

        return result;
    }

    /**
     * Gets the cave content from the DB and stores it.
     */
    public void setCaveContent() {
        caveContents = new CaveContent[graph.getMaximumCaves()];
        AdminSQLite admin = new AdminSQLite(game_Context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cell = db.rawQuery("SELECT GAME.cave_number, CAVE_CONTENT.content FROM GAME, CAVE_CONTENT " +
                "WHERE GAME.id = " + game_ID + " AND CAVE_CONTENT.id = GAME.content;", null);
        if (cell.moveToFirst()) {
            do{
                caveContents[cell.getInt(0) - 1] = getContentFromString(cell.getString(1));
            }while(cell.moveToNext());
        }
        cell.close();
    }

    /**
     * Gets the content of a cave.
     *
     * @param cave_Number Number of the cave.
     * @return The content of the cave.
     */
    public CaveContent getCaveContent (int cave_Number) {
        return caveContents[cave_Number];
    }

    /**
     * Gets the content of a cave based on a string.
     *
     * @param content String containing the cave content.
     * @return The content of the cave.
     */
    public CaveContent getContentFromString (String content) {
        CaveContent result;
        switch (content){
            case "WUMPUS":
                result = CaveContent.WUMPUS;
                break;
            case "PIT":
                result = CaveContent.PIT;
                break;
            case "BAT":
                result = CaveContent.BAT;
                break;
            default:
                result = CaveContent.EMPTY;
                break;
        }
        return result;
    }

    public CaveContent[] getCaveContents() {
        return caveContents;
    }

    public void setCaveContents(CaveContent[] caveContents) {
        this.caveContents = caveContents;
    }

    public int chooseRandomCave(int cave, int totalCaves){
        Random rand = new Random();
        int newCave;
        boolean validCave;
        do {
            newCave = rand.nextInt(totalCaves) + 1;
            validCave = isValid(newCave);
        }while((newCave == cave) || !(validCave));
        return newCave;
    }

    /**
     * Chooses a random empty cave for the player to start.
     *
     * @param totalCaves Total caves in the world.
     * @return Number of the cave.
     */
    public int chooseStartingCave(int totalCaves){
        Random rand = new Random();
        int newCave;
        boolean validCave;
        do {
            newCave = rand.nextInt(totalCaves) + 1;
            validCave = isValid(newCave);
        }while(!(validCave));
        return newCave;
    }

    public boolean isValid(int checkCave){
        boolean valid = false;
        CaveContent caveContent;
        caveContent = getCaveContent(checkCave-1);
        if(caveContent == CaveContent.EMPTY){
            valid = true;
        }
        return valid;
    }

    /**
     * Gets the distance between the user and a cave.
     * @param current_Latitude Current user latitude.
     * @param current_Longitude Current user longitude.
     * @param cave_Number Cave number.
     * @return Distance in meters between the user and the cave.
     */
    public double checkDistance(double current_Latitude, double current_Longitude, int cave_Number) {
        Location loc1 = new Location("");
        loc1.setLatitude(current_Latitude);
        loc1.setLongitude(current_Longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(getLatitudeFromCave(cave_Number));
        loc2.setLongitude(getLongitudeFromCave(cave_Number));

        return round(loc1.distanceTo(loc2), 1);
    }

    /**
     * Gets the latitude of a cave.
     *
     * @param cave_Number Number of the cave.
     * @return Latitude of the cave.
     */
    public double getLatitudeFromCave (int cave_Number){
        AdminSQLite admin = new AdminSQLite(game_Context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT latitude FROM GAME WHERE id = " +
                game_ID + " AND cave_number = " + String.valueOf(cave_Number) + ";", null);

        double result = 0;

        if (cursor.moveToFirst()) {
            result = Double.valueOf(cursor.getString(0));
        }

        cursor.close();
        return result;
    }

    /**
     * Gets the longitude of a cave.
     *
     * @param cave_Number Number of the cave.
     * @return Longitude of the cave.
     */
    public double getLongitudeFromCave (int cave_Number) {
        AdminSQLite admin = new AdminSQLite(game_Context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT longitude FROM GAME WHERE id = " +
                game_ID + " AND cave_number = " + String.valueOf(cave_Number) + ";", null);

        double result = 0;

        if (cursor.moveToFirst()) {
            result = Double.valueOf(cursor.getString(0));
        }

        cursor.close();
        return result;
    }

    /**
     * Rounds a value to a certain number of decimals.
     * @param value Value to round.
     * @param places Number of decimals.
     * @return Rounded value.
     */
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
