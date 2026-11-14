/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: Nutriments.java
 * Description: Model for a product's macronutrient values per 100g (calories, protein, carbohydrates and fat), mapped from Open Food Facts JSON.
 */

package com.example.nutrimpact;

import com.google.gson.annotations.SerializedName;

/**
 * Holds a product's nutritional values per 100g. Missing values default to 0.0.
 */
public class Nutriments {

    /** Energy in kcal per 100g. Source JSON key: "energy-kcal_100g". */
    @SerializedName("energy-kcal_100g")
    private Double caloriesPer100g;

    /** Protein in grams per 100g. Source JSON key: "proteins_100g". */
    @SerializedName("proteins_100g")
    private Double proteinPer100g;

    /** Carbohydrates in grams per 100g. Source JSON key: "carbohydrates_100g". */
    @SerializedName("carbohydrates_100g")
    private Double carbsPer100g;

    /** Total fat in grams per 100g. Source JSON key: "fat_100g". */
    @SerializedName("fat_100g")
    private Double fatPer100g;

    /**
     * Default constructor, required by Gson for deserialisation.
     */
    public Nutriments() {
    }

    /**
     * Checks whether Open Food Facts supplied any of the four nutrition values.
     * Used to tell "no data" apart from a genuine 0.0, since the getters
     * return 0.0 for missing values.
     *
     * @return true if at least one value was present in the API response.
     */
    public boolean hasNutritionData() {
        return caloriesPer100g != null || proteinPer100g != null
                || carbsPer100g != null || fatPer100g != null;
    }

    /**
     * Gets calories per 100g.
     *
     * @return calories in kcal, or 0.0 if the API omitted it.
     */
    public Double getCaloriesPer100g() {
        return (caloriesPer100g != null) ? caloriesPer100g : 0.0;
    }

    /**
     * Sets calories per 100g.
     *
     * @param caloriesPer100g calories in kcal.
     */
    public void setCaloriesPer100g(Double caloriesPer100g) {
        this.caloriesPer100g = caloriesPer100g;
    }

    /**
     * Gets protein per 100g.
     *
     * @return protein in grams, or 0.0 if the API omitted it.
     */
    public Double getProteinPer100g() {
        return (proteinPer100g != null) ? proteinPer100g : 0.0;
    }

    /**
     * Sets protein per 100g.
     *
     * @param proteinPer100g protein in grams.
     */
    public void setProteinPer100g(Double proteinPer100g) {
        this.proteinPer100g = proteinPer100g;
    }

    /**
     * Gets carbohydrates per 100g.
     *
     * @return carbohydrates in grams, or 0.0 if the API omitted it.
     */
    public Double getCarbsPer100g() {
        return (carbsPer100g != null) ? carbsPer100g : 0.0;
    }

    /**
     * Sets carbohydrates per 100g.
     *
     * @param carbsPer100g carbohydrates in grams.
     */
    public void setCarbsPer100g(Double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
    }

    /**
     * Gets total fat per 100g.
     *
     * @return fat in grams, or 0.0 if the API omitted it.
     */
    public Double getFatPer100g() {
        return (fatPer100g != null) ? fatPer100g : 0.0;
    }

    /**
     * Sets total fat per 100g.
     *
     * @param fatPer100g fat in grams.
     */
    public void setFatPer100g(Double fatPer100g) {
        this.fatPer100g = fatPer100g;
    }
}
