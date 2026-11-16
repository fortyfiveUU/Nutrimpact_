/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FakeOpenFoodFacts.java
 * Description: Test helper that runs a fake Open Food Facts server on the device and points the app's API client at it, so search and barcode behaviour (including server errors) can be tested without the real internet service.
 */

package com.example.nutrimpact;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fake server wrapper. Every request the app makes, whatever host it was meant
 * for, is redirected to the local fake server, which replies with responses
 * queued by the test.
 */
public class FakeOpenFoodFacts {

    /** The local fake server. */
    private final MockWebServer server = new MockWebServer();

    /**
     * Starts the server and points the app's API client at it.
     *
     * @throws IOException if the server can't start.
     */
    public void start() throws IOException {
        server.start();
        HttpUrl local = server.url("/");

        // Rewrite each request to the fake server, keeping its path and query.
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    HttpUrl original = chain.request().url();
                    HttpUrl redirected = original.newBuilder()
                            .scheme(local.scheme())
                            .host(local.host())
                            .port(local.port())
                            .build();
                    Request request = chain.request().newBuilder().url(redirected).build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OpenFoodFactsApi.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()))
                .build();
        OpenFoodFactsApi.Client.setApiForTesting(retrofit.create(OpenFoodFactsApi.class));
    }

    /**
     * Stops the server and puts the real API client back.
     *
     * @throws IOException if the server can't stop cleanly.
     */
    public void stop() throws IOException {
        OpenFoodFactsApi.Client.setApiForTesting(null);
        server.shutdown();
    }

    /**
     * Queues a JSON response with a given HTTP status.
     *
     * @param status HTTP status code, e.g. 200 or 503.
     * @param json   response body.
     */
    public void respond(int status, String json) {
        server.enqueue(new MockResponse().setResponseCode(status)
                .setHeader("Content-Type", "application/json").setBody(json));
    }

    /**
     * Gets the path and query of the next request the app made.
     *
     * @return e.g. "/search?page_size=20&amp;fields=...&amp;q=oats".
     * @throws InterruptedException if interrupted while waiting.
     */
    public String takeRequestPath() throws InterruptedException {
        return server.takeRequest().getPath();
    }

    /**
     * A search response with one product, shaped like the real search API.
     *
     * @param name  product name.
     * @param brand brand.
     * @param kcal  calories per 100g.
     * @return JSON text.
     */
    public static String searchJson(String name, String brand, double kcal) {
        return "{\"hits\":[{\"product_name\":\"" + name + "\",\"brands\":[\"" + brand + "\"],"
                + "\"nutriments\":{\"energy-kcal_100g\":" + kcal + ",\"proteins_100g\":8.9,"
                + "\"carbohydrates_100g\":68,\"fat_100g\":6.4}}],\"count\":1}";
    }

    /**
     * A barcode lookup response for a found product, shaped like the real product API.
     *
     * @param name   product name.
     * @param brands brands as one string.
     * @param kcal   calories per 100g.
     * @return JSON text.
     */
    public static String productJson(String name, String brands, double kcal) {
        return "{\"status\":1,\"product\":{\"product_name\":\"" + name + "\",\"brands\":\"" + brands + "\","
                + "\"nutriments\":{\"energy-kcal_100g\":" + kcal + ",\"proteins_100g\":6.3,"
                + "\"carbohydrates_100g\":57.5,\"fat_100g\":30.9}}}";
    }
}
