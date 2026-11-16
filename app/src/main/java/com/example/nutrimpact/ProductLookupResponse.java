/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: ProductLookupResponse.java
 * Description: Model for the Open Food Facts barcode lookup response (api/v2/product/{barcode}), which returns one product with its brands as a single string.
 */

package com.example.nutrimpact;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Barcode lookup result. This endpoint returns "brands" as one comma-separated
 * string, unlike the search endpoint's list, so it has its own model and is
 * converted to a FoodProduct for the rest of the app.
 */
public class ProductLookupResponse {

    /** Status when the product was found. */
    public static final int STATUS_FOUND = 1;

    /** 1 if found, 0 if not. Source JSON key: "status". */
    @SerializedName("status")
    private Integer status;

    /** The product, or null if not found. Source JSON key: "product". */
    @SerializedName("product")
    private LookupProduct product;

    /**
     * Default constructor, required by Gson.
     */
    public ProductLookupResponse() {
    }

    /**
     * Checks whether Open Food Facts found the barcode.
     *
     * @return true if a product was returned.
     */
    public boolean isFound() {
        return status != null && status == STATUS_FOUND && product != null;
    }

    /**
     * Gets the status code.
     *
     * @return 1 if found, 0 if not, or 0 if the API omitted it.
     */
    public Integer getStatus() {
        return (status != null) ? status : 0;
    }

    /**
     * Sets the status code.
     *
     * @param status 1 if found, 0 if not.
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Gets the product converted to the app's FoodProduct model.
     *
     * @return the product, or null if not found.
     */
    public FoodProduct toFoodProduct() {
        if (product == null) {
            return null;
        }
        FoodProduct food = new FoodProduct();
        food.setProductName(product.getProductName());
        food.setBrands(splitBrands(product.getBrands()));
        food.setNutriments(product.getNutriments());
        return food;
    }

    /**
     * Sets the product.
     *
     * @param product the product.
     */
    public void setProduct(LookupProduct product) {
        this.product = product;
    }

    /**
     * Splits "Ferrero, Nutella" into ["Ferrero", "Nutella"].
     *
     * @param brands comma-separated brands, or null.
     * @return the list of brands, or null if there were none.
     */
    static List<String> splitBrands(String brands) {
        if (brands == null || brands.trim().isEmpty()) {
            return null;
        }
        List<String> list = new ArrayList<>();
        Collections.addAll(list, brands.split(","));
        return list;
    }

    /**
     * The product part of the lookup response.
     */
    public static class LookupProduct {

        /** Product name. Source JSON key: "product_name". */
        @SerializedName("product_name")
        private String productName;

        /** Brands as one string, e.g. "Ferrero, Nutella". Source JSON key: "brands". */
        @SerializedName("brands")
        private String brands;

        /** Per-100g nutrition. Source JSON key: "nutriments". */
        @SerializedName("nutriments")
        private Nutriments nutriments;

        /**
         * Default constructor, required by Gson.
         */
        public LookupProduct() {
        }

        /**
         * Gets the product name.
         *
         * @return the name, or null if missing.
         */
        public String getProductName() {
            return productName;
        }

        /**
         * Sets the product name.
         *
         * @param productName the name.
         */
        public void setProductName(String productName) {
            this.productName = productName;
        }

        /**
         * Gets the brands string.
         *
         * @return brands, or null if missing.
         */
        public String getBrands() {
            return brands;
        }

        /**
         * Sets the brands string.
         *
         * @param brands comma-separated brands.
         */
        public void setBrands(String brands) {
            this.brands = brands;
        }

        /**
         * Gets the nutrition values.
         *
         * @return nutriments, or null if missing.
         */
        public Nutriments getNutriments() {
            return nutriments;
        }

        /**
         * Sets the nutrition values.
         *
         * @param nutriments per-100g nutrition.
         */
        public void setNutriments(Nutriments nutriments) {
            this.nutriments = nutriments;
        }
    }
}
