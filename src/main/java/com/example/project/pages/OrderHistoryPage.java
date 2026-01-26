package com.example.project.pages;

import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.SQLController;
import com.example.project.items.Order;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import static com.example.project.utilities.CustomComponent.*;

public class OrderHistoryPage extends StackPane{
    private static final CurrentUser currentUser = CurrentUser.getCurrentUser();
    private static final SQLController sqlController = SQLController.getController();

    public OrderHistoryPage() {
        //Display table page layouts
        //UI components
        CustomLabel pageTitle = new CustomLabel("Order History");
        TableView<Order> orderTable = new TableView<>();

        //table layout
        TableColumn<Order, String> IDCol = new TableColumn<>("Order ID");
        IDCol.setCellValueFactory(new PropertyValueFactory<>("orderID"));

        TableColumn<Order, LocalDateTime> dateCol = new TableColumn<>("Order Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        dateCol.setCellFactory(param -> new TableCell<Order, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); }
            }
        });

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("orderTotal"));

        orderTable.setRowFactory(param -> {
            TableRow<Order> row = new TableRow<>();
            //create a popup when clicked
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    //dynamically create the popup based on current screen
                    double width = orderTable.getScene().getWindow().getWidth();
                    double height = orderTable.getScene().getWindow().getHeight();
                    double x =  orderTable.getScene().getWindow().getX();
                    double y =  orderTable.getScene().getWindow().getY();

                    showOrderDetailsPopup(row.getItem(), width, height).show(orderTable.getScene().getWindow(), x, y);
                }
            });
            return row;
        });

        orderTable.getColumns().addAll(IDCol, dateCol, totalCol);
        System.out.println(currentUser.getCurrentUserName());
        orderTable.setItems(sqlController.getAllOrders(currentUser.getCurrentUserName()));
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //layout
        VBox pageModifyButtons = new VBox(pageTitle);
        VBox fullPage = new VBox(pageModifyButtons, orderTable);
        this.getChildren().add(fullPage);

        VBox.setVgrow(orderTable, Priority.ALWAYS);

        //styling
        this.getStylesheets().add(getClass().getResource("/css/orderHistoryPage.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/css/tableview.css").toExternalForm());
        this.getStyleClass().add("root");
        pageTitle.getStyleClass().add("page-title");



        }

    public Popup showOrderDetailsPopup(Order order, double stageWidth, double stageHeight) {
        //layouts
        Popup orderPage = new Popup();
        TableView<String> orderItemsTable = new TableView<>();
        CustomLabel returnButton = new CustomLabel("Close");
        CustomLabel orderTitle = new CustomLabel("Order " + order.getOrderID());
        CustomLabel orderDate = new CustomLabel("Order Date: " + order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        CustomLabel orderTotal = new CustomLabel("Total: " + order.getOrderTotal());

        Rectangle overlayRectangle = new Rectangle();
        overlayRectangle.setFill(Color.BLACK);
        overlayRectangle.setOpacity(0.5);
        overlayRectangle.setWidth(stageWidth);
        overlayRectangle.setHeight(stageHeight);

        TableColumn<String, CustomLabel> orderItemNameCol = new TableColumn<>("Order Item");
        orderItemNameCol.setCellValueFactory( entry -> {
            //use custom label to allow text wrapping
           CustomLabel productName = new CustomLabel(entry.getValue());
            productName.setStyle("-fx-text-fill: white;");
            return new SimpleObjectProperty<>(productName);
        });

        TableColumn<String, String> productQuantityCol = new TableColumn<>("Quantity");
        productQuantityCol.setCellValueFactory( entry -> new SimpleStringProperty(order.getQuantityForProduct(entry.getValue())));

        //price for each product: price /quantity
        TableColumn<String, String> pricePerProduct = new TableColumn<>("Price Per Product");
        pricePerProduct.setCellValueFactory(entry -> {
            String price = String.valueOf(Double.parseDouble(order.getPriceForProduct(entry.getValue())) / Double.parseDouble(order.getQuantityForProduct(entry.getValue())));
            return new SimpleStringProperty(price);
        });

        TableColumn<String, String> productTotalPriceCol = new TableColumn<>("Total of Product");
        productTotalPriceCol.setCellValueFactory(entry -> new SimpleStringProperty(order.getPriceForProduct(entry.getValue())));

        orderItemsTable.setItems(order.getOrderItems());
        orderItemsTable.getColumns().addAll(orderItemNameCol, productQuantityCol, pricePerProduct, productTotalPriceCol);
        orderItemsTable.sort();

        returnButton.setOnMouseClicked(event -> {
            orderPage.hide();
        });

        VBox orderItemsSection = new VBox(orderItemsTable);
        HBox orderTotalSection = new HBox(orderTotal);
        VBox layoutContainer = new VBox(orderTitle, orderDate, orderItemsSection, orderTotalSection, returnButton);

        orderPage.getContent().add(overlayRectangle);
        orderPage.getContent().add(layoutContainer);
        orderPage.setAutoHide(true);

        //styling
        orderItemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        layoutContainer.setTranslateX(stageWidth / 3.8);
        layoutContainer.setTranslateY(stageHeight / 10);
        VBox.setVgrow(orderItemsTable, Priority.ALWAYS);

        layoutContainer.getStylesheets().add(getClass().getResource("/css/tableview.css").toExternalForm());
        layoutContainer.getStylesheets().add(getClass().getResource("/css/orderHistoryPage.css").toExternalForm());
        layoutContainer.getStyleClass().add("popup");
        returnButton.getStyleClass().add("return-button");
        orderTotal.getStyleClass().add("display-label");
        orderTitle.getStyleClass().add("popup-title");
        orderDate.getStyleClass().add("display-label");


        return orderPage;
    }
}

