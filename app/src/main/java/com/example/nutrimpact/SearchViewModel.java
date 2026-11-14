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

    /** The search currently running, kept so it can be cancelled. */
    private Call<FoodSearchResponse> currentCall;

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
     * @param query the search terms, already trimmed and not empty.
     */
    public void search(String query) {
        if (currentCall != null) {
            currentCall.cancel();
        }

        state.setValue(SearchUiState.loading(query));
        currentCall = OpenFoodFactsApi.Client.getApi().searchProducts(query);
        currentCall.enqueue(new Callback<FoodSearchResponse>() {

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
