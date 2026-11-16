/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DiaryActivity.java
 * Description: The app's home screen. Shows the food logged on a chosen day, grouped by meal, with daily totals and progress towards the user's goals, and lets the user add, browse and remove entries.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Daily food diary. Swipe a food left or right to remove it, with Undo.
 */
public class DiaryActivity extends AppCompatActivity {

    /** Intent extra: the day to show, as days since 1970-01-01. */
    private static final String EXTRA_SHOW_DAY = "com.example.nutrimpact.SHOW_DAY";

    /** Date format for days this year, e.g. "Mon 10 Nov". */
    private static final String DATE_PATTERN = "EEE d MMM";

    /** Date format for days in other years, e.g. "Mon 10 Nov 2025". */
    private static final String DATE_PATTERN_WITH_YEAR = "EEE d MMM yyyy";

    /** How long the Undo snackbar stays up, in milliseconds (LENGTH_LONG is only 2.75s). */
    private static final int UNDO_DURATION_MS = 5000;

    /** Root view, used to show the Undo snackbar. */
    private View layoutRoot;

    /** Shows "Today", "Yesterday" or the date. */
    private TextView textDiaryDay;

    /** Moves to the next day; disabled on today. */
    private Button buttonNextDay;

    /** Day's total energy. */
    private TextView textTotalCalories;

    /** "of 2,000 kcal · 1,848 kcal left" (or "over"). */
    private TextView textCalorieGoal;

    /** Energy progress towards the goal. */
    private LinearProgressIndicator progressCalories;

    /** Protein eaten and goal. */
    private TextView textProteinProgress;

    /** Protein progress towards the goal. */
    private LinearProgressIndicator progressProtein;

    /** Carbohydrates eaten and goal. */
    private TextView textCarbsProgress;

    /** Carbohydrate progress towards the goal. */
    private LinearProgressIndicator progressCarbs;

    /** Fat eaten and goal. */
    private TextView textFatProgress;

    /** Fat progress towards the goal. */
    private LinearProgressIndicator progressFat;

    /** List of meals and foods. */
    private RecyclerView recyclerDiary;

    /** Shown instead of the list when nothing is logged. */
    private TextView textDiaryEmpty;

    /** Turns DiaryItem rows into views. */
    private DiaryAdapter adapter;

    /** Holds the selected day and its entries. */
    private DiaryViewModel viewModel;

    /** Goals and units, saved on the device. */
    private UserSettings settings;

    /** The most recent entries, kept so totals can be redrawn when settings change. */
    private List<FoodLogEntry> lastEntries;

