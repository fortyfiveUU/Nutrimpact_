package com.example.nutrimpact;

public class FoodProducts {


    private String productName;
    private String brands;
    private Nutriments nutriments;

    public FoodProduct() {
    }

    /**
     Constructor
     **/
    public String getProductName(){
        /**
         Retrieves product name
          @return the name of the product
         **/
        public String getProductName() {
            return (productName != null) ? productName : "Product not listed";
        }
        public void setProductName(String productName) {
            this.productName = productName;
        }
        public String getBrands() {
            return (brands != null) ? brands : "Brand not listed";
        }
        public void setBrands(String brands) {
            this.brands = brands;
        }
        public Nutriments getNutriments() {
            return nutriments;
        }
        public void setNutriments(Nutriments nutriments) {
            this.nutriments = nutriments;
        }
        /**
         Assigns the nutritional breakdown.
         **/
        public void setNutriments(Nutriments nutriments) {
            this.nutriments = nutriments;
        }
    }

}




}
