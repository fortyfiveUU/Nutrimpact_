/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: MainActivity.java
 * Description: Launcher activity for Nutrimpact. Shows the food search bar and runs an Open Food Facts search when the user taps the magnifying glass or presses search on the keyboard.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Entry point of the app. Lets the user search for a food by type or brand name.
 * Results are logged to Logcat until the results list is built.
 */
public class MainActivity extends AppCompatActivity {

    /** Logcat tag for API output. */
    private static final String TAG = "NutrimpactAPI";

    /** Outlined box around the search field; shows the magnifying glass and error messages. */
    private TextInputLayout layoutSearch;

    /** Text field the user types their search into. */
    private TextInputEditText editSearch;

    /**
     * Called when the activity is first created. Sets the layout and wires up the search bar.
     *
     * @param savedInstanceState previously saved state, or null on first launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutSearch = findViewById(R.id.layoutSearch);
        editSearch = findViewById(R.id.editSearch);

        // Tapping the magnifying glass runs the search.
        layoutSearch.setEndIconOnClickListener(view -> performSearch());

        // Pressing the search key on the keyboard also runs the search.
        editSearch.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Clear the "please enter" error as soon as the user starts typing.
        editSearch.addTextChangedListener(new TextWatcher() {

            /**
             * Not used.
             *
             * @param s     the text before the change.
             * @param start where the change starts.
             * @param count how many characters are being replaced.
             * @param after how many characters are replacing them.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**
             * Removes the error message once the user types something.
             *
             * @param s      the new text.
             * @param start  where the change starts.
             * @param before how many characters were replaced.
             * @param count  how many characters were added.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutSearch.setError(null);
            }

            /**
             * Not used.
             *
             * @param s the text after the change.
             */
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Reads the search bar, checks it isn't empty, then starts the search.
     * Shows an error under the search bar if nothing was entered.
     */
    private void performSearch() {
        Editable text = editSearch.getText();
        String query = (text != null) ? text.toString().trim() : "";

        if (query.isEmpty()) {
            layoutSearch.setError(getString(R.string.search_error_empty));
            return;
        }

        layoutSearch.setError(null);
        hideKeyboard();
        searchFood(query);
    }

    /**
     * Hides the on-screen keyboard so the results are visible.
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
        }
        editSearch.clearFocus();
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
                            + " | " + product.getBrands()
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
