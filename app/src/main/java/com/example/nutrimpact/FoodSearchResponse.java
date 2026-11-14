/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodSearchResponse.java
 * Description: Top-level model for an Open Food Facts search response, holding the list of matching food products.
 */

package com.example.nutrimpact;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps the list of products returned by an Open Food Facts search.
 */
public class FoodSearchResponse {

    /** Products matching the search. Source JSON key: "products". */
    @SerializedName("products")
    private List<FoodProduct> products;

    /**
     * Default constructor. Starts with an empty product list.
     */
    public FoodSearchResponse() {
        this.products = new ArrayList<>();
    }

    /**
     * Gets the products returned by the search.
     *
     * @return the product list, or an empty list if the API omitted it.
     */
    public List<FoodProduct> getProducts() {
        return (products != null) ? products : new ArrayList<>();
    }

    /**
     * Sets the products returned by the search.
     *
     * @param products the product list.
     */
    public void setProducts(List<FoodProduct> products) {
        this.products = products;
    }
}
