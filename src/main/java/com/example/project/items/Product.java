package com.example.project.items;

import com.example.project.utilities.SQLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Product {
    private String productName;
    private Double productPrice;
    private String productImageName;
    private String category;
    private int stock;
    private String description;
    private ObservableList<String> productTags = FXCollections.observableArrayList();

    public Product(String productName, Double productPrice, String category ,String productImageName, int stock, String description, ObservableList<String> productTags) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImageName = productImageName;
        this.category = category;
        this.stock = stock;
        this.description = description;
        this.productTags = productTags;
    }

    //getters
    public String getProductName() { return productName; }
    public Double getProductPrice() { return productPrice; }
    public String getCategory() { return category; }
    public String getProductImageName() { return productImageName; }
    public Integer getProductStock() { return stock; }
    public String getDescription() { return description; }

    public ObservableList<String> getProductTags(SQLController sqlController) {
        //remove tag if dont exist
        ObservableList<String> tags = sqlController.getTags(getCategory());
        productTags.removeIf(tag -> !tags.contains(tag));
        return productTags;
    }

    public ImageView getProductImage(int width, int height, String productImagePath) {
        ImageView productImage;
        try {
            productImage = new ImageView(new Image("file:" + productImagePath + productImageName));
        } catch (Exception e) {
            productImage = new ImageView(new Image("file:" + productImagePath + "noImage.jpg"));
        }
        productImage.setFitWidth(width);
        productImage.setFitHeight(height);
        return productImage;
    }

    public ImageView getProductImage_multiThread(int width, int height, String productImagePath) {
        ImageView productImage = new ImageView();

        Task<Image> loadImageTask = new Task<>() { //run loading image on another thread
            @Override
            protected Image call() throws Exception {
                try {
                    return new Image("file:" + productImagePath + productImageName);
                } catch (Exception e) {
                    return new Image("file:" + productImagePath + "noImage.jpg"); //if image deleted from file
                }
            }
        };
        loadImageTask.setOnSucceeded(e -> productImage.setImage(loadImageTask.getValue()));
        productImage.setCache(true);
        productImage.setFitWidth(width);
        productImage.setFitHeight(height);

        Thread thread = new Thread(loadImageTask);
        thread.start();

        return productImage;
    }


    //setters
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }
    public void setCategory(String category) { this.category = category; }
    public void setProductImageName(String productImageName) { this.productImageName = productImageName; }
    public void setProductStock(Integer stock) { this.stock = stock; }
    public void setDescription(String description) { this.description = description; }
    public void setProductTags(ObservableList<String> productTags) { this.productTags = productTags; }
}
