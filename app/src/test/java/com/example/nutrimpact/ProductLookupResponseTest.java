/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: ProductLookupResponseTest.java
 * Description: JUnit tests for ProductLookupResponse: parsing real Open Food Facts barcode JSON with Gson and converting it to a FoodProduct.
 */

package com.example.nutrimpact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

/**
 * Unit tests for ProductLookupResponse, using JSON shaped like the real API.
 */
public class ProductLookupResponseTest {

    /** Allowed rounding difference when comparing doubles. */
    private static final double DELTA = 0.0001;

    /** Same Gson setup as the app's Retrofit client. */
    private final Gson gson = new com.google.gson.GsonBuilder()
            .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    /**
     * A found product converts to a FoodProduct with its brands string split into a list.
     */
    @Test
    public void foundProduct_convertsToFoodProduct() {
        String json = "{\"code\":\"3017620422003\",\"status\":1,\"status_verbose\":\"product found\","
                + "\"product\":{\"product_name\":\"Nutella\",\"brands\":\"Ferrero, Nutella\","
                + "\"nutriments\":{\"energy-kcal_100g\":539,\"proteins_100g\":6.3,"
                + "\"carbohydrates_100g\":57.5,\"fat_100g\":30.9}}}";

        ProductLookupResponse response = gson.fromJson(json, ProductLookupResponse.class);
        assertTrue(response.isFound());

        FoodProduct product = response.toFoodProduct();
        assertEquals("Nutella", product.getProductName());
        assertEquals("Ferrero, Nutella", product.getBrands());
        assertEquals(539, product.getNutriments().getCaloriesPer100g(), DELTA);
        assertEquals(30.9, product.getNutriments().getFatPer100g(), DELTA);
    }

    /**
     * An unknown barcode (status 0, no product) is reported as not found.
     */
    @Test
    public void notFound_isReported() {
        String json = "{\"code\":\"0000000000017\",\"status\":0,\"status_verbose\":\"no code or invalid code\"}";

        ProductLookupResponse response = gson.fromJson(json, ProductLookupResponse.class);
        assertFalse(response.isFound());
        assertNull(response.toFoodProduct());
    }

    /**
     * Missing brands and nutrition fall back to the app's safe defaults.
     */
    @Test
    public void missingFields_useDefaults() {
        String json = "{\"status\":1,\"product\":{\"product_name\":\"Mystery\"}}";

        FoodProduct product = gson.fromJson(json, ProductLookupResponse.class).toFoodProduct();
        assertEquals("Brand not listed", product.getBrands());
        assertFalse(product.getNutriments().hasNutritionData());
    }

    /**
     * Brand strings are split on commas; blank strings give no brands.
     */
    @Test
    public void splitBrands_handlesCommasAndBlanks() {
        assertEquals(2, ProductLookupResponse.splitBrands("Ferrero, Nutella").size());
        assertEquals(1, ProductLookupResponse.splitBrands("Twix").size());
        assertNull(ProductLookupResponse.splitBrands("  "));
        assertNull(ProductLookupResponse.splitBrands(null));
    }
}
