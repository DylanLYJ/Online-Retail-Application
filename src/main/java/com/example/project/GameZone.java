package com.example.project;

import com.example.project.items.Product;
import com.example.project.pages.*;
import com.example.project.utilities.SQLController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameZone extends Application {
    private static Stage stage;
    private static BorderPane mainPane;

    //before starting the program, show this first to authenticate
    public static void showLoginPageScene() {
        stage.setMaximized(false);
        LoginPage loginPage = new LoginPage();
        Scene loginScene = new Scene(loginPage, 1250, 650);
        stage.setScene(loginScene);

        stage.setMaximized(true);
        stage.setMinHeight(660);
        stage.setMinWidth(1270);
    }

    //put the mainPane (a borderpane) into the stage
    public static void launchMainApplication() {
        stage.setMaximized(false);
        mainPane = new BorderPane();
        VBox sideBar = new ApplicationSideBar();
        mainPane.setLeft(sideBar);
        mainPane.setCenter(new ShopPage()); // default page

        Scene mainScene = new Scene(mainPane, 1250, 650);
        stage.setScene(mainScene);
        stage.setMaximized(true);
        stage.setMinHeight(610);
        stage.setMinWidth(1270);
    }

    //swap center of borderpane to switch between contents
    public static void showShopPageScene() {
        ShopPage shopPage = new ShopPage();
        mainPane.setCenter(shopPage);

    }

    public static void showProductPageScene(Product product) {
        ProductDetailPage  productDetailPage = new ProductDetailPage(product);
        mainPane.setCenter(productDetailPage);
    }

    public static void showManageProductScene() {
        ManageProductPage productPage = new ManageProductPage();
        mainPane.setCenter(productPage);
    }

    public static void showManageUserScene() {
        ManageUserPage userPage = new ManageUserPage();
        mainPane.setCenter(userPage);
    }

    public static void showSalesDataScene() {
        SalesDataPage salesDataPage = new SalesDataPage();
        mainPane.setCenter(salesDataPage);
    }

    public static void showCartDisplayScene() {
        CartDisplayPage cartDisplayPage = new CartDisplayPage();
        mainPane.setCenter(cartDisplayPage);
    }

    public static void popupManageProductStage(Product product) {
        ManageProductPopupPage manageProductPopupPage = new ManageProductPopupPage(product);
        mainPane.setCenter(manageProductPopupPage);
    }

    public static void showOrderHistory() {
        OrderHistoryPage orderHistoryPage = new OrderHistoryPage();
        mainPane.setCenter(orderHistoryPage);
    }

    @Override
    public void start(Stage stage) {
        GameZone.stage = stage;
        showLoginPageScene();
        stage.setTitle("GameZone");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
