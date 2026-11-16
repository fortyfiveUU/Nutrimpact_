/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: PortionCalculator.java
 * Description: Scales per-100g nutrition values to a user's portion size and converts between grams and ounces.
 */

package com.example.nutrimpact;

/**
 * Pure calculation helpers for portion sizes. Has no Android dependencies,
 * so it can be unit tested on the computer without an emulator.
 */
public final class PortionCalculator {

    /** Grams in one avoirdupois ounce. */
    public static final double GRAMS_PER_OUNCE = 28.349523125;

    /** Largest portion accepted, in grams (5 kg), to catch typing mistakes. */
    public static final double MAX_PORTION_GRAMS = 5000.0;

    /**
     * Private constructor so the class can't be instantiated.
     */
    private PortionCalculator() {
    }

    /**
     * Scales a per-100g value to a portion. For example, 379 kcal per 100g
     * scaled to 40g gives 151.6 kcal.
     *
     * @param valuePer100g  the nutrient value per 100g, or null if unknown.
     * @param portionGrams  the portion size in grams.
     * @return the value for the portion, or 0.0 if the value is unknown or the portion is negative.
     */
    public static double scale(Double valuePer100g, double portionGrams) {
        if (valuePer100g == null || portionGrams <= 0) {
            return 0.0;
        }
        return valuePer100g * portionGrams / 100.0;
    }

    /**
     * Converts ounces to grams.
     *
     * @param ounces the weight in ounces.
     * @return the weight in grams.
     */
    public static double ouncesToGrams(double ounces) {
        return ounces * GRAMS_PER_OUNCE;
    }

    /**
     * Converts grams to ounces.
     *
     * @param grams the weight in grams.
     * @return the weight in ounces.
     */
    public static double gramsToOunces(double grams) {
        return grams / GRAMS_PER_OUNCE;
    }

    /**
     * Checks whether a portion size is sensible: more than 0g and no more than 5kg.
     *
     * @param portionGrams the portion size in grams.
     * @return true if the portion can be logged.
     */
    public static boolean isValidPortion(double portionGrams) {
        return portionGrams > 0 && portionGrams <= MAX_PORTION_GRAMS;
    }
}