    /**
     * Builds an Intent that brings the diary to the front showing a given day,
     * closing any screens opened on top of it.
     *
     * @param context  the screen opening the diary.
     * @param epochDay the day to show, as days since 1970-01-01.
     * @return an Intent ready for startActivity().
     */
    public static Intent createShowDayIntent(Context context, long epochDay) {
        Intent intent = new Intent(context, DiaryActivity.class);
        intent.putExtra(EXTRA_SHOW_DAY, epochDay);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    /**
     * Called when the screen is created, including after rotation.
     *
     * @param savedInstanceState previously saved state, or null on first launch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        settings = new UserSettings(this);

        layoutRoot = findViewById(R.id.layoutDiaryRoot);
        textDiaryDay = findViewById(R.id.textDiaryDay);
        buttonNextDay = findViewById(R.id.buttonNextDay);
        textTotalCalories = findViewById(R.id.textTotalCalories);
        textCalorieGoal = findViewById(R.id.textCalorieGoal);
        progressCalories = findViewById(R.id.progressCalories);
        textProteinProgress = findViewById(R.id.textProteinProgress);
        progressProtein = findViewById(R.id.progressProtein);
        textCarbsProgress = findViewById(R.id.textCarbsProgress);
        progressCarbs = findViewById(R.id.progressCarbs);
        textFatProgress = findViewById(R.id.textFatProgress);
        progressFat = findViewById(R.id.progressFat);
        recyclerDiary = findViewById(R.id.recyclerDiary);
        textDiaryEmpty = findViewById(R.id.textDiaryEmpty);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDiary);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menuProgress) {
                startActivity(ProgressActivity.createIntent(this));
                return true;
            } else if (item.getItemId() == R.id.menuSettings) {
                startActivity(SettingsActivity.createIntent(this));
                return true;
            }
            return false;
        });

        adapter = new DiaryAdapter();
        recyclerDiary.setAdapter(adapter);
        new ItemTouchHelper(new SwipeToDeleteCallback()).attachToRecyclerView(recyclerDiary);

        viewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        if (savedInstanceState == null) {
            showDayFromIntent(getIntent());
        }

        viewModel.getSelectedDay().observe(this, this::renderDay);
        viewModel.getEntries().observe(this, this::renderEntries);

        findViewById(R.id.buttonPreviousDay).setOnClickListener(view -> viewModel.previousDay());
        buttonNextDay.setOnClickListener(view -> viewModel.nextDay());
        findViewById(R.id.fabAddFood).setOnClickListener(view -> openSearch());
    }

    /**
     * Re-reads goals and units each time the diary comes back into view,
     * so changes made in Settings show straight away.
     */
    @Override
    protected void onResume() {
        super.onResume();
        adapter.setEnergyUnit(settings.getEnergyUnit());
        if (lastEntries != null) {
            renderTotals(lastEntries);
        }
    }

    /**
     * Called when the diary is brought back to the front, e.g. after saving a
     * food. Switches to the day the food was logged on.
     *
     * @param intent the Intent that brought the diary back.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showDayFromIntent(intent);
    }

    /**
     * Shows the day named in an Intent, if there is one.
     *
     * @param intent the Intent to read.
     */
    private void showDayFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_SHOW_DAY)) {
            viewModel.showDay(intent.getLongExtra(EXTRA_SHOW_DAY, LocalDate.now().toEpochDay()));
        }
    }

    /**
     * Opens food search. Foods added from there are logged to the day shown here.
     */
    private void openSearch() {
        Long day = viewModel.getSelectedDay().getValue();
        long logDay = (day != null) ? day : LocalDate.now().toEpochDay();
        startActivity(MainActivity.createIntent(this, logDay));
    }

    /**
     * Updates the day label and the next-day button.
     *
     * @param epochDay the day now shown.
     */
    private void renderDay(Long epochDay) {
        LocalDate today = LocalDate.now();
        LocalDate day = (epochDay != null) ? LocalDate.ofEpochDay(epochDay) : today;
        textDiaryDay.setText(dayLabel(day, today));
        buttonNextDay.setEnabled(!viewModel.isShowingToday());
    }

    /**
     * Updates the totals, progress and list for the day's entries.
     *
     * @param entries the day's logged foods.
     */
    private void renderEntries(List<FoodLogEntry> entries) {
        lastEntries = entries;
        renderTotals(entries);

        adapter.submitList(DiaryItem.buildList(entries));
        boolean empty = (entries == null || entries.isEmpty());
        recyclerDiary.setVisibility(empty ? View.GONE : View.VISIBLE);
        textDiaryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows the day's totals against the saved goals.
     *
     * @param entries the day's logged foods.
     */
    private void renderTotals(List<FoodLogEntry> entries) {
        DailyTotals totals = DailyTotals.of(entries);
        NutritionGoals goals = settings.getGoals();
        EnergyUnit unit = settings.getEnergyUnit();

        double eaten = totals.getCalories();
        double goal = goals.getCalories();
        textTotalCalories.setText(unit.format(this, eaten));
        String leftOrOver = (eaten <= goal)
                ? getString(R.string.diary_calories_left, unit.format(this, goal - eaten))
                : getString(R.string.diary_calories_over, unit.format(this, eaten - goal));
        textCalorieGoal.setText(getString(R.string.diary_calorie_goal_line, unit.format(this, goal), leftOrOver));
        progressCalories.setContentDescription(getString(R.string.diary_calories_description,
                unit.format(this, eaten), unit.format(this, goal)));
        showProgress(progressCalories, eaten, goal);

        textProteinProgress.setText(getString(R.string.diary_macro_progress,
                getString(R.string.macro_protein), totals.getProtein(), goals.getProtein()));
        showProgress(progressProtein, totals.getProtein(), goals.getProtein());
        textCarbsProgress.setText(getString(R.string.diary_macro_progress,
                getString(R.string.macro_carbs), totals.getCarbs(), goals.getCarbs()));
        showProgress(progressCarbs, totals.getCarbs(), goals.getCarbs());
        textFatProgress.setText(getString(R.string.diary_macro_progress,
                getString(R.string.macro_fat), totals.getFat(), goals.getFat()));
        showProgress(progressFat, totals.getFat(), goals.getFat());
    }

    /**
     * Fills a progress bar, turning it the theme's error colour once the goal is exceeded.
     *
     * @param bar      the progress bar.
     * @param consumed the amount eaten.
     * @param goal     the goal.
     */
    private void showProgress(LinearProgressIndicator bar, double consumed, double goal) {
        bar.setProgressCompat(NutritionGoals.progressPercent(consumed, goal), false);
        int colourAttr = (consumed > goal) ? com.google.android.material.R.attr.colorError
                : com.google.android.material.R.attr.colorPrimary;
        bar.setIndicatorColor(MaterialColors.getColor(bar, colourAttr));
    }

    /**
     * Builds the label for a day: "Today", "Yesterday", or a date such as "Mon 10 Nov".
     *
     * @param day   the day to label.
     * @param today today's date.
     * @return the label.
     */
    private String dayLabel(LocalDate day, LocalDate today) {
        if (day.equals(today)) {
            return getString(R.string.diary_today);
        } else if (day.equals(today.minusDays(1))) {
            return getString(R.string.diary_yesterday);
        }
        String pattern = (day.getYear() == today.getYear()) ? DATE_PATTERN : DATE_PATTERN_WITH_YEAR;
        return day.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
    }

    /**
     * Removes a food when its row is swiped, and offers Undo.
     */
    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        /**
         * Allows swiping left or right, with no drag-and-drop.
         */
        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        /**
         * Not used: rows can't be dragged.
         *
         * @param recyclerView the list.
         * @param viewHolder   the row being dragged.
         * @param target       the row it's dragged over.
         * @return always false.
         */
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        /**
         * Stops meal headings from being swiped; only food rows can be removed.
         *
         * @param recyclerView the list.
         * @param viewHolder   the row.
         * @return the allowed swipe directions.
         */
        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder instanceof DiaryAdapter.HeaderViewHolder) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        /**
         * Removes the swiped food and shows a snackbar with Undo.
         *
         * @param viewHolder the swiped row.
         * @param direction  the swipe direction.
         */
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            FoodLogEntry entry = adapter.getRow(position).getEntry();
            if (entry == null) {
                adapter.notifyItemChanged(position);
                return;
            }

            viewModel.deleteEntry(entry);
            Snackbar.make(layoutRoot, getString(R.string.diary_entry_removed, entry.getProductName()),
                            UNDO_DURATION_MS)
                    .setAnchorView(R.id.fabAddFood)
                    .setAction(R.string.diary_undo, view -> viewModel.restoreEntry(entry))
                    .show();
        }
    }
}
