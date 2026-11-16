/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: SearchViewModel.java
 * Description: ViewModel that runs Open Food Facts searches and holds the search screen's state, so results and loading progress survive screen rotation.
 */

package com.example.nutrimpact;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Owns the search logic for MainActivity. Android keeps the same ViewModel
 * when the activity is rebuilt (for example on rotation), so a search that is
 * running or already finished is not lost.
 */
public class SearchViewModel extends ViewModel {

    /** Logcat tag for API output. */
    private static final String TAG = "NutrimpactAPI";

    /** Current screen state, observed by MainActivity. */
    private final MutableLiveData<SearchUiState> state = new MutableLiveData<>(SearchUiState.idle());

    /** HTTP status Open Food Facts returns for an unknown barcode. */
    private static final int HTTP_NOT_FOUND = 404;

    /** The search or barcode lookup currently running, kept so it can be cancelled. */
    private Call<?> currentCall;

    /**
     * Default constructor, used by ViewModelProvider.
     */
    public SearchViewModel() {
    }

    /**
     * Gets the screen state for the activity to observe.
     *
     * @return read-only LiveData of the current state.
     */
    public LiveData<SearchUiState> getState() {
        return state;
    }

    /**
     * Searches Open Food Facts in the background and updates the state with
     * the results, a "no results" state or an error state. Cancels any earlier
     * search so its results can't overwrite this one.
     *
     * A valid barcode (EAN-8, UPC-A, EAN-13 or GTIN-14 with a correct check
     * digit) is looked up directly instead of being searched as text.
     *
     * @param query the search terms or barcode, already trimmed and not empty.
     */
    public void search(String query) {
        if (currentCall != null) {
            currentCall.cancel();
        }

        state.setValue(SearchUiState.loading(query));
        if (BarcodeUtils.isValidBarcode(query)) {
            lookupBarcode(query);
            return;
        }

        Call<FoodSearchResponse> call = OpenFoodFactsApi.Client.getApi().searchProducts(query);
        currentCall = call;
        call.enqueue(new Callback<FoodSearchResponse>() {

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
                    state.setValue(SearchUiState.serverError(query));
                    return;
                }

                SearchUiState result = SearchUiState.results(query, response.body().getProducts());
                Log.d(TAG, "Found " + result.getProducts().size() + " products for \"" + query + "\"");
                state.setValue(result);
            }

            /**
             * Handles a network failure, such as no connection or a timeout.
             * Does nothing if the search was cancelled on purpose.
             *
             * @param call the call that failed.
             * @param t    the error that occurred.
             */
            @Override
            public void onFailure(@NonNull Call<FoodSearchResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                state.setValue(SearchUiState.networkError(query));
            }
        });
    }

    /**
     * Looks up a single product by barcode and shows it as a one-item result list.
     *
     * @param barcode the barcode digits.
     */
    private void lookupBarcode(String barcode) {
        Call<ProductLookupResponse> call = OpenFoodFactsApi.Client.getApi().lookupBarcode(barcode);
        currentCall = call;
        call.enqueue(new Callback<ProductLookupResponse>() {

            /**
             * Handles the lookup response: found, not found, or a server error.
             *
             * @param call     the call that was made.
             * @param response the server's response.
             */
            @Override
            public void onResponse(@NonNull Call<ProductLookupResponse> call,
                                   @NonNull Response<ProductLookupResponse> response) {
                ProductLookupResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isFound()) {
                    Log.d(TAG, "Barcode " + barcode + " found");
                    state.setValue(SearchUiState.results(barcode,
                            Collections.singletonList(body.toFoodProduct())));
                } else if (response.code() == HTTP_NOT_FOUND || (response.isSuccessful() && body != null)) {
                    Log.d(TAG, "Barcode " + barcode + " not found");
                    state.setValue(SearchUiState.barcodeNotFound(barcode));
                } else {
                    Log.e(TAG, "Barcode lookup failed with HTTP code " + response.code());
                    state.setValue(SearchUiState.serverError(barcode));
                }
            }

            /**
             * Handles a network failure. Does nothing if the lookup was cancelled on purpose.
             *
             * @param call the call that failed.
             * @param t    the error that occurred.
             */
            @Override
            public void onFailure(@NonNull Call<ProductLookupResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                state.setValue(SearchUiState.networkError(barcode));
            }
        });
    }

    /**
     * Called when the screen is closed for good (not on rotation).
     * Cancels any running search.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
