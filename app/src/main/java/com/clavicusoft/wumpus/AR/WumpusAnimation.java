package com.clavicusoft.wumpus.AR;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.clavicusoft.wumpus.R;

public class WumpusAnimation extends Activity {

    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wumpusanimation);
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


                    Intent intent = new Intent(WumpusAnimation.this, Splash_screen.class);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(WumpusAnimation.this, R.anim.fade_in,
                            R.anim.fade_out);
                    startActivity(intent,options.toBundle());
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
