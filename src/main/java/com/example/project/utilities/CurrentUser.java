package com.example.project.utilities;

import com.example.project.items.Product;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import java.util.Map;

public class CurrentUser {
    private static CurrentUser currentUser;
    private static String currentUserName;
    private static Boolean isAdmin;
    private final static ObservableMap<Product, Integer> cartMap = FXCollections.observableHashMap();

    private static final ProductManager productManager = ProductManager.getManager();
    private static final SQLController sqlController = SQLController.getController();

    private CurrentUser(String username, boolean isAdmin) {
        CurrentUser.currentUserName = username;
        CurrentUser.isAdmin = isAdmin;
        cartMap.clear();

        //Convert productName Map into Product Object Map
        Map<String, Integer> unConvertedCartList = sqlController.getCartList(username);
        for (Map.Entry<String, Integer> entry : unConvertedCartList.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();

            //insert into cartList if product name string = product object name
            for (Product product : productManager.getProductList()) {
                if (product.getProductName().equals(productName)) {
                    cartMap.put(product, quantity);
                    break;
                }
            }
        }

    }

    public static CurrentUser getCurrentUser() { return currentUser; }
    public String getCurrentUserName() { return currentUserName; }
    public boolean isUserAdmin() { return isAdmin; }
    public ObservableMap<Product, Integer> getCartMap() { return cartMap; }

    public void addOrUpdateCartItem(Product product, int quantity) {
        cartMap.put(product, quantity);
        sqlController.insertAndUpdateCartProduct(getCurrentUserName(), product.getProductName(), quantity);
    }

    public void removeCartItem(Product product) {
        cartMap.remove(product);
        sqlController.deleteCartProduct(getCurrentUserName(), product.getProductName());
    }

    public void removeAllCartItems() {
        cartMap.clear();
        sqlController.deleteAllUserCart(getCurrentUserName());
    }

    public static void initializeCurrentUser(String username, boolean isAdmin) {
        if (currentUser != null) {
            currentUser = null;
        }
        currentUser = new CurrentUser(username, isAdmin);

    }
}
