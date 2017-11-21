package com.clavicusoft.wumpus.Select;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.clavicusoft.wumpus.Bluetooth.BluetoothChat;
import com.clavicusoft.wumpus.Database.AdminSQLite;
import com.clavicusoft.wumpus.Draw.DrawMazeActivity;
import com.clavicusoft.wumpus.Map.Coordinates;
import com.clavicusoft.wumpus.R;

import java.util.ArrayList;
import java.util.List;

public class SelectPolyActivity extends Activity {

    ViewPager viewPager;
    CustomSwip  customSwip;
    int currentPage;
    AlertDialog.Builder alert; //Alert
    int whichActivity=0; //start game, draw, library, multiplayer

    /**
     * Requests the number of permissions pending, if none are pending returns true
     * @return False if there are still pending permissions and true if there are none
     */
    private boolean checkAndRequestPermissions() { //requests the number of permissions pending: 2, 1 or none.
        int permissionCAMERA = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA); //camera permissions
        int locationPermission = ContextCompat.checkSelfPermission(this,

                Manifest.permission.ACCESS_FINE_LOCATION); //ubication permissions

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) { //adds permissions to list
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCAMERA != PackageManager.PERMISSION_GRANTED) { //adds permissions to list
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) { //if there are permissions to be requested, it does
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }

    /**
     * Sets the view once this activity starts. Fills the slider with the images.
     *
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        alert = new AlertDialog.Builder(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_poly);
        currentPage = 1;
        viewPager=(ViewPager)findViewById(R.id.ImageSlider);
        int[] imageResources = {R.drawable.tetra_light, R.drawable.octa_light,
                R.drawable.cube_light, R.drawable.icosa_light, R.drawable.dodeca_light};
        customSwip = new CustomSwip(this,imageResources);
        viewPager.setAdapter(customSwip);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position + 1;
            }
        });
        checkAndRequestPermissions();
    }

    /**
     * Requests the user to accept permissions for camera and ubication services if they
     * were not previously accepted on installation.
     *
     * @param requestCode Application specific request code to match with a result
     *                    reported to onRequestPermissionsResult(int, String[], int[])
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if(grantResults.length == 1 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) { //if only one permission was pending it must start the activity
                //Permission accepted
                if(whichActivity == 1) { //start activity
                    this.imageClicked(currentPage);
                }
                if(whichActivity == 2){ //draw activity
                    Intent i = new Intent(this, DrawMazeActivity.class);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                            R.anim.fade_out);
                    startActivity(i, options.toBundle());
                }
                if(whichActivity == 3) { //library activity
                    Intent i = new Intent(this, SelectFromLibActivity.class);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                            R.anim.fade_out);
                    startActivity(i, options.toBundle());
                }
                if(whichActivity == 4){ //multiplayer activity
                    Intent i = new Intent(this, BluetoothChat.class);
                    i.putExtra("funcion", "inicio");
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                            R.anim.fade_out);
                    startActivity(i, options.toBundle());
                }
            }
            else if(grantResults.length == 2 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                //Permission accepted
                //if both permissions are granted, does nothing since checkAndResquestPermissions returns true and button opens activity
            }
            else {
                //Permissions denied
                alert.setTitle("Error");
                alert.setMessage("Para poder continuar con el juego debe permitir a Wumpus acceder a la cámara y a su ubicación");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        }
    }

    /**
     * Retrieves the DB information about the selected graph and sends it to the Coordinates
     * activity.
     *
     * @param graph The graph's position inside the slider.
     */
    public void imageClicked(int graph) {
        //Starts the DB
        AdminSQLite admin = new AdminSQLite(this, "WumpusDB", null, 7);
        SQLiteDatabase db = admin.getWritableDatabase();

        //Gets the name of the selected maze
        String graphName = "";
        switch (graph) {
            case 1:
                graphName = "Tetrahedron";
                break;
            case 2:
                graphName = "Octahedron";
                break;
            case 3:
                graphName = "Cube";
                break;
            case 4:
                graphName = "Icosahedron";
                break;
            case 5:
                graphName = "Dodecahedron";
                break;
        }

        //Gets the DB information the selected maze.
        Cursor cell = db.rawQuery("SELECT GRAPH.id FROM GRAPH WHERE GRAPH.name = \"" + graphName +
                "\";", null);
        if (cell.moveToFirst()){
            int graphID = cell.getInt(0);
            cell.close();
            String stringGraphID = Integer.toString(graphID);

            //Sends the information about the maze to the Coordinates activity.
            Intent i = new Intent(this, Coordinates.class);
            i.putExtra("graphID",stringGraphID);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                    R.anim.fade_out);
            startActivity(i, options.toBundle());
        }
        else {
            Toast.makeText(this, "El Wumpus no se encuentra en estas cuevas, intenta otra.",
                    Toast.LENGTH_LONG).show();
            db.close();
        }
        cell.close();
    }

    /**
     * Starts the draw maze activity, and sets the animation for the transition.
     *
     * @param view Current view.
     */
    public void drawLabyrinthView(View view)
    {
        whichActivity =2;
        if(checkAndRequestPermissions()) {
            Intent i = new Intent(this, DrawMazeActivity.class);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                    R.anim.fade_out);
            startActivity(i, options.toBundle());
        }
    }

    /**
     * Starts the multiplayer activity, and sets the animation for the transition.
     *
     * @param view Current view.
     */
    public void multiplayerView(View view)
    {
        whichActivity =4;
        if(checkAndRequestPermissions()) {
            Intent i = new Intent(this, BluetoothChat.class);
            i.putExtra("funcion", "inicio");
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                    R.anim.fade_out);
            startActivity(i, options.toBundle());
        }
    }

    /**
     * Starts the select from library activity, and sets the animation for the transition.
     *
     * @param view Current view.
     */
    public void selectFromLibView(View view)
    {
        whichActivity =3;
        if(checkAndRequestPermissions()) {
            Intent i = new Intent(this, SelectFromLibActivity.class);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in,
                    R.anim.fade_out);
            startActivity(i, options.toBundle());
        }
    }

    /**
     * Sets the animation for the onBackPressed function.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    /**
     * Gets the position of the slider and sends it to the imageClicked function.
     *
     * @param view Current view.
     */
    public void startGame(View view) {
        whichActivity =1;
        if(checkAndRequestPermissions()){
            this.imageClicked(currentPage);
        }

    }

}
