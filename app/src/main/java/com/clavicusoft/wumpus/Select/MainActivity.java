package com.clavicusoft.wumpus.Select;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.clavicusoft.wumpus.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    AlertDialog.Builder alert; //Alert
    int whichActivity=0; //1 Single, 2 Multiplayer

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
     * Sets the view once this activity starts.
     *
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        alert = new AlertDialog.Builder(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                if(whichActivity == 1) { //singleplayer activity
                    Intent i = new Intent(this, SelectPolyActivity.class);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_down,
                            R.anim.slide_out_down);
                    startActivity(i, options.toBundle());
                }
                if(whichActivity == 2){ //multiplayer activity
                    Intent i = new Intent(this, SelectPolyActivity.class);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_down,
                            R.anim.slide_out_down);
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
     * Starts the single player activity, and sets the animation for the transition.
     *
     * @param view Current view.
     */
    public void singlePlayer (View view)
    {
        whichActivity = 1; //Singleplayer
        //---This block of code ensures camera and location permissions are granted before launching anything else
        if(checkAndRequestPermissions()) {
            Intent i = new Intent(this, SelectPolyActivity.class);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_down,
                    R.anim.slide_out_down);
            startActivity(i, options.toBundle());
        }
    }

    /**
     * Close application.
     */
    @Override
    public void onBackPressed() {
        finish();
    }
}
