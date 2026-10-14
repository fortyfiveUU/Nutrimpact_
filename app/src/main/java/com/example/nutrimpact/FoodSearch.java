package com.example.nutrimpact;
import java.util.ArrayList;
import java.util.List;
public class FoodSearch {

    private List<FoodProducts> products;
    /**
     Constructor for FoodSearch
     **/
    public FoodSearch(){
        this.products = new ArrayList<>();
    }

    public List<FoodProducts> getProducts() {
        return products;
    }
    public void setProducts(List<FoodProducts> products) {
        this.products = products;
    }
}
