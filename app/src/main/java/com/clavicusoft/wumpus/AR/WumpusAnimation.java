package com.clavicusoft.wumpus.AR;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.clavicusoft.wumpus.R;

public class WumpusAnimation extends Activity {

    MediaPlayer mp;
    String visitedCaves;
    String visitedBatCaves;
    String usedArrows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wumpusanimation);

        //Gets info of game from previos activity.
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        visitedCaves = b.getString("visitedCaves");
        visitedBatCaves = b.getString("visitedBatCaves");
        usedArrows = b.getString("usedArrows");

        mp=MediaPlayer.create(this,R.raw.wumpussoundtrack);
        mp.start();
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(7400);   // set the duration of splash screen
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                finally {
                    Intent i = new Intent(WumpusAnimation.this, Splash_screen.class);
                    i.putExtra("visitedCaves", visitedCaves);
                    i.putExtra("visitedBatCaves", visitedBatCaves);
                    i.putExtra("usedArrows", usedArrows);

                    mp.release();
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(WumpusAnimation.this, R.anim.fade_in,
                            R.anim.fade_out);
                    startActivity(i,options.toBundle());
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
