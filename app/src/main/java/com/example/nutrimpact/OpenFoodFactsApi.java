/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: OpenFoodFactsApi.java
 * Description: Retrofit interface for the Open Food Facts REST API, plus a singleton Client that builds and shares one configured instance.
 */

package com.example.nutrimpact;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Defines the Open Food Facts endpoints used by Nutrimpact.
 */
public interface OpenFoodFactsApi {

    /** Base URL of the main Open Food Facts API, used for product lookups. */
    String BASE_URL = "https://world.openfoodfacts.org/";

    /**
     * Full URL of the Open Food Facts search service ("Search-a-licious").
     * It replaces the older cgi/search.pl endpoint, which was often overloaded
     * and returned HTTP 503 errors. Only the fields the app uses are requested,
     * to keep responses small.
     */
    String SEARCH_URL = "https://search.openfoodfacts.org/search"
            + "?page_size=20&fields=product_name,brands,nutriments";

    /**
     * Searches Open Food Facts for products matching a food type or brand name.
     *
     * @param query the search terms, e.g. "oats" or "Heinz".
     * @return a Retrofit call that returns the matching products.
     */
    @GET(SEARCH_URL)
    Call<FoodSearchResponse> searchProducts(@Query("q") String query);

    /**
     * Builds and holds a single shared OpenFoodFactsApi instance.
     */
    final class Client {

        /** User-Agent sent with every request, as Open Food Facts asks apps to identify themselves. */
        private static final String USER_AGENT = "Nutrimpact/1.0 (Android)";

        /** The shared API instance, created on first use. */
        private static OpenFoodFactsApi api;

        /**
         * Private constructor so the class can't be instantiated.
         */
        private Client() {
        }

        /**
         * Gets the shared API instance, creating it on first use.
         *
         * @return the configured OpenFoodFactsApi.
         */
        public static synchronized OpenFoodFactsApi getApi() {
            if (api == null) {
                Gson gson = new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();

                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request request = chain.request().newBuilder()
                                    .header("User-Agent", USER_AGENT)
                                    .build();
                            return chain.proceed(request);
                        })
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(httpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                api = retrofit.create(OpenFoodFactsApi.class);
            }
            return api;
        }
    }
}
