package com.clavicusoft.wumpus.Select;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.clavicusoft.wumpus.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


/**
 * In order to run this test successfully,
 * you must grant the location permissions to the application,
 * you have to create a maze with 2 caves with the name "prueba"
 * and then you must modify in the code of the test the identifier of the saved maze, as indicated below.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MultiplayerGameSendingFromLibrary {

    @Rule
    public ActivityTestRule<IntroAnimation> mActivityTestRule = new ActivityTestRule<>(IntroAnimation.class);

    @Test
    public void multiplayerGameSendingFromLibrary() {
        ViewInteraction button9 = onView(
                allOf(withId(R.id.buttonMultijuador), withText("Multijugador"),
                        withParent(withId(R.id.linearLayout)),
                        isDisplayed()));
        button9.perform(click());

        ViewInteraction button10 = onView(
                allOf(withId(R.id.btOnBluetooth), withText("Enviar"), isDisplayed()));
        button10.perform(click());

        ViewInteraction textView = onView(
                //To run this test successfully change the numbers between '-' and '\n' to the current identifier of the maze.
                allOf(withId(R.id.text_view_item), withText("Nombre: prueba-1509550928\nNúmero de cuevas: 2"),
                        childAtPosition(
                                withId(R.id.listViewMazes),
                                0),
                        isDisplayed()));
        textView.perform(click());

        ViewInteraction button11 = onView(
                allOf(withId(R.id.scan), withText("Seleccione un dispositivo"), isDisplayed()));
        button11.perform(click());

        ViewInteraction button12 = onView(
                allOf(withId(R.id.button_scan),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        button12.check(matches(isDisplayed()));

        pressBack();

        ViewInteraction button13 = onView(
                allOf(withId(R.id.button_send), withText("Enviar laberinto"), isDisplayed()));
        button13.perform(click());

        ViewInteraction button14 = onView(
                allOf(withId(R.id.button_send),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                        0),
                                0),
                        isDisplayed()));
        button14.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
