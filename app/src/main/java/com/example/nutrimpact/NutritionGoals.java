/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: NutritionGoals.java
 * Description: The user's daily calorie, protein, carbohydrate and fat targets, with UK Reference Intake defaults and range checks.
 */

package com.example.nutrimpact;

/**
 * Immutable set of daily nutrition targets. Has no Android dependencies,
 * so it can be unit tested on the computer.
 */
public final class NutritionGoals {

    /** Default daily energy target in kcal (UK Reference Intake for an adult). */
    public static final int DEFAULT_CALORIES = 2000;

    /** Default daily protein target in grams (UK Reference Intake). */
    public static final int DEFAULT_PROTEIN = 50;

    /** Default daily carbohydrate target in grams (UK Reference Intake). */
    public static final int DEFAULT_CARBS = 260;

    /** Default daily fat target in grams (UK Reference Intake). */
    public static final int DEFAULT_FAT = 70;

    /** Lowest calorie goal accepted, in kcal. */
    public static final int MIN_CALORIES = 500;

    /** Highest calorie goal accepted, in kcal. */
    public static final int MAX_CALORIES = 10000;

    /** Highest macronutrient goal accepted, in grams. */
    public static final int MAX_MACRO_GRAMS = 1000;

    /** Daily energy target in kcal. */
    private final int calories;

    /** Daily protein target in grams. */
    private final int protein;

    /** Daily carbohydrate target in grams. */
    private final int carbs;

    /** Daily fat target in grams. */
    private final int fat;

    /**
     * Creates a set of goals. Values should be checked with the isValid methods first.
     *
     * @param calories daily energy target in kcal.
     * @param protein  daily protein target in grams.
     * @param carbs    daily carbohydrate target in grams.
     * @param fat      daily fat target in grams.
     */
    public NutritionGoals(int calories, int protein, int carbs, int fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    /**
     * Gets the UK Reference Intake goals used until the user sets their own.
     *
     * @return 2000 kcal, 50g protein, 260g carbohydrate, 70g fat.
     */
    public static NutritionGoals defaults() {
        return new NutritionGoals(DEFAULT_CALORIES, DEFAULT_PROTEIN, DEFAULT_CARBS, DEFAULT_FAT);
    }

    /**
     * Checks a calorie goal is sensible.
     *
     * @param calories the goal in kcal.
     * @return true if between 500 and 10,000 kcal.
     */
    public static boolean isValidCalories(int calories) {
        return calories >= MIN_CALORIES && calories <= MAX_CALORIES;
    }

    /**
     * Checks a protein, carbohydrate or fat goal is sensible.
     *
     * @param grams the goal in grams.
     * @return true if between 1 and 1,000 g.
     */
    public static boolean isValidMacro(int grams) {
        return grams >= 1 && grams <= MAX_MACRO_GRAMS;
    }

    /**
     * Gets the daily energy target.
     *
     * @return the target in kcal.
     */
    public int getCalories() {
        return calories;
    }

    /**
     * Gets the daily protein target.
     *
     * @return the target in grams.
     */
    public int getProtein() {
        return protein;
    }

    /**
     * Gets the daily carbohydrate target.
     *
     * @return the target in grams.
     */
    public int getCarbs() {
        return carbs;
    }

    /**
     * Gets the daily fat target.
     *
     * @return the target in grams.
     */
    public int getFat() {
        return fat;
    }

    /**
     * Works out how far through a goal an amount is, for progress bars.
     *
     * @param consumed the amount eaten.
     * @param goal     the target; 0 or less is treated as already met.
     * @return a whole percentage from 0 to 100 (never above 100, even when over the goal).
     */
    public static int progressPercent(double consumed, double goal) {
        if (goal <= 0) {
            return 100;
        }
        if (consumed <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(consumed / goal * 100.0));
    }
}
