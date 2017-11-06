package com.clavicusoft.wumpus.Select;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.clavicusoft.wumpus.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MultiplayerGameReceiveMaze {

    @Rule
    public ActivityTestRule<IntroAnimation> mActivityTestRule = new ActivityTestRule<>(IntroAnimation.class);

    @Test
    public void multiplayerGameReceiveMaze() {
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction button = onView(
                allOf(withId(R.id.buttonMultijuador), withText("Multijugador"),
                        withParent(withId(R.id.linearLayout)),
                        isDisplayed()));
        button.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btLabs), withText("Compartir Laberintos"), isDisplayed()));
        button2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.btReceive), withText("Recibir"), isDisplayed()));
        button3.perform(click());

    }

}
