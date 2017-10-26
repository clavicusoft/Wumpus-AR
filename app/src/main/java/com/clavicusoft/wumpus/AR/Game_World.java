package com.clavicusoft.wumpus.AR;


import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;
import com.clavicusoft.wumpus.Maze.Cave;
import com.clavicusoft.wumpus.Maze.CaveContent;
import com.clavicusoft.wumpus.R;

import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Random;

public class Game_World extends FragmentActivity implements OnClickBeyondarObjectListener {

    private BeyondarFragmentSupport currentBeyondARFragment;
    private AR_Helper worldHelper;
    private World world;
    private Game_Data data;
    private int game_ID;
    private int number_of_caves;
    private TextView currentCave;

    /**
     * Sets the view once this activity starts.
     *
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_layout);
        currentCave = (TextView) findViewById(R.id.numCave); //current cave number textView

        //Get the game parameters
        Bundle b = getIntent().getExtras();
        game_ID = b.getInt("game_ID");
        number_of_caves = b.getInt("number_of_caves");

        data = new Game_Data(this, game_ID, 1);

        //Sets the fragment.
        currentBeyondARFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
                R.id.beyondarFragment);

        worldHelper = new AR_Helper(this);
        worldHelper.updateObjects(this, 1, data);

        //Allows BeyondAR to access user's position
        BeyondarLocationManager.setLocationManager((LocationManager) this.getSystemService(
                Context.LOCATION_SERVICE));

        //Starts the world
        world = worldHelper.getWorld();

        setDistanceParameters();
        currentBeyondARFragment.setWorld(world);

        setLocationParameters();

        //Assign onClick listener
        currentBeyondARFragment.setOnClickBeyondarObjectListener(this);

        showHints(1);
    }

    /**
     * Enables the GPS once the game resumes.
     */
    @Override
    protected void onResume(){
        // Enable GPS
        super.onResume();
        BeyondarLocationManager.enable();
    }

    /**
     * Disables the GPS once the game pauses.
     */
    @Override
    protected void onPause(){
        // Disable GPS
        super.onPause();
        BeyondarLocationManager.disable();
    }

