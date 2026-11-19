/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: SearchScreenTest.java
 * Description: Espresso UI tests for the food search screen, using a fake Open Food Facts server so results, barcode lookups and server errors can be tested reliably.
 */

package com.example.nutrimpact;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests MainActivity (search) against a fake server.
 */
@RunWith(AndroidJUnit4.class)
public class SearchScreenTest {

    /** Fake Open Food Facts server. */
    private final FakeOpenFoodFacts server = new FakeOpenFoodFacts();

    /** The running search screen. */
    private ActivityScenario<MainActivity> scenario;

    /**
     * Starts the fake server, resets settings, and opens search.
     *
     * @throws Exception if the server can't start.
     */
    @Before
    public void setUp() throws Exception {
        TestHelpers.resetAppData();
        server.start();
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    /**
     * Closes the screen and stops the fake server.
     *
     * @throws Exception if the server can't stop.
     */
    @After
    public void tearDown() throws Exception {
        scenario.close();
        server.stop();
    }

    /**
     * Types a search and taps the magnifying glass.
     *
     * @param text what to search for.
     */
    private void search(String text) {
        onView(withId(R.id.editSearch)).perform(replaceText(text));
        closeSoftKeyboard();
        onView(withContentDescription(R.string.search_button_description)).perform(click());
    }

    /**
     * A successful search shows a result card with the product's details,
     * and sends the search words to the API.
     */
    @Test
    public void search_showsResultCards() throws Exception {
        server.respond(200, FakeOpenFoodFacts.searchJson("Test Oats", "Quaker", 379));
        search("oats");

        TestHelpers.waitFor(onView(withText("Test Oats")), matches(isDisplayed()));
        onView(withText("Quaker")).check(matches(isDisplayed()));
        onView(withText("379 kcal per 100g")).check(matches(isDisplayed()));
        assertTrue(server.takeRequestPath().contains("q=oats"));
    }

    /**
     * A server error (HTTP 503) shows the "having problems" message.
     * This couldn't be tested against the real server.
     */
    @Test
    public void serverError_showsProblemMessage() {
        server.respond(503, "{}");
        search("oats");

        TestHelpers.waitFor(onView(withText(R.string.search_error_server)), matches(isDisplayed()));
    }

    /**
     * An empty result list shows the no-results message with the search words.
     */
    @Test
    public void noResults_showsMessage() {
        server.respond(200, "{\"hits\":[],\"count\":0}");
        search("qzxv");

        String expected = TestHelpers.context().getString(R.string.search_no_results, "qzxv");
        TestHelpers.waitFor(onView(withText(expected)), matches(isDisplayed()));
    }

    /**
     * Searching with nothing typed shows an error and makes no request.
     */
    @Test
    public void emptySearch_showsError() {
        search("   ");

        onView(withText(R.string.search_error_empty)).check(matches(isDisplayed()));
        onView(withText(R.string.search_start_message)).check(matches(isDisplayed()));
    }

    /**
     * A valid barcode is looked up on the product endpoint and shown as one result.
     */
    @Test
    public void barcode_isLookedUp() throws Exception {
        server.respond(200, FakeOpenFoodFacts.productJson("Nutella", "Ferrero, Nutella", 539));
        search("3017620422003");

        TestHelpers.waitFor(onView(withText("Nutella")), matches(isDisplayed()));
        onView(withText("Ferrero, Nutella")).check(matches(isDisplayed()));
        onView(withText("539 kcal per 100g")).check(matches(isDisplayed()));
        assertTrue(server.takeRequestPath().startsWith("/api/v2/product/3017620422003"));
    }

    /**
     * An unknown barcode (HTTP 404) shows the not-found message.
     */
    @Test
    public void unknownBarcode_showsNotFound() {
        server.respond(404, "{\"status\":0,\"status_verbose\":\"product not found\"}");
        search("0000000000000");

        String expected = TestHelpers.context().getString(R.string.search_barcode_not_found, "0000000000000");
        TestHelpers.waitFor(onView(withText(expected)), matches(isDisplayed()));
    }

    /**
     * A number with a wrong check digit is searched as text, not looked up as a barcode.
     */
    @Test
    public void wrongCheckDigit_isSearchedAsText() throws Exception {
        server.respond(200, "{\"hits\":[],\"count\":0}");
        search("3017620422004");

        String path = server.takeRequestPath();
        assertTrue(path.startsWith("/search"));
        assertTrue(path.contains("q=3017620422004"));
    }

    /**
     * Results are shown in kJ when that unit is chosen in Settings.
     */
    @Test
    public void results_followEnergyUnitSetting() {
        new UserSettings(TestHelpers.context()).setEnergyUnit(EnergyUnit.KJ);
        scenario.recreate();
        server.respond(200, FakeOpenFoodFacts.searchJson("Test Oats", "Quaker", 379));
        search("oats");

        TestHelpers.waitFor(onView(withText("1,586 kJ per 100g")), matches(isDisplayed()));
        assertEquals(EnergyUnit.KJ, new UserSettings(TestHelpers.context()).getEnergyUnit());
    }

    /**
     * The back arrow in the top bar closes search.
     */
    @Test
    public void backArrow_closesSearch() {
        onView(withContentDescription(R.string.add_food_back)).perform(click());
        assertTrue("Search did not close",
                TestHelpers.waitUntil(() -> scenario.getState() == Lifecycle.State.DESTROYED));
    }
}
