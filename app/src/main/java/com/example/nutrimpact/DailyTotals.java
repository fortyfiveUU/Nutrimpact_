/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DailyTotals.java
 * Description: Adds up the calories, protein, carbohydrates and fat of a list of logged foods, e.g. for a whole day or a single meal.
 */

package com.example.nutrimpact;

import java.util.List;

/**
 * Immutable totals for a set of food log entries. Has no Android
 * dependencies, so it can be unit tested on the computer.
 */
public final class DailyTotals {

    /** Total calories in kcal. */
    private final double calories;

    /** Total protein in grams. */
    private final double protein;

    /** Total carbohydrates in grams. */
    private final double carbs;

    /** Total fat in grams. */
    private final double fat;

    /**
     * Creates a set of totals. Use {@link #of(List)} to calculate them.
     *
     * @param calories total calories in kcal.
     * @param protein  total protein in grams.
     * @param carbs    total carbohydrates in grams.
     * @param fat      total fat in grams.
     */
    private DailyTotals(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    /**
     * Adds up the portion nutrition of every entry in a list.
     *
     * @param entries the logged foods; null or empty gives all zeros.
     * @return the totals.
     */
    public static DailyTotals of(List<FoodLogEntry> entries) {
        double calories = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;
        if (entries != null) {
            for (FoodLogEntry entry : entries) {
                if (entry == null) {
                    continue;
                }
                calories += entry.getPortionCalories();
                protein += entry.getPortionProtein();
                carbs += entry.getPortionCarbs();
                fat += entry.getPortionFat();
            }
        }
        return new DailyTotals(calories, protein, carbs, fat);
    }

    /**
     * Gets total calories.
     *
     * @return calories in kcal.
     */
    public double getCalories() {
        return calories;
    }

    /**
     * Gets total protein.
     *
     * @return protein in grams.
     */
    public double getProtein() {
        return protein;
    }

    /**
     * Gets total carbohydrates.
     *
     * @return carbohydrates in grams.
     */
    public double getCarbs() {
        return carbs;
    }

    /**
     * Gets total fat.
     *
     * @return fat in grams.
     */
    public double getFat() {
        return fat;
    }
}
