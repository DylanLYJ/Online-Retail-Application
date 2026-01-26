package com.example.project.pages;

import com.example.project.GameZone;
import com.example.project.items.Product;
import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.CustomComponent.*;
import javafx.animation.*;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Map;

public class ApplicationSideBar extends VBox {
    CurrentUser currentUser = CurrentUser.getCurrentUser();

    public ApplicationSideBar() {
        CustomButton adminControlButton = new CustomButton("Admin panel");
        CustomButton userButton = new CustomButton(currentUser.getCurrentUserName());
        CustomButton storeButton = new CustomButton("Store");
        CustomButton cartButton = new CustomButton("");
        CustomButton manageProductButton = new CustomButton("Manage Products");
        CustomButton manageSalesButton = new CustomButton("Sales");
        CustomButton manageAdminButton = new CustomButton("Manage Admins");
        ContextMenu showUserPane = new ContextMenu();
        MenuItem orderHistoryButton = new MenuItem("Order History");
        MenuItem logOutButton = new MenuItem("Log Out");
        showUserPane.getItems().addAll(orderHistoryButton, logOutButton);
        Button hideSideBarButton = new Button("«");

        //gather of sidebar button that swap section
        CustomButton[] swap_buttons = {storeButton, cartButton, manageProductButton, manageSalesButton, manageAdminButton};

        if (!currentUser.isUserAdmin()) {
            adminControlButton.setVisible(false);
            adminControlButton.setManaged(false);
        }

        VBox adminPanel = new VBox(5, manageProductButton, manageAdminButton, manageSalesButton);
        adminPanel.setVisible(false);
        adminPanel.setManaged(false);

        //swap center of a borderpane (swap between screens)
        manageProductButton.setOnAction(mouseEvent -> {
            setFocusOnButton(swap_buttons, manageProductButton, true);
            GameZone.showManageProductScene();
        });

        orderHistoryButton.setOnAction(mouseEvent -> {
            setFocusOnButton(swap_buttons, null, false);
            GameZone.showOrderHistory();
        });

        manageSalesButton.setOnAction(mouseEvent -> {
            setFocusOnButton(swap_buttons, manageSalesButton, true);
            GameZone.showSalesDataScene();
        });

        cartButton.setOnMouseClicked(mouseEvent -> {
            setFocusOnButton(swap_buttons, cartButton, false);
            GameZone.showCartDisplayScene();
        });

        storeButton.setOnMouseClicked(mouseEvent -> {
            GameZone.showShopPageScene();
            setFocusOnButton(swap_buttons, storeButton, false);

        });
        manageAdminButton.setOnAction(mouseEvent -> {
            setFocusOnButton(swap_buttons, manageAdminButton, true);
            GameZone.showManageUserScene();
        });

        logOutButton.setOnAction(mouseEvent -> {
            GameZone.showLoginPageScene();
        });

        //open the admin selection
        adminControlButton.setOnMouseClicked(mouseEvent -> {
            adminPanel.setVisible(!adminPanel.isVisible());
            adminPanel.setManaged(!adminPanel.isManaged());
        });

        //open user selection
        userButton.setOnMouseClicked(mouseEvent -> {
            Bounds positionTracker = userButton.localToScreen(userButton.getBoundsInLocal());
            double x = positionTracker.getMinX()+30;
            double y = positionTracker.getMaxY()+10;
            showUserPane.show(userButton, x, y);
        });

        //a map that keep track of cart item
        //productQuantityMap consist of <product in cart, its quantity in cart>
        ObservableMap<Product, Integer> productQuantityMap = currentUser.getCartMap();
        MapChangeListener<Product, Integer> cartTotalPriceListener = change -> {
            int total = 0;
            for (Map.Entry<Product, Integer> entry : productQuantityMap.entrySet()) {
                total += (entry.getValue());
            }
            cartButton.setText("Cart (" + total + ")");
        };
        productQuantityMap.addListener(cartTotalPriceListener);
        cartTotalPriceListener.onChanged(null); //run once manually


        //layout
        VBox hideButtonWrapper = new VBox(hideSideBarButton);
        VBox sideBarContent = new VBox(userButton, storeButton, adminControlButton, adminPanel,cartButton);
        HBox fullBar = new HBox(sideBarContent, hideButtonWrapper);
        this.getChildren().addAll(fullBar);

        //animation to hide sidebar
        hideSideBarButton.setOnAction(mouseEvent -> {
            boolean isVisible = sideBarContent.isVisible();

            if (isVisible) {
                //slide the buttons away
                TranslateTransition translateTransition = new TranslateTransition(Duration.millis(400), sideBarContent);
                translateTransition.setFromX(0);
                translateTransition.setToX(-270);

                //change the width of the sidebar in a period
                KeyValue widthValue = new KeyValue(this.prefWidthProperty(), 20);
                KeyFrame widthFrame = new KeyFrame(Duration.millis(500), widthValue);
                Timeline widthTimeline = new Timeline(widthFrame);

                //play animation
                ParallelTransition parallelTransition = new ParallelTransition(translateTransition, widthTimeline);
                parallelTransition.play();

                //swap visibility
                translateTransition.setOnFinished(event -> {
                    sideBarContent.setVisible(false);
                    sideBarContent.setManaged(false);
                });

                parallelTransition.setOnFinished(event -> {
                    hideSideBarButton.setText("»");
                });

            }  else {
                //slide the buttons back in
                TranslateTransition translateTransition = new TranslateTransition(Duration.millis(650), sideBarContent);
                translateTransition.setFromX(-270);
                translateTransition.setToX(0);

                //swap visibility 100ms into the animation
                KeyFrame sideBarAppearFrame = new KeyFrame(Duration.millis(100), e -> {
                    sideBarContent.setVisible(true);
                    sideBarContent.setManaged(true);
                });

                //change the width of the sidebar in a period
                KeyValue widthValue = new KeyValue(this.prefWidthProperty(), 270);
                KeyFrame widthFrame = new KeyFrame(Duration.millis(500), widthValue);
                Timeline widthTimeline = new Timeline(widthFrame, sideBarAppearFrame);

                ParallelTransition parallelTransition = new ParallelTransition(translateTransition, widthTimeline);
                parallelTransition.play();

                parallelTransition.setOnFinished(event -> {
                    hideSideBarButton.setText("«");
                });
            }
        });

        //Styling
        this.getStylesheets().add(getClass().getResource("/css/sideBar.css").toExternalForm());
        HBox.setHgrow(sideBarContent, Priority.ALWAYS);
        VBox.setVgrow(hideButtonWrapper, Priority.ALWAYS);
        this.getStyleClass().add("root");
        adminPanel.setId("admin-panel");
        showUserPane.setId("user-panel");
        hideSideBarButton.getStyleClass().add("hide-SideBarButton");

        CustomButton[] sideBarMainButton = {userButton, storeButton, adminControlButton, cartButton};
        for (CustomButton button : sideBarMainButton) {
            button.getStyleClass().add("main-button");
        }

        CustomButton[] adminButtons = {manageProductButton, manageSalesButton, manageAdminButton};
        for (CustomButton button : adminButtons) {
            button.getStyleClass().add("admin-button");
        }

        MenuItem[] userMenuItems = {orderHistoryButton, logOutButton};
        for (MenuItem menuItem : userMenuItems) {
            menuItem.getStyleClass().add("menu-item");
        }
    }

    //set highlight background for buttons based on current pane
    public static void setFocusOnButton(CustomButton[] buttons, CustomButton selectedButton, boolean padding) {
        for (CustomButton button : buttons) {
            //padding is for admin buttons which have different spacing
            if (!padding) {
                button.getStyleClass().remove("highlight-background");
            } else {
                button.getStyleClass().remove("highlight-background-padding");
            }
        }

        //skip if no button needed to be highlight
        if (selectedButton != null) {
            if (!padding) {
                selectedButton.getStyleClass().add("highlight-background");
            } else {
                selectedButton.getStyleClass().add("highlight-background-padding");
            }
        }
    }
}
