package com.clavicusoft.wumpus.Select;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.clavicusoft.wumpus.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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

/**
 * In order to run this test successfully,
 * you must grant the location permissions to the application,
 * you have to create a maze with 2 caves with the name "prueba"
 * and then you must modify in the code of the test the identifier of the saved maze, as indicated below.
 */

@RunWith(AndroidJUnit4.class)
public class IndividualGameSelectAndPlaceFromLibrary {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void selectAndPlaceFromLibrary() {
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button10 = onView(
                allOf(ViewMatchers.withId(R.id.Individual), withText("Individual"),
                        withParent(withId(R.id.linearLayout)),
                        isDisplayed()));
        button10.perform(click());

        ViewInteraction button11 = onView(
                allOf(withId(R.id.bttnChooseLib), withText("Biblioteca de laberintos"), isDisplayed()));
        button11.perform(click());

        ViewInteraction textView = onView(
                //To run this test successfully change the numbers between '-' and '\n' to the current identifier of the maze.
                allOf(withId(R.id.text_view_item), withText("Nombre: prueba-1508116860\nNúmero de cuevas: 2"),
                        childAtPosition(
                                withId(R.id.listViewMazes),
                                0),
                        isDisplayed()));
        textView.perform(click());

        ViewInteraction button13 = onView(
                allOf(withId(R.id.buttonMyLocation), withText("Agregar ubicación"), isDisplayed()));
        button13.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button14 = onView(
                allOf(withId(R.id.bcontinuar), withText("¡Continuar!"), isDisplayed()));
        button14.perform(click());

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
