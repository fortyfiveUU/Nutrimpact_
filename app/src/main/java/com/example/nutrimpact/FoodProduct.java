/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodProduct.java
 * Description: Model for a single Open Food Facts product, holding its name, brand and nutritional breakdown.
 */

package com.example.nutrimpact;

import com.google.gson.annotations.SerializedName;

/**
 * Represents one food product returned by the Open Food Facts API.
 */
public class FoodProduct {

    /** Product name. Source JSON key: "product_name". */
    @SerializedName("product_name")
    private String productName;

    /** Comma-separated brand names. Source JSON key: "brands". */
    @SerializedName("brands")
    private String brands;

    /** Per-100g nutritional values. Source JSON key: "nutriments". */
    @SerializedName("nutriments")
    private Nutriments nutriments;

    /**
     * Default constructor, required by Gson for deserialisation.
     */
    public FoodProduct() {
    }

    /**
     * Gets the product name.
     *
     * @return the product name, or "Product not listed" if the API omitted it.
     */
    public String getProductName() {
        return (productName != null) ? productName : "Product not listed";
    }

    /**
     * Sets the product name.
     *
     * @param productName the product name.
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Gets the product's brand names.
     *
     * @return the brands, or "Brand not listed" if the API omitted them.
     */
    public String getBrands() {
        return (brands != null) ? brands : "Brand not listed";
    }

    /**
     * Sets the product's brand names.
     *
     * @param brands the brand names.
     */
    public void setBrands(String brands) {
        this.brands = brands;
    }

    /**
     * Gets the product's nutritional breakdown.
     *
     * @return the nutriments, or an empty Nutriments object (all values 0.0)
     *         if the API omitted them.
     */
    public Nutriments getNutriments() {
        return (nutriments != null) ? nutriments : new Nutriments();
    }

    /**
     * Sets the product's nutritional breakdown.
     *
     * @param nutriments the nutritional values per 100g.
     */
    public void setNutriments(Nutriments nutriments) {
        this.nutriments = nutriments;
    }
}
