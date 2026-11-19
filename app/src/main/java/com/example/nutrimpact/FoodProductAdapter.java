/**
 * Module: EEE521 - Final Year Project
 * Project: Nutrimpact
 * File: FoodProductAdapter.java
 * Description: RecyclerView adapter that shows Open Food Facts search results as cards with the product name, brand, calories and macros per 100g.
 */

package com.example.nutrimpact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds a list of FoodProduct objects to item_food_product cards.
 */
public class FoodProductAdapter extends RecyclerView.Adapter<FoodProductAdapter.FoodProductViewHolder> {

    /**
     * Receives taps on a product card.
     */
    public interface OnProductClickListener {

        /**
         * Called when the user taps a product.
         *
         * @param product the product that was tapped.
         */
        void onProductClick(FoodProduct product);
    }

    /** Products currently shown in the list. */
    private final List<FoodProduct> products = new ArrayList<>();

    /** Receives card taps. */
    private final OnProductClickListener clickListener;

    /** Unit energy is shown in. */
    private EnergyUnit energyUnit = EnergyUnit.KCAL;

    /**
     * Creates an adapter with an empty list.
     *
     * @param clickListener called when a product card is tapped.
     */
    public FoodProductAdapter(OnProductClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Replaces the products shown in the list and refreshes the display. Each
     * search is a completely new list, so the old rows are removed and the new
     * ones inserted, rather than redrawing everything with notifyDataSetChanged().
     *
     * @param newProducts the products to show; null clears the list.
     */
    public void setProducts(List<FoodProduct> newProducts) {
        int oldCount = products.size();
        products.clear();
        if (oldCount > 0) {
            notifyItemRangeRemoved(0, oldCount);
        }
        if (newProducts != null && !newProducts.isEmpty()) {
            products.addAll(newProducts);
            notifyItemRangeInserted(0, products.size());
        }
    }

    /**
     * Sets the unit energy is shown in, redrawing the cards if it changed.
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
     * Creates a new card view for a product.
     *
     * @param parent   the RecyclerView the card will be added to.
     * @param viewType the view type (only one type is used).
     * @return a ViewHolder wrapping the new card.
     */
    @NonNull
    @Override
    public FoodProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_product, parent, false);
        return new FoodProductViewHolder(view);
    }

    /**
     * Fills a card with the product at the given position.
     *
     * @param holder   the card to fill.
     * @param position the product's position in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull FoodProductViewHolder holder, int position) {
        FoodProduct product = products.get(position);
        holder.bind(product, energyUnit);
        holder.itemView.setOnClickListener(view -> clickListener.onProductClick(product));
    }

    /**
     * Gets the number of products in the list.
     *
     * @return the product count.
     */
    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * Holds the views for one product card so they aren't looked up on every scroll.
     */
    public static class FoodProductViewHolder extends RecyclerView.ViewHolder {

        /** Shows the product name. */
        private final TextView textProductName;

        /** Shows the brand names. */
        private final TextView textBrand;

        /** Shows calories per 100g. */
        private final TextView textCalories;

        /** Shows protein, carbs and fat per 100g. */
        private final TextView textMacros;

        /**
         * Finds the card's text views.
         *
         * @param itemView the inflated card layout.
         */
        public FoodProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            textBrand = itemView.findViewById(R.id.textBrand);
            textCalories = itemView.findViewById(R.id.textCalories);
            textMacros = itemView.findViewById(R.id.textMacros);
        }

        /**
         * Shows a product's details in this card. Products with no nutrition
         * data show a message instead of misleading zero values.
         *
         * @param product the product to show.
         * @param unit    the unit to show energy in.
         */
        public void bind(FoodProduct product, EnergyUnit unit) {
            Context context = itemView.getContext();
            Nutriments nutriments = product.getNutriments();

            textProductName.setText(product.getProductName());
            textBrand.setText(product.getBrands());

            if (nutriments.hasNutritionData()) {
                textCalories.setText(context.getString(R.string.item_calories,
                        unit.format(context, nutriments.getCaloriesPer100g())));
                textMacros.setText(context.getString(R.string.item_macros,
                        nutriments.getProteinPer100g(),
                        nutriments.getCarbsPer100g(),
                        nutriments.getFatPer100g()));
                textMacros.setVisibility(View.VISIBLE);
            } else {
                textCalories.setText(R.string.item_no_nutrition);
                textMacros.setVisibility(View.GONE);
            }
        }
    }
}
