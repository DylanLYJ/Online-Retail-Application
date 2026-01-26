package com.example.project.pages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.ProductManager;
import com.example.project.utilities.SQLController;
import com.example.project.items.Product;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.project.utilities.CustomComponent.*;

public class CartDisplayPage extends StackPane {
    private static final SQLController sqlController = SQLController.getController();
    private static final ProductManager productManager = ProductManager.getManager();
    private static final CurrentUser currentUser = CurrentUser.getCurrentUser();
    private static final ObservableList<Product> productList = productManager.getProductList();
    private static final ObservableMap<Product, Integer> productQuantityMap = currentUser.getCartMap();

    public CartDisplayPage() {
        ObservableList<Product> cartList = FXCollections.observableArrayList(productQuantityMap.keySet());
        ObservableList<Product> invalidProductList = getInvalidProductList(); //keep track for out-of-stock or removed item in cart
        //button / label
        CustomLabel pageTitle = new CustomLabel("Cart");
        CustomButton purchaseButton = new CustomButton("Purchase");
        CustomLabel totalPriceDisplay = new CustomLabel("");
        CustomLabel noItemInCart = new CustomLabel("No item in your cart");
        noItemInCart.setVisible(false);
        noItemInCart.setManaged(false);

        AtomicReference<Double> purchaseTotal = new AtomicReference<>(0.0);

        ListView<Product> cartViewList = new ListView<>(cartList);
        cartViewList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Set the HBox as display
                    setGraphic(createProductTile(item, invalidProductList));
                }
            }
        });

        MapChangeListener<Product, Integer> totalPriceListener = change -> {
            double total = 0;
            int itemQuantity = 0;
            for (Map.Entry<Product, Integer> entry : productQuantityMap.entrySet()) {
                total += (entry.getValue() * entry.getKey().getProductPrice());
                itemQuantity += entry.getValue();
            }
            purchaseTotal.set(total);
            totalPriceDisplay.setText("Total (" + itemQuantity + "): " + "RM" + String.format("%.2f", total));
        };

        MapChangeListener<Product, Integer> mapSyncListener = change -> {
            Product product = change.getKey();
            if (change.wasRemoved() && !change.wasAdded()) {
                cartList.remove(product);
            } else if (change.wasAdded() && !change.wasRemoved()) {
                cartList.add(product);
            }
        };

        productQuantityMap.addListener(totalPriceListener);
        productQuantityMap.addListener(mapSyncListener);
        totalPriceListener.onChanged(null);  //run once manually

        //if invalid item in cart, do not allow purchase action
        purchaseButton.setDisable(!invalidProductList.isEmpty());
        invalidProductList.addListener((ListChangeListener<Product>) change -> {
            purchaseButton.setDisable(!invalidProductList.isEmpty());
        });

        //actions
        //insert order into sql, then update productList
        purchaseButton.setOnAction(event -> {
            if (!productQuantityMap.isEmpty()) {
                showPurchaseNotification(totalPriceDisplay.getText()).show(purchaseButton.getScene().getWindow());
                LocalDateTime now = LocalDateTime.now();
                String formattedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                sqlController.insertOrder(currentUser.getCurrentUserName(), formattedNow, purchaseTotal.get(), productQuantityMap);
                for (Product product: productQuantityMap.keySet()) {
                    productManager.updateProductStock(product, product.getProductStock() - productQuantityMap.get(product));
                }
                currentUser.removeAllCartItems();
            } else { //show warning for no item to purchase in cart
                if (!noItemInCart.isVisible()) {
                    noItemInCart.setVisible(true);
                    noItemInCart.setManaged(true);
                    PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
                    pause.setOnFinished(event2 -> {
                            noItemInCart.setVisible(false);
                            noItemInCart.setManaged(false);
                });
                    pause.play();
                }
            }
        });

        VBox cartPage = new VBox(pageTitle, cartViewList,  totalPriceDisplay, noItemInCart, purchaseButton);

        //styling
        VBox.setVgrow(cartViewList, Priority.ALWAYS);
        this.getStylesheets().add(getClass().getResource("/css/cartPage.css").toExternalForm());
        this.getStyleClass().add("root");
        pageTitle.getStyleClass().add("page-title");
        cartViewList.getStyleClass().add("cart-list");
        purchaseButton.getStyleClass().add("purchase-button");
        totalPriceDisplay.getStyleClass().add("total-purchase");
        purchaseButton.getStyleClass().add("purchase-box");
        noItemInCart.getStyleClass().add("no-item-warning");


        this.getChildren().add(cartPage);
    }

    public HBox createProductTile(Product product, ObservableList<Product> invalidProductList) {
        ImageView productImage = product.getProductImage(200,200, productManager.getProductDirectoryPath());
        CustomLabel productName = new CustomLabel(product.getProductName());
        CustomLabel productPrice = new CustomLabel("RM" + product.getProductPrice().toString());
        CustomLabel productPriceTotal = new CustomLabel("");

        CustomButton addQuantity = new CustomButton("+");
        CustomButton removeQuantity = new CustomButton("-");
        CustomTextField productQuantity = new CustomTextField("", "integer");
        CustomButton removeFromCartButton = new CustomButton("Remove");

        productQuantity.setText(productQuantityMap.get(product).toString());
        productPriceTotal.setText("RM" +
                String.format("%.2f", Double.parseDouble(
                        product.getProductPrice().toString()) * productQuantityMap.get(product)));

        productQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, "") || Objects.equals(newValue, "0")) {
                productQuantity.setText("1");
                Platform.runLater(productQuantity::selectAll);
            } else if (Integer.parseInt(newValue) > product.getProductStock()) {
                productQuantity.setText("" + product.getProductStock());
            }
            currentUser.addOrUpdateCartItem(product, Integer.parseInt(productQuantity.getText()));
            double productTotal = Double.parseDouble(product.getProductPrice().toString()) * productQuantityMap.get(product);
            productPriceTotal.setText("RM" + String.format("%.2f", productTotal));
        });

        addQuantity.setOnMouseClicked(event -> {
            int currentQuantity = Integer.parseInt(productQuantity.getText());
            if (currentQuantity < product.getProductStock()) {
                productQuantity.setText("" + (currentQuantity + 1));
            }
        });

        removeQuantity.setOnMouseClicked(event -> {
            int currentQuantity = Integer.parseInt(productQuantity.getText());
            if (currentQuantity > 1) {
                productQuantity.setText("" + (currentQuantity - 1));
            }
        });

        removeFromCartButton.setOnMouseClicked(event -> {
            currentUser.removeCartItem(product);
        });

        VBox productDetail = new VBox (productName, productPrice);
        HBox productAction = new HBox (removeFromCartButton ,removeQuantity, productQuantity,addQuantity, productPriceTotal);
        VBox detailAndAction = new VBox(productDetail, productAction);
        HBox fullPage = new HBox(productImage, detailAndAction);

        if (invalidProductList.contains(product)) {
            if (!productList.contains(product)) {
                CustomLabel removeInvalidProduct = new CustomLabel("This item is removed, please remove this item");
                detailAndAction.getChildren().add(removeInvalidProduct);
                removeInvalidProduct.getStyleClass().add("invalid-product");
            } else {
                CustomLabel invalidStock = new CustomLabel("This item have insufficient Stock, please adjust the quantity");
                detailAndAction.getChildren().add(invalidStock);
                invalidStock.getStyleClass().add("invalid-product");

                //if stock returns to sufficient remove this label
                productQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
                    if ((Integer.parseInt(newValue)) <= product.getProductStock()) {
                        detailAndAction.getChildren().remove(invalidStock);
                    }
                });
            }
        }

        //css styling
        productName.setMaxWidth(350);
        productName.setWrapText(true);

        fullPage.getStyleClass().add("product-tile");
        addQuantity.getStyleClass().add("add-quantity-button");
        removeQuantity.getStyleClass().add("remove-quantity-button");
        removeFromCartButton.getStyleClass().add("remove-from-cart-button");
        productQuantity.getStyleClass().add("product-quantity-field");
        productName.getStyleClass().add("product-name");
        productPrice.getStyleClass().add("product-price");
        productPriceTotal.getStyleClass().add("product-total");

        return fullPage;
    }

    public Popup showPurchaseNotification(String totalPriceDisplay) {
        Popup purchaseNotification = new Popup();
        purchaseNotification.setWidth(150);
        purchaseNotification.setHeight(150);
        CustomLabel purchaseLabelTitle = new CustomLabel("✅ Purchase Successful");
        CustomLabel purchaseMessageLabel = new CustomLabel("Your Order Had Been Made.\n" + totalPriceDisplay);
        Button closeNofifButton = new Button("Close");

        VBox notifPanel = new VBox(10, purchaseLabelTitle, purchaseMessageLabel, closeNofifButton);
        purchaseNotification.getContent().add(notifPanel);

        closeNofifButton.setOnAction(event -> {
            purchaseNotification.hide();
        });

        //styling
        notifPanel.getStylesheets().add(getClass().getResource("/css/cartPage.css").toExternalForm());
        notifPanel.getStyleClass().add("notif-panel");
        closeNofifButton.getStyleClass().add("notif-button");
        purchaseLabelTitle.getStyleClass().add("notif-label");
        purchaseMessageLabel.getStyleClass().add("notif-label");

        return purchaseNotification;
    }

    //list of invalid items (either removed / insufficient stock
    public static ObservableList<Product> getInvalidProductList() {
        ObservableList<Product> invalidProductList = FXCollections.observableArrayList();
        for (Product product : productQuantityMap.keySet()) {
            if (productQuantityMap.get(product) > product.getProductStock() || !productList.contains(product)) {
                invalidProductList.add(product);
            }
        }

        //sync with productQuantityMap to update invalidItemList
        MapChangeListener<Product, Integer> syncInvalidProductList = change -> {
            Product product = change.getKey();
            if (change.wasRemoved()) {
                invalidProductList.remove(product);
            } else {
                int newQuantity = change.getValueAdded();
                invalidProductList.remove(product);
                if (newQuantity <= product.getProductStock()) {
                    invalidProductList.remove(product);
                }
            }

        };

        productQuantityMap.addListener(syncInvalidProductList);
        return invalidProductList;
    }

}
