package com.clavicusoft.wumpus.AR;


import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.clavicusoft.wumpus.Select.MainActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class Game_World extends FragmentActivity implements OnClickBeyondarObjectListener {

    private BeyondarFragmentSupport currentBeyondARFragment;
    private AR_Helper worldHelper;
    private World world;
    private Game_Data data;
    private int game_ID;
    private int number_of_caves;
    private TextView currentCave;
    private Map<String, Integer> score;

    private Random random;

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
        random = new Random();

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

        score = new HashMap<>();
        score.put("visitedCaves",0);
        score.put("visitedBatCaves",0);
        score.put("usedArrows",0);


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
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        if (distance <= 10) {
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
        worldHelper.updateObjects(this, cave_Number, data);
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
        CaveContent content = data.getCaveContent(cave_Number - 1);
        switch (content) {
            case WUMPUS:
                break;
            case BAT:
                generateBat(cave_Number);
                break;
            case PIT:
                managePit();
                break;
            case EMPTY:
                manageEmptyCave(cave_Number);
                this.showHints(cave_Number);
                break;
        }
    }


    private void showHints(int cave_Number) {
        CaveContent[] allCaves = this.data.getCaveContents().clone();
        ArraySet<CaveContent> adjacentHints = new ArraySet<>();
        for (int i = 0; i < this.number_of_caves; i++) {
            if (this.data.getGraph().areConnected(cave_Number - 1, i)) {
                adjacentHints.add(allCaves[i]);
            }
        }
        CaveContent randomHint = adjacentHints.valueAt(random.nextInt(adjacentHints.size()));
        if (adjacentHints.contains(CaveContent.BAT)) {
            Toast.makeText(this, "Acabas de percibir un chillido de murcielago.", Toast.LENGTH_LONG).show();
            if (randomHint == CaveContent.BAT) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.pterodactyl);
                mp.start();
                try {
                    Thread.sleep(3100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.release();
            }
        }

        if (adjacentHints.contains(CaveContent.PIT)) {
            Toast.makeText(this, "Acabas de percibir una brisa fría", Toast.LENGTH_LONG).show();
            if (randomHint == CaveContent.PIT) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.waterdrop);
                mp.start();
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(3100);
                try {
                    Thread.sleep(3100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.release();
            }
        }

        if (adjacentHints.contains(CaveContent.WUMPUS)) {
            Toast.makeText(this, "Acabas de percibir un olor repugnante a Wumpus", Toast.LENGTH_LONG).show();
            if (randomHint == CaveContent.WUMPUS) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.wumpushint);
                mp.start();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.release();
            }
        }
    }


    public void manageEmptyCave (int cave_Number) {
        Toast.makeText(this, "Esta cueva esta vacia.", Toast.LENGTH_SHORT).show();
        score.put("visitedCaves",score.get("visitedCaves")+1);
        worldHelper.updateObjects(this, cave_Number, data);
    }

    public void generateBat(int cave_Number) {
        score.put("visitedBatCaves",score.get("visitedBatCaves")+1);
        final int newCave;
        final Context context = this;
        newCave = data.chooseRandomCave(cave_Number,number_of_caves);
        worldHelper.createBat(this, cave_Number, newCave, data);
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Un murciélago salvaje ha aparecido");
        newDialog.setMessage("El murciélago te ha llevado a la cueva "
                + newCave + ". Para continuar debes desplazarte a esa cueva.");
        newDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                worldHelper.moveToCave(context, newCave, data);
                dialog.dismiss();
            }
        });
        newDialog.show();
    }

    /**
     * Manages if the player fall into a pit
     */
    public void managePit () {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.hombre_cayendo);
        mediaPlayer.start(); //Plays a falling sound
        showScore(mediaPlayer);
    }

    /**
     * Shows a score dialog
     * @param mediaPlayer MediaPlayer to release
     */
    public void showScore(final MediaPlayer mediaPlayer) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_gameover);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Sets an animation for the dialog
        TextView txtV1 = dialog.findViewById(R.id.txtViewNumVisitedC);
        TextView txtV2 = dialog.findViewById(R.id.txtViewNumVisitedBatC);
        TextView txtV3 = dialog.findViewById(R.id.txtViewNumUsedA);
        txtV1.setText(score.get("visitedCaves").toString());    //Puts the scores on the fields
        txtV2.setText(score.get("visitedBatCaves").toString());
        txtV3.setText(score.get("usedArrows").toString());
        Button btn1 = dialog.findViewById(R.id.btnRestartGame);
        Button btn2 = dialog.findViewById(R.id.btnExitGame);
        btn1.setOnClickListener(new View.OnClickListener(){     //To restart the game
            @Override
            public void onClick(View v) {
                mediaPlayer.release();
                Intent i = new Intent(v.getContext(),MainActivity.class);   //To return to the main activity
                ActivityOptions options = ActivityOptions.makeCustomAnimation(v.getContext(),R.anim.fade_out,R.anim.fade_out);
                startActivity(i, options.toBundle());
                dialog.dismiss();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){     //To exit the game
            @Override
            public void onClick(View v) {
                mediaPlayer.release();
                BeyondarLocationManager.disable();
                dialog.cancel();
                finishAffinity();
            }
        });
        dialog.show();

    }
}
