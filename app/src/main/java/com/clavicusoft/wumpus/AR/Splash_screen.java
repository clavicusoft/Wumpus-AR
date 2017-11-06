package com.clavicusoft.wumpus.AR;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beyondar.android.util.location.BeyondarLocationManager;
import com.clavicusoft.wumpus.R;
import com.clavicusoft.wumpus.Select.MainActivity;

public class Splash_screen extends Activity {

    String visitedCaves;
    String visitedBatCaves;
    String usedArrows;

    /**Indicates the end of the game and show the score of the game
     * @param savedInstanceState Activity's previous saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Gets info of game from previos activity.
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        visitedCaves = b.getString("visitedCaves");
        visitedBatCaves = b.getString("visitedBatCaves");
        usedArrows = b.getString("usedArrows");

        showScore();
    }

    /**
     * Shows a score dialog and allow the user to restart or exit the game
     */
    public void showScore() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_gameover);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Sets an animation for the dialog
        TextView txtV1 = dialog.findViewById(R.id.txtViewNumVisitedC);
        TextView txtV2 = dialog.findViewById(R.id.txtViewNumVisitedBatC);
        TextView txtV3 = dialog.findViewById(R.id.txtViewNumUsedA);
        txtV1.setText(visitedCaves);    //Puts the scores on the fields
        txtV2.setText(visitedBatCaves);
        txtV3.setText(usedArrows);
        Button btn1 = dialog.findViewById(R.id.btnRestartGame);
        Button btn2 = dialog.findViewById(R.id.btnExitGame);
        btn1.setOnClickListener(new View.OnClickListener(){     //To restart the game
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),MainActivity.class);   //To return to the main activity
                ActivityOptions options = ActivityOptions.makeCustomAnimation(v.getContext(),R.anim.fade_out,R.anim.fade_out);
                startActivity(i, options.toBundle());
                dialog.dismiss();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){     //To exit the game
            @Override
            public void onClick(View v) {
                BeyondarLocationManager.disable();
                dialog.cancel();
                finishAffinity();
            }
        });
        dialog.show();

    }

}
