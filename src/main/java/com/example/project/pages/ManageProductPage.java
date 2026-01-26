package com.example.project.pages;

import com.example.project.GameZone;
import com.example.project.utilities.ProductManager;
import com.example.project.items.Product;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import static com.example.project.utilities.CustomComponent.*;

public class ManageProductPage extends StackPane {
    private static final ProductManager productManager = ProductManager.getManager();

    public ManageProductPage() {
        //clickable
        CustomLabel pageTitle = new CustomLabel("Product Management");
        CustomButton addButton = new CustomButton("Add Product");
        TableView<Product> productTable = new TableView<>();

        //table layout
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");

        nameCol.setCellFactory(param -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    //uses custom label to allow wrap text
                    Product product = getTableRow().getItem();
                    CustomLabel nameLabel = new CustomLabel(product.getProductName());
                    nameLabel.setStyle("-fx-text-fill: white");
                    nameLabel.setWrapText(true);
                    setGraphic(nameLabel);
                }
            }
        });

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price (RM)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("ProductPrice"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("ProductStock"));

        TableColumn<Product, Void> imageCol = new TableColumn<>("Image");
        imageCol.setCellFactory(param -> new TableCell<Product, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    //set image of product as content
                    Product product = getTableRow().getItem();
                    setGraphic(product.getProductImage(130, 130, productManager.getProductDirectoryPath()));
                }
            }
        });

        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> {

            return new TableCell<Product, Void>() {
                //add buttons into column
                final CustomButton updateButton = new CustomButton("Update");
                final CustomButton deleteButton = new CustomButton("Delete");
                final VBox buttonContainer = new VBox(10, updateButton, deleteButton);

                {
                    buttonContainer.setPadding(new Insets(35, 20, 20, 20));
                    updateButton.setOnAction(event -> {
                        Product product = getTableRow().getItem();
                        GameZone.popupManageProductStage(product); //a popup to update the product
                    });

                    deleteButton.setOnAction(event -> {
                        Product product = getTableRow().getItem();
                        productManager.deleteProduct(product); //delete the product
                    });

                    updateButton.getStyleClass().add("action-button");
                    deleteButton.getStyleClass().add("action-button");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonContainer); //put the defined buttons into column
                    }
                }
            };
        });

        productTable.getColumns().addAll(nameCol, priceCol,categoryCol, stockCol, imageCol, actionCol);
        productTable.setItems(productManager.getProductList());
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addButton.setOnAction(event -> {
            GameZone.popupManageProductStage(null);
        });

        AnchorPane addButtonPane = new AnchorPane(addButton);
        AnchorPane.setRightAnchor(addButton, 20.0);
        VBox fullPage = new VBox(15, pageTitle, addButtonPane, productTable);
        VBox.setVgrow(productTable, Priority.ALWAYS);

        //css styling
        this.getStylesheets().add(getClass().getResource("/css/productManagePage.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/css/tableview.css").toExternalForm());
        this.getStyleClass().add("root");
        productTable.getStyleClass().add("product-table-view");
        addButton.getStyleClass().add("action-button");
        pageTitle.getStyleClass().add("page-title");

        this.getChildren().add(fullPage);
    }
}