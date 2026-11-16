/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: AddFoodScreenTest.java
 * Description: Espresso UI tests for the Add Food screen: portion preview, grams/ounces conversion, validation, and saving to the database.
 */

package com.example.nutrimpact;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.database.Cursor;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests AddFoodActivity opened for a 379 kcal/100g product.
 */
@RunWith(AndroidJUnit4.class)
public class AddFoodScreenTest {

    /** The running Add Food screen. */
    private ActivityScenario<AddFoodActivity> scenario;

    /**
     * Resets data and opens Add Food for a test product.
     */
    @Before
    public void setUp() {
        TestHelpers.resetAppData();
        FoodProduct oats = TestHelpers.product("Test Oats", "Quaker", 379, 8.9, 68, 6.4);
        Intent intent = AddFoodActivity.createIntent(TestHelpers.context(), oats, TestHelpers.today());
        scenario = ActivityScenario.launch(intent);
    }

    /**
     * Closes the screen.
     */
    @After
    public void tearDown() {
        scenario.close();
    }

    /**
     * Types an amount into the amount box.
     *
     * @param amount the amount text.
     */
    private void typeAmount(String amount) {
        onView(withId(R.id.editAmount)).perform(replaceText(amount));
        closeSoftKeyboard();
    }

    /**
     * The screen opens at 100 g with the per-100g values as the preview.
     */
    @Test
    public void opensAt100Grams() {
        onView(withId(R.id.editAmount)).check(matches(withText("100")));
        onView(withId(R.id.textPortionCalories)).check(matches(withText("379 kcal")));
    }

    /**
     * The preview scales to the amount typed.
     */
    @Test
    public void preview_scalesWithAmount() {
        typeAmount("40");
        onView(withId(R.id.textPortionCalories)).check(matches(withText("152 kcal")));
        onView(withId(R.id.textPortionMacros)).check(matches(withText("Protein 3.6g · Carbs 27.2g · Fat 2.6g")));
    }

    /**
     * Switching unit converts the amount so the portion stays the same.
     */
    @Test
    public void unitToggle_convertsAmount() {
        typeAmount("40");
        onView(withId(R.id.buttonOunces)).perform(click());
        onView(withId(R.id.editAmount)).check(matches(withText("1.4")));

        typeAmount("2");
        onView(withId(R.id.textPortionCalories)).check(matches(withText("215 kcal")));
        onView(withId(R.id.buttonGrams)).perform(click());
        onView(withId(R.id.editAmount)).check(matches(withText("56.7")));
    }

    /**
     * Zero, over 5 kg and an empty box can't be added.
     */
    @Test
    public void invalidAmounts_disableAdd() {
        typeAmount("0");
        onView(withId(R.id.buttonAddToLog)).check(matches(not(isEnabled())));
        onView(withText(R.string.add_food_error_amount)).check(matches(withText(R.string.add_food_error_amount)));

        typeAmount("6000");
        onView(withId(R.id.buttonAddToLog)).check(matches(not(isEnabled())));

        typeAmount("");
        onView(withId(R.id.buttonAddToLog)).check(matches(not(isEnabled())));
        onView(withId(R.id.textPortionMacros)).check(matches(withText(R.string.add_food_portion_empty)));

        typeAmount("5000");
        onView(withId(R.id.buttonAddToLog)).check(matches(isEnabled()));
    }

    /**
     * Adding saves the right portion, meal and day to the database.
     */
    @Test
    public void add_savesToDatabase() {
        typeAmount("40");
        onView(withId(R.id.chipLunch)).perform(scrollTo(), click());
        onView(withId(R.id.buttonAddToLog)).perform(click());

        assertTrue("Entry was not saved", TestHelpers.waitUntil(() -> countEntries() == 1));
        try (Cursor cursor = query("SELECT product_name, meal_type, portion_grams, log_date FROM food_log")) {
            assertTrue(cursor.moveToFirst());
            assertEquals("Test Oats", cursor.getString(0));
            assertEquals("LUNCH", cursor.getString(1));
            assertEquals(40.0, cursor.getDouble(2), 0.0001);
            assertEquals(TestHelpers.today(), cursor.getLong(3));
        }
    }

    /**
     * Counts saved entries.
     *
     * @return number of rows in the food log.
     */
    private static int countEntries() {
        try (Cursor cursor = query("SELECT COUNT(*) FROM food_log")) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /**
     * Runs a read-only SQL query on the app's database.
     *
     * @param sql the query.
     * @return the result cursor; the caller must close it.
     */
    private static Cursor query(String sql) {
        return NutrimpactDatabase.getInstance(TestHelpers.context()).query(new SimpleSQLiteQuery(sql));
    }
}
