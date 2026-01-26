package com.example.project.pages;

import com.example.project.GameZone;
import com.example.project.items.Product;
import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.CustomComponent.*;
import com.example.project.utilities.ProductManager;
import com.example.project.utilities.SQLController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Objects;

public class ProductDetailPage extends StackPane {
    ProductManager productManager = ProductManager.getManager();
    SQLController sqlController = SQLController.getController();

    public ProductDetailPage(Product product) {
        CustomTextField purchaseAmountDisplay = new CustomTextField("", "integer");
        purchaseAmountDisplay.setText(1 + "");

        CustomButton addToCartButton = new CustomButton("Add To Cart");
        CustomButton plusAmountButton = new CustomButton("+");
        CustomButton minusAmountButton = new CustomButton("-");
        Button returnButton = new Button("<");
        ImageView productImage = product.getProductImage(400,400, productManager.getProductDirectoryPath());
        CustomLabel productName = new CustomLabel(product.getProductName());
        CustomLabel productPrice = new CustomLabel("RM" + product.getProductPrice().toString());
        CustomLabel productTagsLabel = new CustomLabel("Tags");
        CustomLabel productDescription = new CustomLabel(product.getDescription());
        CustomLabel productNoTags = new CustomLabel("This product has no tags");
        CustomLabel quantityLabel = new CustomLabel("QTY: ");
        CustomLabel stockLabel = new CustomLabel("");

        //fill in the applied tags
        ScrollPane productTagsScroller = new ScrollPane();
        FlowPane productTagsContainer = new FlowPane();
        ObservableList<String> tags = product.getProductTags(sqlController); //list of tag fetched from database
        CustomLabel[] productTags = new CustomLabel[tags.size()];
        if (tags.isEmpty()) { //include an custom label indicating no tags
            productTagsScroller.setContent(productNoTags);
        } else {
            //fill in the flowpane with custom label of tag name
            for (int i = 0; i < tags.size(); i++) {
                CustomLabel tagLabel = new CustomLabel(tags.get(i));
                productTagsContainer.getChildren().add(tagLabel);
                productTags[i] = tagLabel;
            }
            productTagsScroller.setContent(productTagsContainer);
        }

        CurrentUser currentUser = CurrentUser.getCurrentUser();
        //if current user cart have product, get the amount in cart, else it is 0
        int currentAmountInCart = currentUser.getCartMap().getOrDefault(product, 0);
        stockLabel.setText("Current stock: " + product.getProductStock() + " (" + currentAmountInCart + " In Cart)");
        //if cart cannot fit more of this product (out of stock), disable the buttons
        if(product.getProductStock() == 0 | currentAmountInCart == product.getProductStock()) {
            addToCartButton.setDisable(true);
            plusAmountButton.setDisable(true);
            minusAmountButton.setDisable(true);
            purchaseAmountDisplay.setDisable(true);
        }

        //execute everytime the quantity text box change
        purchaseAmountDisplay.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, "") || Objects.equals(newValue, "0")) {
                //if empty / 0, set it to 1
                purchaseAmountDisplay.setText("1");
                Platform.runLater(purchaseAmountDisplay::selectAll);
                //if it exceed the available stock, make it the maximum stock
            } else if (Integer.parseInt(newValue) > product.getProductStock() - currentAmountInCart) {
                purchaseAmountDisplay.setText("" + (product.getProductStock() - currentAmountInCart));
            }
        });

        //update cart list held in currentUser class
        addToCartButton.setOnAction(event -> {
            //replace existing item with identical item with different quantity if exist
            int purchaseAmount = Integer.parseInt(purchaseAmountDisplay.getText());
            currentUser.addOrUpdateCartItem(product, currentAmountInCart + purchaseAmount);
            GameZone.showShopPageScene();
        });

        //increment quantity text field by 1 if not larger than stock
        plusAmountButton.setOnAction(event -> {
            int purchaseAmount = Integer.parseInt(purchaseAmountDisplay.getText());
            if (purchaseAmount < (product.getProductStock() - currentAmountInCart)) {
                purchaseAmountDisplay.setText((purchaseAmount + 1) + "");
            }
        });

        //decrement quantity text field by 1 if not lower than 1
        minusAmountButton.setOnAction(event -> {
            int purchaseAmount = Integer.parseInt(purchaseAmountDisplay.getText());
            if (purchaseAmount > 1) {
                purchaseAmountDisplay.setText((purchaseAmount - 1) + "");
            }
        });

        returnButton.setOnAction(event -> {
            GameZone.showShopPageScene();
        });

        //layout
        VBox imageAndStock = new VBox(5, productImage, stockLabel);
        HBox productAmountSelection = new HBox(5, quantityLabel, minusAmountButton, purchaseAmountDisplay, plusAmountButton);
        VBox productDetailsSection = new VBox(5, productName ,productPrice ,productDescription, productTagsLabel, productTagsScroller, productAmountSelection, addToCartButton);
        HBox productAddToCartScreen = new HBox(5, imageAndStock, productDetailsSection);
        VBox fullPage = new VBox(5, returnButton, productAddToCartScreen);

        //styling
        this.getStylesheets().add(getClass().getResource("/css/productPage.css").toExternalForm());
        HBox.setHgrow(productDetailsSection, Priority.ALWAYS);
        HBox.setHgrow(productTagsContainer, Priority.ALWAYS);
        productDescription.setWrapText(true);
        this.getStyleClass().add("root");
        returnButton.getStyleClass().add("return-button");
        productName.getStyleClass().add("product-name");
        productDescription.getStyleClass().add("product-description");
        productPrice.getStyleClass().add("product-price");
        productTagsLabel.getStyleClass().add("product-label");
        quantityLabel.getStyleClass().add("product-label");
        productNoTags.getStyleClass().add("no-tags");
        productTagsScroller.getStyleClass().add("product-tag-scroller");
        addToCartButton.getStyleClass().add("addTo-cart-button");
        plusAmountButton.getStyleClass().add("plus-button");
        minusAmountButton.getStyleClass().add("minus-button");
        purchaseAmountDisplay.getStyleClass().add("purchase-amount-display");
        productAmountSelection.getStyleClass().add("product-amount-selection");
        productDetailsSection.getStyleClass().add("product-details-section");

        if (product.getProductStock() == 0 || product.getProductStock() - currentAmountInCart == 0) {
            stockLabel.getStyleClass().add("out-of-stock");
        } else {
            stockLabel.getStyleClass().add("stock");
        }

        for (CustomLabel tag : productTags) {
            tag.getStyleClass().add("product-tag");
        }

        this.getChildren().add(fullPage);
        returnButton.setOnAction(event -> {
            GameZone.showShopPageScene();});
    }
}
