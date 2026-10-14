package com.example.nutrimpact;

import com.google.gson.annotations.SerializedName;

public class Nutriments {

    @SerializedName("energy-kcal_100g")
    private Double caloriesPer100g;

    @SerializedName("proteins_100g")
    private Double proteinPer100g;

    @SerializedName("carbohydrates_100g")
    private Double carbsPer100g;

    @SerializedName("fat_100g")
    private Double fatPer100g;

    /**
     Default constructor
     */
    public Nutriments() {
    }

    /**
     Gets calories per 100g.
     */
    public Double getCaloriesPer100g() {
        return (caloriesPer100g != null) ? caloriesPer100g : 0.0;
    }

    /**
     Sets calories per 100g.
     */
    public void setCaloriesPer100g(Double caloriesPer100g) {
        this.caloriesPer100g = caloriesPer100g;
    }

    /**
     * Gets protein in grams per 100g.
     *
     * @return protein in grams, defaulting to 0.0 if omitted.
     */
    public Double getProteinPer100g() {
        return (proteinPer100g != null) ? proteinPer100g : 0.0;
    }

    /**
     * Sets protein in grams per 100g.
     *
     * @param proteinPer100g protein in grams.
     */
    public void setProteinPer100g(Double proteinPer100g) {
        this.proteinPer100g = proteinPer100g;
    }

    /**
     * Gets carbohydrates in grams per 100g.
     */
    public Double getCarbsPer100g() {
        return (carbsPer100g != null) ? carbsPer100g : 0.0;
    }

    /**
     * Sets carbohydrates in grams per 100g.
     */
    public void setCarbsPer100g(Double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
    }

    /**
     Gets total fat in grams per 100g.
     * */
    public Double getFatPer100g() {
        return (fatPer100g != null) ? fatPer100g : 0.0;
    }

    /**
     Sets total fat in grams per 100g.
     */
    public void setFatPer100g(Double fatPer100g) {
        this.fatPer100g = fatPer100g;
    }
}
