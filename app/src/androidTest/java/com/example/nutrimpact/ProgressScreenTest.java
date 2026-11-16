/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: ProgressScreenTest.java
 * Description: Espresso UI tests for the Progress screen: weekly averages, days within goal, and an empty previous week.
 */

package com.example.nutrimpact;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests ProgressActivity with two logged days this week.
 */
@RunWith(AndroidJUnit4.class)
public class ProgressScreenTest {

    /** The running progress screen. */
    private ActivityScenario<ProgressActivity> scenario;

    /**
     * Logs 2,200 kcal today (over the 2,000 goal) and 1,800 kcal yesterday (within it).
     */
    @Before
    public void setUp() {
        TestHelpers.resetAppData();
        TestHelpers.addFood("Today food", MealType.DINNER, TestHelpers.today(), 1000, 220);
        TestHelpers.addFood("Yesterday food", MealType.LUNCH, TestHelpers.today() - 1, 1000, 180);
        scenario = ActivityScenario.launch(ProgressActivity.class);
    }

    /**
     * Closes the screen.
     */
    @After
    public void tearDown() {
        scenario.close();
    }

    /**
     * The summary averages logged days and counts days within the goal.
     */
    @Test
    public void summary_showsAveragesAndGoalDays() {
        TestHelpers.waitFor(onView(withId(R.id.textAverage)), matches(withText("2,000 kcal a day on average")));
        onView(withId(R.id.textDaysLogged)).check(matches(withText("Based on 2 of 7 days logged")));
        onView(withId(R.id.textWithinGoal)).check(matches(withText("Within your goal on 1 of 2 days")));
        onView(withId(R.id.chartWeek)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonNextWeek)).check(matches(not(isEnabled())));
    }

    /**
     * The previous week has nothing logged, so the summary is hidden and a message shown.
     */
    @Test
    public void previousWeek_isEmpty() {
        TestHelpers.waitFor(onView(withId(R.id.textAverage)), matches(isDisplayed()));
        onView(withId(R.id.buttonPreviousWeek)).perform(click());

        TestHelpers.waitFor(onView(withId(R.id.textProgressEmpty)), matches(isDisplayed()));
        onView(withId(R.id.cardSummary)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.buttonNextWeek)).check(matches(isEnabled()));
    }
}
