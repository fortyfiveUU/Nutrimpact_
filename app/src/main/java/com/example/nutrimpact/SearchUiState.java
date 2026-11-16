/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: SearchUiState.java
 * Description: Immutable snapshot of what the search screen should show: the start message, the loading spinner, a list of results, or a no-results or error message.
 */

package com.example.nutrimpact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes the current state of the search screen. Created by SearchViewModel
 * and drawn by MainActivity. Holds no Android Context, so it can safely
 * outlive the activity when the screen rotates.
 */
public final class SearchUiState {

    /**
     * The possible states of the search screen.
     */
    public enum Status {
        /** No search has been run yet. */
        IDLE,
        /** A search is in progress. */
        LOADING,
        /** The search found products. */
        RESULTS,
        /** The search worked but found nothing. */
        NO_RESULTS,
        /** A barcode was looked up but Open Food Facts doesn't have it. */
        BARCODE_NOT_FOUND,
        /** The device couldn't reach the server. */
        NETWORK_ERROR,
        /** The server returned an error. */
        SERVER_ERROR
    }

    /** Which state the screen is in. */
    private final Status status;

    /** Products to show; only filled when status is RESULTS. */
    private final List<FoodProduct> products;

    /** The search terms, used in the "no results" message. */
    private final String query;

    /**
     * Creates a state. Use the static factory methods instead of calling this directly.
     *
     * @param status   which state the screen is in.
     * @param products products to show, or null for none.
     * @param query    the search terms, or null if there is no search yet.
     */
    private SearchUiState(Status status, List<FoodProduct> products, String query) {
        this.status = status;
        this.products = (products != null)
                ? Collections.unmodifiableList(new ArrayList<>(products))
                : Collections.emptyList();
        this.query = query;
    }

    /**
     * Creates the starting state, before any search.
     *
     * @return an IDLE state.
     */
    public static SearchUiState idle() {
        return new SearchUiState(Status.IDLE, null, null);
    }

    /**
     * Creates the state shown while a search is running.
     *
     * @param query the search terms.
     * @return a LOADING state.
     */
    public static SearchUiState loading(String query) {
        return new SearchUiState(Status.LOADING, null, query);
    }

    /**
     * Creates the state for a successful search. Becomes NO_RESULTS if the list is empty.
     *
     * @param query    the search terms.
     * @param products the products found.
     * @return a RESULTS or NO_RESULTS state.
     */
    public static SearchUiState results(String query, List<FoodProduct> products) {
        Status status = (products == null || products.isEmpty()) ? Status.NO_RESULTS : Status.RESULTS;
        return new SearchUiState(status, products, query);
    }

    /**
     * Creates the state for a barcode that Open Food Facts doesn't know.
     *
     * @param barcode the barcode that was looked up.
     * @return a BARCODE_NOT_FOUND state.
     */
    public static SearchUiState barcodeNotFound(String barcode) {
        return new SearchUiState(Status.BARCODE_NOT_FOUND, null, barcode);
    }

    /**
     * Creates the state for a failed connection.
     *
     * @param query the search terms.
     * @return a NETWORK_ERROR state.
     */
    public static SearchUiState networkError(String query) {
        return new SearchUiState(Status.NETWORK_ERROR, null, query);
    }

    /**
     * Creates the state for a server error, such as HTTP 503.
     *
     * @param query the search terms.
     * @return a SERVER_ERROR state.
     */
    public static SearchUiState serverError(String query) {
        return new SearchUiState(Status.SERVER_ERROR, null, query);
    }

    /**
     * Gets which state the screen is in.
     *
     * @return the status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the products to show.
     *
     * @return a read-only list of products, empty unless the status is RESULTS.
     */
    public List<FoodProduct> getProducts() {
        return products;
    }

    /**
     * Gets the search terms.
     *
     * @return the query, or an empty string if no search has been run.
     */
    public String getQuery() {
        return (query != null) ? query : "";
    }
}
