package com.example.project.utilities;

import com.example.project.items.Product;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProductManager {
    private static final SQLController sqlController = SQLController.getController();
    private static final ProductManager productManager = new ProductManager();
    private final File productImageDirectory;
    private final ObservableList<Product> productList; //an updating list of all product and its detail

    private ProductManager() {
        productList = sqlController.getAllProducts();

        //create image directory if doesnt exist
        productImageDirectory = new File(System.getProperty("user.dir"), "productImages");
        if (!productImageDirectory.exists()) {
            productImageDirectory.mkdirs();
        }
    }

    public static ProductManager getManager() { return productManager; }
    public ObservableList<Product> getProductList() { return productList; }
    public String getProductDirectoryPath() { return (productImageDirectory.getAbsolutePath() + File.separator); }

    //update the product list and sql
    public void addProduct(String productName, double productPrice, String productCategory, String productImageName, int productStock, String productDescription, ObservableList<String> productTags) {
        Product product = new Product(productName, productPrice, productCategory, productImageName, productStock, productDescription, productTags);
        productList.add(product);
        sqlController.insertProduct(productName, productPrice, productCategory, productImageName, productStock, productDescription);
        sqlController.insertAndUpdateProductTag(productName, productCategory, productTags);
    }

    //add image to directory
    public void addProductImage(String productImageName, File selectedImage) {
        Path productImagePath = Paths.get(productImageDirectory.getAbsolutePath(), String.valueOf(productImageName));
        try {
            Files.copy(selectedImage.toPath(), productImagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("No image found in file, replacing with noImage.jpg");
            //if image no longer exist, replace with noImage.jpg
            for (Product product : productList) {
                if (product.getProductImageName().equals(productImagePath.getFileName().toString())) {
                    product.setProductImageName("noImage.jpg");
                    sqlController.updateProduct(product.getProductName(), product.getProductName(), product.getProductPrice(), product.getCategory(), product.getProductImageName(), product.getProductStock(), product.getDescription());
                }
            }
        }
    }

    //update product list and sql
    public void updateProduct(Product product, String productName, double productPrice, String productCategory, String productImageName, int productStock, String productDescription, ObservableList<String> appliedTags) {
        int productIndex = productList.indexOf(product);
        String productOriginalName = productList.get(productIndex).getProductName();
        productList.set(productIndex, product);
        productList.get(productIndex).setProductName(productName);
        productList.get(productIndex).setProductPrice(productPrice);
        productList.get(productIndex).setCategory(productCategory);
        productList.get(productIndex).setProductImageName(productImageName);
        productList.get(productIndex).setProductStock(productStock);
        productList.get(productIndex).setDescription(productDescription);
        productList.get(productIndex).setProductTags(appliedTags);
        sqlController.updateProduct(productOriginalName, productName, productPrice, productCategory, productImageName, productStock, productDescription);
        sqlController.insertAndUpdateProductTag(productName, productCategory, appliedTags);
        //
    }

    //delete product from list and sql
    public void deleteProduct(Product product) {
        String productName = product.getProductName();
        productList.remove(product);
        sqlController.deleteProduct(productName);
    }

    //update stock in list and sql
    public void updateProductStock (Product product, int stock) {
        productList.get(productList.indexOf(product)).setProductStock(stock);
        sqlController.updateProductStock(product.getProductName(), stock);
    }


}
