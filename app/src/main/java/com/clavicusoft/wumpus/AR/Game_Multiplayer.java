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
import com.clavicusoft.wumpus.Database.Firebase_Helper;
import com.clavicusoft.wumpus.Maze.CaveContent;
import com.clavicusoft.wumpus.R;
import com.clavicusoft.wumpus.Select.SelectPolyActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class Game_Multiplayer extends FragmentActivity implements OnClickBeyondarObjectListener {

    private BeyondarFragmentSupport currentBeyondARFragment;
    private AR_Helper worldHelper;
    private World world;
    private Game_Data data;
    private int game_ID;
    private int number_of_caves;
    private int numArrows;
    private TextView currentCave;
    private TextView arrowNumber;
    private ImageButton arrowButton;
    private Map<String, Integer> score;
    private Boolean arrowPressed;
    private Random random;
    private Firebase_Helper gameDataBase;

    /**
     * Sets the view once this activity starts.
     *
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        numArrows = 5;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_layout);
        currentCave = (TextView) findViewById(R.id.numCave); //current cave number textView
        arrowNumber = (TextView) findViewById(R.id.numArrow); //current arrow number textView
        random = new Random();

        arrowPressed = false;
        arrowButton = (ImageButton) findViewById(R.id.arrow_icon);
        arrowNumber.setText(String.valueOf(numArrows));
        arrowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(arrowPressed == false){
                    arrowPressed = true;
                    arrowButton.setBackgroundResource(R.drawable.arrow_icon_selected);
                }
                else
                {
                    arrowPressed = false;
                    arrowButton.setBackgroundResource(R.drawable.arrow_icon);
                }
            }
        });

        //Get the game parameters
        Bundle b = getIntent().getExtras();
        game_ID = b.getInt("game_ID");
        number_of_caves = b.getInt("number_of_caves");

        data = new Game_Data(this, game_ID, 1);
        data.setCurrentCave(data.chooseStartingCave(number_of_caves));
        currentCave.setText(String.valueOf(data.getCurrentCave()));

        //Sets the fragment.
        currentBeyondARFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
                R.id.beyondarFragment);

        worldHelper = new AR_Helper(this);
        worldHelper.updateObjects(this, data.getCurrentCave(), data);

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

        startDB(b);
    }

    @Override
    public void onStart () {
        super.onStart();
        showCurrentCave();
    }

    private void startDB(Bundle b)
    {
        gameDataBase = new Firebase_Helper(b.getString("room"), b.getString("username"), this);
        gameDataBase.changePlayerStatus("1"); //Player is alive in the DataBase as soon as app starts
        gameDataBase.changePlayerCave(String.valueOf(data.getCurrentCave())); //Update new cave on database
    }

    public void showCurrentCave() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("¡A cazar el Wumpus!");
        newDialog.setMessage("Te encuentras en la cueva " + data.getCurrentCave());
        newDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
                showHints(data.getCurrentCave());
            }
        });
        newDialog.show();
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
        if (arrowPressed) {
            numArrows--;
            arrowNumber.setText(String.valueOf(numArrows)); //update number of available arrows
            arrowPressed = false; //after shooting, put button back to normal
            arrowButton.setBackgroundResource(R.drawable.arrow_icon); //after shooting, put button back to normal
            shootArrow(cave_Number);
        }
        else {
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
        gameDataBase.changePlayerCave(String.valueOf(cave_Number));  //Update cave number in database
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
        CaveContent content = data.getCaveContent(cave_Number - 1);
        switch (content) {
            case WUMPUS:
                manageWumpus();
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

    /**
     * Verify if there are entities in the adjacent caves from a given cave and show hints to the user.
     * @param cave_Number The current cave where's the user.
     */
    private void showHints(int cave_Number) {
        CaveContent[] allCaves = this.data.getCaveContents().clone();
        ArraySet<CaveContent> adjacentHints = new ArraySet<>();
        for (int i = 0; i < this.number_of_caves; i++) {
            if (this.data.getGraph().areConnected(cave_Number - 1, i)) {
                adjacentHints.add(allCaves[i]);
            }
        }
        CaveContent randomHint = adjacentHints.valueAt(random.nextInt(adjacentHints.size()));

        this.batHint(adjacentHints,randomHint);
        this.pitHint(adjacentHints,randomHint);
        this.wumpusHint(adjacentHints,randomHint);
    }

    /**
     * If there is a Bat in an adjacent cave, plays an audio and show a message as a hint.
     * @param arraySet Entities that are in the adjacent caves.
     * @param caveContent A random entity, if this is equal to the entity case, the method will play
     *                    an audio.
     */
    private void batHint(ArraySet<CaveContent> arraySet, CaveContent caveContent) {
        if (arraySet.contains(CaveContent.BAT)) {
            Toast.makeText(this, "Acabas de percibir un chillido de murcielago.", Toast.LENGTH_LONG).show();
            if (caveContent == CaveContent.BAT) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.lotsofbats);
                mp.start();
                try {
                    Thread.sleep(3100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.release();
            }
        }

    }

    /**
     *  If there is a Pit in an adjacent cave, plays an audio and show a message as a hint.
     * @param arraySet Entities that are in the adjacent caves.
     * @param caveContent A random entity, if this is equal to the entity case, the method will play
     *                    an audio.
     */
    private void pitHint(ArraySet<CaveContent> arraySet, CaveContent caveContent) {
        if (arraySet.contains(CaveContent.PIT)) {
            Toast.makeText(this, "Acabas de percibir una brisa fría", Toast.LENGTH_LONG).show();
            if (caveContent == CaveContent.PIT) {
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
    }

    /**
     * If there is a Wumpus in an adjacent cave, plays an audio and show a message as a hint.
     * @param arraySet Entities that are in the adjacent caves.
     * @param caveContent A random entity, if this is equal to the entity case, the method will play
     *                    an audio.
     */
    private void wumpusHint(ArraySet<CaveContent> arraySet, CaveContent caveContent) {
        if (arraySet.contains(CaveContent.WUMPUS)) {
            Toast.makeText(this, "Acabas de percibir un olor repugnante a Wumpus", Toast.LENGTH_LONG).show();
            if (caveContent == CaveContent.WUMPUS) {
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

    /**
     * If the player falls in the Wumpus' cave then the game ends.
     */
    public void manageWumpus() {
        gameDataBase.changePlayerStatus("0"); //Update player dies in DB
        Intent i = new Intent(Game_Multiplayer.this, WumpusAnimation.class);
        i.putExtra("usedArrows", score.get("usedArrows").toString());
        i.putExtra("visitedBatCaves", score.get("visitedBatCaves").toString());
        i.putExtra("visitedCaves", score.get("visitedCaves").toString());
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                R.anim.fade_out);
        startActivity(i,options.toBundle());

    }

    /**
     * If the cave content is empty then it updates the game and the score.
     * @param cave_Number
     */
    public void manageEmptyCave (int cave_Number) {
        Toast.makeText(this, "Esta cueva esta vacia.", Toast.LENGTH_SHORT).show();
        score.put("visitedCaves",score.get("visitedCaves")+1);
        worldHelper.updateObjects(this, cave_Number, data);
    }

    /**
     * If the player falls in a cave where there's a bat, then the bat appears
     * and guides the player to the cave that he is taken to.
     * @param cave_Number Current cave where the player is located
     */
    public void generateBat(int cave_Number) {
        score.put("visitedBatCaves",score.get("visitedBatCaves")+1);
        final int newCave;
        final Context context = this;
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.pterodactyl); //Creates bat sound
        newCave = data.chooseRandomCave(cave_Number,number_of_caves);
        worldHelper.createBat(this, cave_Number, newCave, data); //Create the bat in front of you
        mp.start(); //Plays bat sound
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mp.release();
        //Instructions that the player must follow to continue the game
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Un murciélago ha aparecido");
        newDialog.setMessage("El murciélago te ha llevado a la cueva "
                + newCave + ". Para continuar debes desplazarte a esa cueva.");
        newDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //Sets the destination cave that the player must follow
                worldHelper.moveToCave(context, newCave, data);
                dialog.dismiss();
            }
        });
        newDialog.show();
    }

    /**
     * Manages if the player fall into a pit, plays a falling sound and allow the user to restart or exit the game
     */
    public void managePit () {
        gameDataBase.changePlayerStatus("0"); //Update player dies in DB
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.hombre_cayendo);
        mediaPlayer.start(); //Plays a falling sound
        showScore(mediaPlayer);
    }

    /**
     * Shows a score dialog and allow the user to restart or exit the game
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
                if (mediaPlayer != null){
                    mediaPlayer.release();
                }
                Intent i = new Intent(v.getContext(),SelectPolyActivity.class);   //To return to the main activity
                ActivityOptions options = ActivityOptions.makeCustomAnimation(v.getContext(),R.anim.fade_out,R.anim.fade_out);
                startActivity(i, options.toBundle());
                dialog.dismiss();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){     //To exit the game
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null){
                    mediaPlayer.release();
                }
                BeyondarLocationManager.disable();
                dialog.cancel();
                finishAffinity();
            }
        });
        dialog.show();

    }

    /**
     * Shoots an arrow to the respective cave.
     * @param cave Cave number.
     */
    public void shootArrow (int cave) {
        score.put("usedArrows",score.get("usedArrows")+1);
        int finalArrowCave = data.generateArrowCave(cave - 1);
        if (finalArrowCave + 1 == data.getCurrentCave()){
            manageArrowShot();
        }
        else {
            switch (data.getCaveContent(finalArrowCave)){
                case BAT:
                    Toast.makeText(this, "La flecha ha agitado a los murciélagos", Toast.LENGTH_LONG).show();
                    break;
                case PIT:
                    Toast.makeText(this, "La flecha ha desaparecido en la oscuridad de una cueva", Toast.LENGTH_LONG).show();
                    break;
                case WUMPUS:
                    manageKillWumpus();
                    break;
                case EMPTY:
                    gameDataBase.shootArrowMultiplayer(finalArrowCave+1);
                    Toast.makeText(this, "La flecha chocó en la pared de una cueva " + (finalArrowCave + 1), Toast.LENGTH_LONG).show();
                    break;
            }
        }
        if (numArrows == 0) {
            outOfArrows();
        }
    }

    /**
     * Manages if the player is hit by an arrow.
     */
    public void manageArrowShot(){
        gameDataBase.changePlayerStatus("0"); //Update player dies in DB
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.arrow_hit_blood);
        mediaPlayer.start();
        showScore(mediaPlayer);
    }

    /**
     * Manages if the arrow kills the Wumpus
     */
    public void manageKillWumpus(){
        gameDataBase.winGame();
        final MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.kill_wumpus);
        mediaPlayer.start();
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Has ganado");
        newDialog.setMessage("La flecha acabó con el Wumpus. Has liberado estas tierras de sus garras.");
        newDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                showScore(mediaPlayer);
                dialog.dismiss();
            }
        });
        newDialog.show();

    }

    public void outOfArrows(){
        gameDataBase.changePlayerStatus("0"); //Update player dies in DB
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Has perdido");
        newDialog.setMessage("Se te han acabado las flechas y no has logrado matar al Wumpus.");
        newDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                showScore(null);
                dialog.dismiss();
            }
        });
        newDialog.show();
    }

    public void changeArrowState(){
        if(arrowPressed == false){
            arrowPressed = true;
            arrowButton.setBackgroundResource(R.drawable.arrow_icon_selected);
        }
        else
        {
            arrowPressed = false;
            arrowButton.setBackgroundResource(R.drawable.arrow_icon);
        }
    }

    /**
     * Manages getting killed by another player's arrow.
     */
    public void manageGettingKilled() {
        final MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.kill_wumpus);
        mediaPlayer.start();
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Has sido eliminado");
        newDialog.setMessage("La flecha de otro jugador ha acabado contigo.");
        newDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                showScore(mediaPlayer);
                dialog.dismiss();
            }
        });
        newDialog.show();
    }

    public void manageKillPlayer(){
        Toast.makeText(this, "Has matado a un jugador.", Toast.LENGTH_LONG).show();
    }

    /**
     * Manages when another player has killed the wumpus.
     */
    public void finishGame () {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Ha finalizado el juego");
        newDialog.setMessage("La flecha de otro cazador acabó con el Wumpus.");
        newDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                showScore(null);
                dialog.dismiss();
            }
        });
        newDialog.show();
    }
}