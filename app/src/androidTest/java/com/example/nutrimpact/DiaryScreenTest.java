/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DiaryScreenTest.java
 * Description: Espresso UI tests for the diary home screen: empty state, totals against goals, going over a goal, swipe-to-delete with Undo, and switching days.
 */

package com.example.nutrimpact;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests DiaryActivity with foods added directly to the database.
 */
@RunWith(AndroidJUnit4.class)
public class DiaryScreenTest {

    /** The running diary screen. */
    private ActivityScenario<DiaryActivity> scenario;

    /**
     * Starts every test with an empty log and default settings.
     */
    @Before
    public void setUp() {
        TestHelpers.resetAppData();
    }

    /**
     * Closes the screen.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Adds 40 g of oats (breakfast) and 30 g of bran (snacks) today: 151.6 + 45 = 196.6 kcal.
     */
    private void addTwoFoodsToday() {
        TestHelpers.addFood("Oats", MealType.BREAKFAST, TestHelpers.today(), 40, 379);
        TestHelpers.addFood("Bran", MealType.SNACKS, TestHelpers.today(), 30, 150);
    }

    /**
     * An empty day shows the empty message, 0 kcal and the full default goal.
     */
    @Test
    public void emptyDay_showsEmptyState() {
        scenario = ActivityScenario.launch(DiaryActivity.class);

        TestHelpers.waitFor(onView(withId(R.id.textDiaryEmpty)), matches(isDisplayed()));
        onView(withId(R.id.textTotalCalories)).check(matches(withText("0 kcal")));
        onView(withId(R.id.textCalorieGoal)).check(matches(withText("of 2,000 kcal · 2,000 kcal left")));
        onView(withId(R.id.textDiaryDay)).check(matches(withText(R.string.diary_today)));
        onView(withId(R.id.buttonNextDay)).check(matches(not(isEnabled())));
    }

    /**
     * Foods are grouped under meals and the totals and goal line add up.
     */
    @Test
    public void loggedFoods_showTotalsAndMeals() {
        addTwoFoodsToday();
        scenario = ActivityScenario.launch(DiaryActivity.class);

        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));
        onView(withId(R.id.textCalorieGoal)).check(matches(withText("of 2,000 kcal · 1,803 kcal left")));
        onView(withText("Breakfast")).check(matches(isDisplayed()));
        onView(withText("Snacks")).check(matches(isDisplayed()));
        onView(withText("Oats")).check(matches(isDisplayed()));
        onView(withText("Bran")).check(matches(isDisplayed()));
    }

    /**
     * Going over the calorie goal says how much over.
     */
    @Test
    public void overGoal_showsAmountOver() {
        TestHelpers.addFood("Big bowl", MealType.DINNER, TestHelpers.today(), 600, 379); // 2,274 kcal
        scenario = ActivityScenario.launch(DiaryActivity.class);

        TestHelpers.waitFor(onView(withId(R.id.textCalorieGoal)),
                matches(withText("of 2,000 kcal · 274 kcal over")));
    }

    /**
     * Swiping a food removes it and updates the total; Undo brings it back.
     */
    @Test
    public void swipeToDelete_thenUndo() {
        addTwoFoodsToday();
        scenario = ActivityScenario.launch(DiaryActivity.class);
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));

        onView(withId(R.id.recyclerDiary)).perform(actionOnItem(hasDescendant(withText("Bran")), swipeLeft()));
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("152 kcal")));
        onView(withText("Bran")).check(doesNotExist());
        onView(withText("Snacks")).check(doesNotExist());

        onView(withText(R.string.diary_undo)).perform(click());
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));
        onView(withText("Bran")).check(matches(isDisplayed()));
    }

    /**
     * A food can be swiped away again after being restored with Undo.
     */
    @Test
    public void swipeAgainAfterUndo() {
        addTwoFoodsToday();
        scenario = ActivityScenario.launch(DiaryActivity.class);
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));

        for (int i = 0; i < 3; i++) {
            onView(withId(R.id.recyclerDiary)).perform(actionOnItem(hasDescendant(withText("Oats")), swipeLeft()));
            TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("45 kcal")));
            onView(withText(R.string.diary_undo)).perform(click());
            TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));
        }
    }

    /**
     * Meal headings can't be swiped away.
     */
    @Test
    public void headings_cantBeSwiped() {
        addTwoFoodsToday();
        scenario = ActivityScenario.launch(DiaryActivity.class);
        TestHelpers.waitFor(onView(withText("Breakfast")), matches(isDisplayed()));

        onView(withId(R.id.recyclerDiary)).perform(actionOnItem(hasDescendant(withText("Breakfast")), swipeLeft()));

        onView(withText("Breakfast")).check(matches(isDisplayed()));
        onView(withId(R.id.textTotalCalories)).check(matches(withText("197 kcal")));
    }

    /**
     * The arrows move between days, showing each day's own foods.
     */
    @Test
    public void dayArrows_switchDays() {
        addTwoFoodsToday();
        TestHelpers.addFood("Yesterday soup", MealType.LUNCH, TestHelpers.today() - 1, 100, 50);
        scenario = ActivityScenario.launch(DiaryActivity.class);
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));

        onView(withId(R.id.buttonPreviousDay)).perform(click());
        onView(withId(R.id.textDiaryDay)).check(matches(withText(R.string.diary_yesterday)));
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("50 kcal")));
        onView(withText("Yesterday soup")).check(matches(isDisplayed()));
        onView(withId(R.id.buttonNextDay)).check(matches(isEnabled()));

        onView(withId(R.id.buttonNextDay)).perform(click());
        onView(withId(R.id.textDiaryDay)).check(matches(withText(R.string.diary_today)));
        TestHelpers.waitFor(onView(withId(R.id.textTotalCalories)), matches(withText("197 kcal")));
    }
}
