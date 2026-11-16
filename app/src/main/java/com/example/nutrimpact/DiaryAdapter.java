/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: DiaryAdapter.java
 * Description: RecyclerView adapter for the diary, showing meal headings with calorie subtotals and the foods logged under each meal.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

/**
 * Shows DiaryItem rows using two layouts: one for meal headings and one for
 * logged foods. Uses DiffUtil to work out exactly which rows changed, so
 * swiping, deleting and Undo animate correctly and the swipe handler is
 * always told which row was removed.
 */
public class DiaryAdapter extends ListAdapter<DiaryItem, RecyclerView.ViewHolder> {

    /**
     * Tells DiffUtil which rows are the same item and whether their contents changed.
     */
    private static final DiffUtil.ItemCallback<DiaryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<DiaryItem>() {

        /**
         * Headings match by meal; foods match by database ID.
         *
         * @param oldItem the row in the old list.
         * @param newItem the row in the new list.
         * @return true if both rows represent the same thing.
         */
        @Override
        public boolean areItemsTheSame(@NonNull DiaryItem oldItem, @NonNull DiaryItem newItem) {
            if (oldItem.getType() != newItem.getType()) {
                return false;
            }
            if (oldItem.getType() == DiaryItem.TYPE_HEADER) {
                return oldItem.getMeal() == newItem.getMeal();
            }
            return oldItem.getEntry().getId() == newItem.getEntry().getId();
        }

        /**
         * Checks whether what's shown on screen would change.
         *
         * @param oldItem the row in the old list.
         * @param newItem the row in the new list.
         * @return true if the row looks the same.
         */
        @Override
        public boolean areContentsTheSame(@NonNull DiaryItem oldItem, @NonNull DiaryItem newItem) {
            if (oldItem.getType() == DiaryItem.TYPE_HEADER) {
                return oldItem.getMealCalories() == newItem.getMealCalories();
            }
            FoodLogEntry a = oldItem.getEntry();
            FoodLogEntry b = newItem.getEntry();
            return a.getPortionGrams() == b.getPortionGrams()
                    && a.getCaloriesPer100g() == b.getCaloriesPer100g()
                    && a.getProteinPer100g() == b.getProteinPer100g()
                    && a.getCarbsPer100g() == b.getCarbsPer100g()
                    && a.getFatPer100g() == b.getFatPer100g()
                    && Objects.equals(a.getProductName(), b.getProductName())
                    && Objects.equals(a.getBrands(), b.getBrands());
        }
    };

    /** Unit energy is shown in. */
    private EnergyUnit energyUnit = EnergyUnit.KCAL;

    /**
     * Creates an adapter with an empty list.
     */
    public DiaryAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Sets the unit energy is shown in, redrawing the rows if it changed.
     *
     * @param unit KCAL or KJ.
     */
    public void setEnergyUnit(EnergyUnit unit) {
        if (unit != energyUnit) {
            energyUnit = unit;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    /**
     * Gets the row at a position.
     *
     * @param position the row's position.
     * @return the row.
     */
    public DiaryItem getRow(int position) {
        return getItem(position);
    }

    /**
     * Gets which layout a row uses.
     *
     * @param position the row's position.
     * @return DiaryItem.TYPE_HEADER or DiaryItem.TYPE_ENTRY.
     */
    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    /**
     * Creates a view for a heading or a food row.
     *
     * @param parent   the RecyclerView.
     * @param viewType DiaryItem.TYPE_HEADER or DiaryItem.TYPE_ENTRY.
     * @return a ViewHolder for the new view.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == DiaryItem.TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_diary_header, parent, false));
        }
        return new EntryViewHolder(inflater.inflate(R.layout.item_diary_entry, parent, false));
    }

    /**
     * Fills a view with the row at a position.
     *
     * @param holder   the view to fill.
     * @param position the row's position.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DiaryItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item, energyUnit);
        } else if (holder instanceof EntryViewHolder) {
            ((EntryViewHolder) holder).bind(item.getEntry(), energyUnit);
        }
    }

    /**
     * Holds the views for a meal heading.
     */
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        /** Shows the meal name. */
        private final TextView textMealName;

        /** Shows the meal's calorie subtotal. */
        private final TextView textMealCalories;

        /**
         * Finds the heading's text views.
         *
         * @param itemView the inflated heading layout.
         */
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textMealName = itemView.findViewById(R.id.textMealName);
            textMealCalories = itemView.findViewById(R.id.textMealCalories);
        }

        /**
         * Shows a meal heading.
         *
         * @param item the heading row.
         * @param unit the unit to show energy in.
         */
        public void bind(DiaryItem item, EnergyUnit unit) {
            Context context = itemView.getContext();
            textMealName.setText(item.getMeal().getLabelResId());
            textMealCalories.setText(unit.format(context, item.getMealCalories()));
        }
    }

    /**
     * Holds the views for one logged food.
     */
    public static class EntryViewHolder extends RecyclerView.ViewHolder {

        /** Shows the product name. */
        private final TextView textEntryName;

        /** Shows the portion and brand. */
        private final TextView textEntryPortion;

        /** Shows the portion's protein, carbs and fat. */
        private final TextView textEntryMacros;

        /** Shows the portion's calories. */
        private final TextView textEntryCalories;

        /**
         * Finds the row's text views.
         *
         * @param itemView the inflated food row layout.
         */
        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textEntryName = itemView.findViewById(R.id.textEntryName);
            textEntryPortion = itemView.findViewById(R.id.textEntryPortion);
            textEntryMacros = itemView.findViewById(R.id.textEntryMacros);
            textEntryCalories = itemView.findViewById(R.id.textEntryCalories);
        }

        /**
         * Shows a logged food.
         *
         * @param entry the logged food.
         * @param unit  the unit to show energy in.
         */
        public void bind(FoodLogEntry entry, EnergyUnit unit) {
            Context context = itemView.getContext();
            textEntryName.setText(entry.getProductName());
            textEntryPortion.setText(context.getString(R.string.diary_entry_portion,
                    entry.getPortionGrams(), entry.getBrands()));
            textEntryMacros.setText(context.getString(R.string.item_macros,
                    entry.getPortionProtein(), entry.getPortionCarbs(), entry.getPortionFat()));
            textEntryCalories.setText(unit.format(context, entry.getPortionCalories()));
        }
    }
}
