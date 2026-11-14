/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodProduct.java
 * Description: Model for a single Open Food Facts product, holding its name, brand and nutritional breakdown.
 */

package com.example.nutrimpact;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one food product returned by the Open Food Facts API.
 */
public class FoodProduct {

    /** Product name. Source JSON key: "product_name". */
    @SerializedName("product_name")
    private String productName;

    /** Brand names, e.g. ["Quaker", "Quaker Oats"]. Source JSON key: "brands". */
    @SerializedName("brands")
    private List<String> brands;

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
     * Gets the product's brand names as one readable string, e.g. "Quaker, Quaker Oats".
     * Blank entries are skipped and extra spaces are trimmed.
     *
     * @return the brands joined with commas, or "Brand not listed" if there are none.
     */
    public String getBrands() {
        if (brands == null) {
            return "Brand not listed";
        }

        List<String> cleaned = new ArrayList<>();
        for (String brand : brands) {
            if (brand != null && !brand.trim().isEmpty()) {
                cleaned.add(brand.trim());
            }
        }
        return cleaned.isEmpty() ? "Brand not listed" : String.join(", ", cleaned);
    }

    /**
     * Sets the product's brand names.
     *
     * @param brands the list of brand names.
     */
    public void setBrands(List<String> brands) {
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
