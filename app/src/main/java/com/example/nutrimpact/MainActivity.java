/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: MainActivity.java
 * Description: Launcher activity for Nutrimpact. Shows the main layout and runs a test Open Food Facts search, logging the results to Logcat.
 */

package com.example.nutrimpact;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Entry point of the app. Until the search UI is built in Sprint 3,
 * it runs a fixed search and logs the parsed results.
 */
public class MainActivity extends AppCompatActivity {

    /** Logcat tag for API output. */
    private static final String TAG = "NutrimpactAPI";

    /** Search term used for the test query. */
    private static final String TEST_QUERY = "Oats";

    /**
     * Called when the activity is first created. Sets the layout and starts the test search.
     *
     * @param savedInstanceState previously saved state, or null on first launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchFood(TEST_QUERY);
    }

    /**
     * Searches Open Food Facts in the background and logs each product's
     * name, calories and protein.
     *
     * @param query the search terms.
     */
    private void searchFood(String query) {
        OpenFoodFactsApi.Client.getApi().searchProducts(query).enqueue(new Callback<FoodSearchResponse>() {

            /**
             * Handles a response from the server, successful or not.
             *
             * @param call     the call that was made.
             * @param response the server's response.
             */
            @Override
            public void onResponse(@NonNull Call<FoodSearchResponse> call,
                                   @NonNull Response<FoodSearchResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Search failed with HTTP code " + response.code());
                    return;
                }

                List<FoodProduct> products = response.body().getProducts();
                Log.d(TAG, "Found " + products.size() + " products for \"" + query + "\"");

                for (FoodProduct product : products) {
                    Nutriments nutriments = product.getNutriments();
                    Log.d(TAG, product.getProductName()
                            + " | " + nutriments.getCaloriesPer100g() + " kcal"
                            + " | " + nutriments.getProteinPer100g() + " g protein");
                }
            }

            /**
             * Handles a network failure, such as no connection or a timeout.
             *
             * @param call the call that failed.
             * @param t    the error that occurred.
             */
            @Override
            public void onFailure(@NonNull Call<FoodSearchResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
            }
        });
    }
}