    /**
     * Displays alert message when the user click a GeoObject.
     * @param arrayList List of the GeoObjects.
     */
    @Override
    public void onClickBeyondarObject(ArrayList<BeyondarObject> arrayList) {
        // The first element in the array belongs to the closest BeyondarObject
        final int cave_Number = getCaveNumberFromName(arrayList.get(0).getName());
        double distance = data.checkDistance(world.getLatitude(), world.getLongitude(), cave_Number);
        if (distance <= 10) {
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("Has encontrado " + arrayList.get(0).getName());
            newDialog.setMessage("¿Desea entrar a esta cueva?");
            newDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                    updateGame(cave_Number);
                }
            });
            newDialog.setNegativeButton("No", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }
            });
            newDialog.show();
        }
        else {
            Toast.makeText(this,"Debes acercarte a la cueva para poder entrar en ella. Estás a " + String.valueOf(distance) + " metros de ella." ,Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Sets the distance parameters for BeyondAR
     */
    public void setDistanceParameters () {
        //Set the distance (in meters) which the objects will be considered to render.
        currentBeyondARFragment.setMaxDistanceToRender(3000);
        // Set the distance factor for rendering all the objects. As bigger the factor the
        // closer the objects
        currentBeyondARFragment.setDistanceFactor(5);
        /*
         * When a GeoObject is rendered
         * according to its position it could look very big if it is too close. Use
         * this method to render near objects as if there were farther.
         * For instance if there is an object at 1 meters and we want to have
         * everything at to look like if they where at least at 10 meters, we could
         * use this method for that purpose.
         */
        currentBeyondARFragment.setPushAwayDistance(4);
        /*
         * When a GeoObject is rendered
         * according to its position it could look very small if it is far away. Use
         * this method to render far objects as if there were closer.
         * For instance if there are objects farther than 50 meters and we want them
         * to be displayed as they where at 50 meters, we could use this method for
         * that purpose.
         */
        currentBeyondARFragment.setPullCloserDistance(0);
    }

    /**
     * Sets the location parameters for BeyondAR.
     */
    public void setLocationParameters () {
        //Dynamic position for the world
        BeyondarLocationManager.enable();

        //Allow BeyondAR to update the world position.
        BeyondarLocationManager.addWorldLocationUpdate(world);
        BeyondarLocationManager.setLocationManager((LocationManager) getSystemService(
                Context.LOCATION_SERVICE));
    }

    /**
     * Updates the game board and world.
     *
     * @param cave_Number Current cave number.
     */
    public void updateGame (int cave_Number) {
        currentCave.setText(String.valueOf(cave_Number));
        checkCaveContent(cave_Number);
    }

    /**
     * Gets the cave number from the BeyondAR Object's name.
     *
     * @param name Object's name.
     * @return Int: cave number.
     */
    public int getCaveNumberFromName (String name) {
        return Integer.parseInt(name.substring(name.length() - 1));
    }

    /**
     * Checks the content of the cave.
     *
     * @param cave_Number Current cave number.
     */
    public void checkCaveContent (int cave_Number){
        Toast toast;
        AlertDialog.Builder newDialog;
        CaveContent content = data.getCaveContent(cave_Number);
        switch (content) {
            case WUMPUS:
                toast = Toast.makeText(this, "Has caido en la cueva del Wumpus.", Toast.LENGTH_SHORT);
                toast.show();
                worldHelper.updateObjects(this, cave_Number, data);
                break;
            case BAT:
                toast = Toast.makeText(this, "Has caido en la cueva de un murcielago.", Toast.LENGTH_SHORT);
                toast.show();
                final int newCave;
                final Context context = this;
                newCave = data.chooseRandomCave(cave_Number,number_of_caves);
                worldHelper.createBat(this, cave_Number, newCave, data);
                newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("Un murciélago salvaje ha aparecido");
                newDialog.setMessage("El murciélago te ha llevado a la cueva "
                + newCave + ". Para continuar debes desplazarte a esa cueva.");
                newDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                        worldHelper.moveToCave(context, newCave, data);
                    }
                });
                newDialog.show();
                break;
            case PIT:
                MediaPlayer mediaPlayer;
                toast = Toast.makeText(this, "Has caido en un pozo.", Toast.LENGTH_SHORT);
                toast.show();
                mediaPlayer = MediaPlayer.create(this, R.raw.hombre_cayendo);
                mediaPlayer.start();
                //stop();           //Stop the current game
                //data.showScore();       //Show the game score
                newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("Ha caído en un pozo.");
                newDialog.setMessage("Está fuera del juego. ¿Desea volver a jugar?");
                newDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        //Restart the game
                    }
                });
                newDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        //Close app
                        dialog.dismiss();
                    }
                });
                newDialog.show();
                break;
            case EMPTY:
                this.showHints(cave_Number);
                worldHelper.updateObjects(this, cave_Number, data);
                break;
        }
    }

    private void showHints(int cave_Number) {
        CaveContent[] allCaves = this.data.getCaveContents();
        ArraySet<CaveContent> adjacentHints = new ArraySet<>();

        for (int i = 0 ; i< allCaves.length ;i++) {
            if(this.data.getGraph().areConnected(cave_Number,i)) {
                adjacentHints.add(allCaves[i]);
            }
        }

        if(adjacentHints.contains(CaveContent.BAT)) {
            Toast.makeText(this, "Acabas de percibir un chillido de murcielago.", Toast.LENGTH_SHORT).show();
        }

        if(adjacentHints.contains(CaveContent.PIT)) {
            Toast.makeText(this, "Acabas de percibir una brisa fría", Toast.LENGTH_SHORT).show();
        }

        if(adjacentHints.contains(CaveContent.WUMPUS)) {
            Toast.makeText(this, "Acabas de percibir un olor repugnante a Wumpus", Toast.LENGTH_SHORT).show();
        }
    }
}