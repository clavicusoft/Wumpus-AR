package com.clavicusoft.wumpus.AR;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.clavicusoft.wumpus.Database.AdminSQLite;
import com.clavicusoft.wumpus.R;


public class AR_Helper {
    private World world;
    private int cave_Image;
    //private Location location;

    /**
     * Creates the AR World
     *
     * @param context App´s context.
     */
    public AR_Helper(Context context){
        world = new World(context);
        cave_Image = R.drawable.cave;
    }

    /**
     * Returns the current world
     * @return world
     */
    public World getWorld () {
        return world;
    }

    /**
     * Updates the GeoObjects of the world.
     *
     * @param context Game's context.
     * @param cave_Number Current cave number.
     * @param data Current game's data.
     */
    public void updateObjects(Context context, int cave_Number, Game_Data data){
        world.clearWorld();

        AdminSQLite admin = new AdminSQLite(context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT latitude, longitude, cave_number FROM GAME WHERE id = " +
                String.valueOf(data.getGame_ID()) + ";", null);

        int geo_id = 1;
        if (cursor.moveToFirst()) {
            do{
                //Only displays caves connected to the current one.
                if (data.getGraph().areConnected(cave_Number - 1, cursor.getInt(2) - 1)) {
                    GeoObject geo_Object = new GeoObject(geo_id);
                    geo_Object.setGeoPosition(Double.parseDouble(cursor.getString(0)), Double.parseDouble(cursor.getString(1)));
                    geo_Object.setName("Cueva: " + String.valueOf(cursor.getInt(2)));
                    geo_Object.setImageResource(cave_Image);
                    world.addBeyondarObject(geo_Object);
                    ++geo_id;
                }
            }while(cursor.moveToNext());
        }

        cursor.close();
    }

    /**
     *Creates the bat object and the cave where the bat chooses to take the player and disables everything else.
     * @param context App Context.
     * @param cave_Number Cave where the bat appears.
     * @param newCave Cave where the player is taken.
     * @param data Game data.
     */
    public void createBat(Context context, int cave_Number, int newCave, Game_Data data){
        world.clearWorld();

        AdminSQLite admin = new AdminSQLite(context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT latitude, longitude, cave_number FROM GAME WHERE id = " +
                String.valueOf(data.getGame_ID()) + " AND cave_number = " + String.valueOf(cave_Number) + ";", null);
        //Shows the bat in the camera.
        int geo_id = 1;
        if (cursor.moveToFirst()) {
            GeoObject geo_Object = new GeoObject(geo_id);
            geo_Object.setGeoPosition(Double.parseDouble(cursor.getString(0)), Double.parseDouble(cursor.getString(1)));
            geo_Object.setName("Murciélago");
            geo_Object.setImageResource(R.drawable.bat);
            world.addBeyondarObject(geo_Object);
            geo_id++;
        }

        cursor = db.rawQuery("SELECT latitude, longitude, cave_number FROM GAME WHERE id = " +
                String.valueOf(data.getGame_ID()) + " AND cave_number = " + String.valueOf(newCave) + ";", null);
        //Shows the cave in the camera.
        if (cursor.moveToFirst()) {
            GeoObject geo_Object = new GeoObject(geo_id);
            geo_Object.setGeoPosition(Double.parseDouble(cursor.getString(0)), Double.parseDouble(cursor.getString(1)));
            geo_Object.setName("Cueva: " + String.valueOf(cursor.getInt(2)));
            geo_Object.setImageResource(R.drawable.cave);
            world.addBeyondarObject(geo_Object);
        }
        cursor.close();
    }

    /**
     * Creates a cave object where the player has to go and disables everything else.
     * @param context App context.
     * @param newCave Cave where the player is taken to.
     * @param data Game data.
     */
    public void moveToCave(Context context, int newCave, Game_Data data){
        world.clearWorld();

        AdminSQLite admin = new AdminSQLite(context, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT latitude, longitude, cave_number FROM GAME WHERE id = " +
                String.valueOf(data.getGame_ID()) + " AND cave_number = " + String.valueOf(newCave) + ";", null);
        //Shows the cave with the bat above it in the camera.
        int geo_id = 1;
        if (cursor.moveToFirst()) {
            GeoObject geo_Object = new GeoObject(geo_id);
            geo_Object.setGeoPosition(Double.parseDouble(cursor.getString(0)), Double.parseDouble(cursor.getString(1)));
            geo_Object.setName("Cueva: " + String.valueOf(cursor.getInt(2)));
            geo_Object.setImageResource(R.drawable.cave_with_bat);
            world.addBeyondarObject(geo_Object);
        }
        cursor.close();
    }
}
