/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: TestHelpers.java
 * Description: Shared helpers for the Espresso UI tests: resetting the database and settings, adding test foods, building test products, and waiting for screens that update from the database.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Test helpers. Database calls here must not be made on the main thread;
 * JUnit test methods run on the instrumentation thread, so that's fine.
 */
public final class TestHelpers {

    /** How long to keep retrying a check before failing. */
    private static final long WAIT_TIMEOUT_MS = 5000;

    /** Pause between retries. */
    private static final long WAIT_STEP_MS = 100;

    /**
     * Private constructor so the class can't be instantiated.
     */
    private TestHelpers() {
    }

    /**
     * Gets the app context.
     *
     * @return the application context.
     */
    public static Context context() {
        return ApplicationProvider.getApplicationContext();
    }

    /**
     * Empties the food log and resets all settings to their defaults.
     */
    public static void resetAppData() {
        NutrimpactDatabase.getInstance(context()).clearAllTables();
        context().getSharedPreferences("nutrimpact_settings", Context.MODE_PRIVATE).edit().clear().commit();
    }

    /**
     * Gets today as days since 1970-01-01.
     *
     * @return today's epoch day.
     */
    public static long today() {
        return LocalDate.now().toEpochDay();
    }

    /**
     * Adds a food straight into the app's database.
     *
     * @param name  product name.
     * @param meal  meal.
     * @param day   epoch day.
     * @param grams portion in grams.
     * @param kcal  calories per 100g.
     * @return the saved entry's ID.
     */
    public static long addFood(String name, MealType meal, long day, double grams, double kcal) {
        FoodLogEntry entry = FoodLogDaoTest.entry(name, meal, day, System.currentTimeMillis(), grams, kcal, 10);
        return NutrimpactDatabase.getInstance(context()).foodLogDao().insert(entry);
    }

    /**
     * Builds a search-result product with nutrition data.
     *
     * @param name    product name.
     * @param brand   brand.
     * @param kcal    calories per 100g.
     * @param protein protein per 100g.
     * @param carbs   carbohydrates per 100g.
     * @param fat     fat per 100g.
     * @return the product.
     */
    public static FoodProduct product(String name, String brand, double kcal, double protein,
                                      double carbs, double fat) {
        Nutriments nutriments = new Nutriments();
        nutriments.setCaloriesPer100g(kcal);
        nutriments.setProteinPer100g(protein);
        nutriments.setCarbsPer100g(carbs);
        nutriments.setFatPer100g(fat);
        FoodProduct product = new FoodProduct();
        product.setProductName(name);
        product.setBrands(Arrays.asList(brand));
        product.setNutriments(nutriments);
        return product;
    }

    /**
     * Retries an Espresso check until it passes or five seconds go by. Used
     * where the screen updates from the database or network a moment later,
     * which Espresso doesn't wait for on its own.
     *
     * @param view      the view to check.
     * @param assertion the check.
     */
    public static void waitFor(ViewInteraction view, ViewAssertion assertion) {
        long deadline = SystemClock.uptimeMillis() + WAIT_TIMEOUT_MS;
        while (true) {
            try {
                view.check(assertion);
                return;
            } catch (AssertionError | RuntimeException e) {
                if (SystemClock.uptimeMillis() > deadline) {
                    throw e;
                }
                SystemClock.sleep(WAIT_STEP_MS);
            }
        }
    }

    /**
     * Retries a plain condition until it's true or five seconds go by.
     *
     * @param condition the condition.
     * @return true if it became true in time.
     */
    public static boolean waitUntil(java.util.function.BooleanSupplier condition) {
        long deadline = SystemClock.uptimeMillis() + WAIT_TIMEOUT_MS;
        while (SystemClock.uptimeMillis() <= deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            SystemClock.sleep(WAIT_STEP_MS);
        }
        return false;
    }
}
