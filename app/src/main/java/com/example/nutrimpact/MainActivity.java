/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: MainActivity.java
 * Description: Launcher activity for Nutrimpact. Handles the search bar and draws the search state from SearchViewModel: a spinner while loading, the results list, or a friendly message.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

/**
 * Entry point of the app. Hosts the food search screen. The search itself
 * runs in SearchViewModel, so it survives screen rotation.
 */
public class MainActivity extends AppCompatActivity {

    /** Intent extra: the day foods are logged to, as days since 1970-01-01. */
    private static final String EXTRA_LOG_DAY = "com.example.nutrimpact.LOG_DAY";

    /** Day that foods picked here are logged to. */
    private long logDay;

    /** Outlined box around the search field; shows the magnifying glass and error messages. */
    private TextInputLayout layoutSearch;

    /** Text field the user types their search into. */
    private TextInputEditText editSearch;

    /** Spinner shown while a search is running. */
    private ProgressBar progressSearch;

    /** List of search results. */
    private RecyclerView recyclerResults;

    /** Message shown instead of the list: start hint, no results or an error. */
    private TextView textMessage;

    /** Adapter that turns FoodProduct objects into result cards. */
    private FoodProductAdapter adapter;

    /** Runs searches and holds the screen state across rotation. */
    private SearchViewModel viewModel;

    /**
     * Builds an Intent that opens food search for logging to a given day.
     *
     * @param context the screen opening search.
     * @param logDay  the day foods will be logged to, as days since 1970-01-01.
     * @return an Intent ready for startActivity().
     */
    public static Intent createIntent(Context context, long logDay) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_LOG_DAY, logDay);
        return intent;
    }

    /**
     * Called when the activity is created, including after rotation. Sets the
     * layout, wires up the search bar and starts observing the search state.
     *
     * @param savedInstanceState previously saved state, or null on first launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logDay = getIntent().getLongExtra(EXTRA_LOG_DAY, LocalDate.now().toEpochDay());

        layoutSearch = findViewById(R.id.layoutSearch);
        editSearch = findViewById(R.id.editSearch);
        progressSearch = findViewById(R.id.progressSearch);
        recyclerResults = findViewById(R.id.recyclerResults);
        textMessage = findViewById(R.id.textMessage);

        adapter = new FoodProductAdapter(this::onProductClicked);
        recyclerResults.setAdapter(adapter);

        // Returns the existing ViewModel after rotation, or creates one on first launch.
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        viewModel.getState().observe(this, this::render);

        // Tapping the magnifying glass runs the search.
        layoutSearch.setEndIconOnClickListener(view -> performSearch());

        // The search key on the on-screen keyboard also runs the search.
        editSearch.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH && event == null) {
                performSearch();
                return true;
            }
            return false;
        });

        // Enter on a physical keyboard runs the search when the key is released.
        // The key-down is consumed too: some keyboards swallow it, and if it is
        // left alone Android moves focus to the first result card instead.
        editSearch.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    performSearch();
                }
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
     * Reads the search bar, checks it isn't empty, then asks the ViewModel to search.
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
        viewModel.search(query);
    }

    /**
     * Opens the Add Food screen for a tapped product. Products with no
     * nutrition data can't be logged, so a short message is shown instead.
     *
     * @param product the product that was tapped.
     */
    private void onProductClicked(FoodProduct product) {
        if (!product.getNutriments().hasNutritionData()) {
            Snackbar.make(recyclerResults, R.string.item_cannot_log, Snackbar.LENGTH_LONG).show();
            return;
        }
        startActivity(AddFoodActivity.createIntent(this, product, logDay));
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
     * Updates the screen to match the search state. Called whenever the state
     * changes, and again with the latest state after rotation.
     *
     * @param state the state to show.
     */
    private void render(SearchUiState state) {
        switch (state.getStatus()) {
            case LOADING:
                // Clear the old list and reset its scroll position, so new
                // results start at the top instead of where the old list was.
                adapter.setProducts(null);
                recyclerResults.scrollToPosition(0);
                showOnly(progressSearch);
                break;
            case RESULTS:
                adapter.setProducts(state.getProducts());
                showOnly(recyclerResults);
                break;
            case NO_RESULTS:
                showMessage(getString(R.string.search_no_results, state.getQuery()));
                break;
            case NETWORK_ERROR:
                showMessage(getString(R.string.search_error_network));
                break;
            case SERVER_ERROR:
                showMessage(getString(R.string.search_error_server));
                break;
            case IDLE:
            default:
                showMessage(getString(R.string.search_start_message));
                break;
        }
    }

    /**
     * Shows a message in place of the list, e.g. "no results" or an error.
     *
     * @param message the text to show.
     */
    private void showMessage(String message) {
        adapter.setProducts(null);
        textMessage.setText(message);
        showOnly(textMessage);
    }

    /**
     * Makes one of the spinner, list or message visible and hides the other two.
     *
     * @param visibleView the view to show.
     */
    private void showOnly(View visibleView) {
        progressSearch.setVisibility(visibleView == progressSearch ? View.VISIBLE : View.GONE);
        recyclerResults.setVisibility(visibleView == recyclerResults ? View.VISIBLE : View.GONE);
        textMessage.setVisibility(visibleView == textMessage ? View.VISIBLE : View.GONE);
    }
}
