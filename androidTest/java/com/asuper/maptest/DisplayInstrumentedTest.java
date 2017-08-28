package com.asuper.maptest;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DisplayInstrumentedTest {

    @Rule
    public ActivityTestRule<MapsActivity> mMapsActivityActivityTestRule =
            new ActivityTestRule<MapsActivity>(MapsActivity.class);

    @Test
    public void clickLocationButton_updatesUI() throws Exception{
        onView(withId(R.id.mLocationButton))
                .perform(click());
        onView(withId(R.id.mDescriptionText))
                .check(matches(withText("scattered clouds")));
    }

}

