/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: SettingsScreenTest.java
 * Description: Espresso UI tests for the Settings screen: default values, validation, reset, and saving goals and units.
 */

package com.example.nutrimpact;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests SettingsActivity and that its values are stored.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsScreenTest {

    /** The running settings screen. */
    private ActivityScenario<SettingsActivity> scenario;

    /**
     * Resets settings and opens the screen.
     */
    @Before
    public void setUp() {
        TestHelpers.resetAppData();
        scenario = ActivityScenario.launch(SettingsActivity.class);
    }

    /**
     * Closes the screen.
     */
    @After
    public void tearDown() {
        scenario.close();
    }

    /**
     * Types into a goal box.
     *
     * @param id    the box's EditText ID.
     * @param value the text.
     */
    private void type(int id, String value) {
        onView(withId(id)).perform(scrollTo(), replaceText(value));
        closeSoftKeyboard();
    }

    /**
     * The form starts with the UK Reference Intake defaults and the privacy note.
     */
    @Test
    public void showsDefaults() {
        onView(withId(R.id.editCalorieGoal)).check(matches(withText("2000")));
        onView(withId(R.id.editProteinGoal)).check(matches(withText("50")));
        onView(withId(R.id.editCarbsGoal)).check(matches(withText("260")));
        onView(withId(R.id.editFatGoal)).check(matches(withText("70")));
        onView(withText(R.string.settings_privacy_text)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Out-of-range goals show errors and nothing is saved.
     */
    @Test
    public void invalidGoals_areRejected() {
        type(R.id.editCalorieGoal, "100");
        type(R.id.editFatGoal, "0");
        onView(withId(R.id.buttonSaveSettings)).perform(click());

        onView(withText(R.string.settings_error_calories)).check(matches(isDisplayed()));
        onView(withText(R.string.settings_error_macro)).check(matches(isDisplayed()));
        assertEquals(2000, new UserSettings(TestHelpers.context()).getGoals().getCalories());
    }

    /**
     * Reset puts the defaults back and clears errors.
     */
    @Test
    public void reset_restoresDefaults() {
        type(R.id.editCalorieGoal, "100");
        onView(withId(R.id.buttonSaveSettings)).perform(click());
        onView(withId(R.id.buttonResetGoals)).perform(scrollTo(), click());

        onView(withId(R.id.editCalorieGoal)).check(matches(withText("2000")));
        onView(withText(R.string.settings_error_calories)).check(doesNotExist());
    }

    /**
     * Valid goals and units are saved.
     */
    @Test
    public void save_storesGoalsAndUnits() {
        type(R.id.editCalorieGoal, "1800");
        type(R.id.editProteinGoal, "120");
        onView(withId(R.id.buttonUnitKj)).perform(scrollTo(), click());
        onView(withId(R.id.buttonPortionOunces)).perform(scrollTo(), click());
        onView(withId(R.id.buttonSaveSettings)).perform(click());

        UserSettings settings = new UserSettings(TestHelpers.context());
        assertTrue("Settings were not saved", TestHelpers.waitUntil(() -> settings.getGoals().getCalories() == 1800));
        assertEquals(120, settings.getGoals().getProtein());
        assertEquals(260, settings.getGoals().getCarbs());
        assertEquals(EnergyUnit.KJ, settings.getEnergyUnit());
        assertTrue(settings.isOuncesDefault());
    }
}
