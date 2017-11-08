package com.clavicusoft.wumpus.Select;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.clavicusoft.wumpus.R;


public class IntroAnimation extends Activity {

    MediaPlayer mp;

    /**
     *Show the introduction animation and call the main activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introanimation);
        //mp=MediaPlayer.create(this,R.raw.introsoundtrack);
        //mp.start();
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(7500);   // set the duration of splash screen
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                finally {
                    Intent intent = new Intent(IntroAnimation.this, SelectPolyActivity.class);
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(IntroAnimation.this, R.anim.fade_in,
                            R.anim.fade_out);
                    startActivity(intent,options.toBundle());
                }
            }
        };
        timer.start();
    }

    /**
     *Pause the activity when the user is not inside in this activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
