package com.example.project.items;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Order {
    private final String orderID;
    private final LocalDateTime orderDate;
    private final Double orderTotal;
    private final Map<String, List<String>> orderItems; //product name as key, quantity and price as value

    public Order(String orderID, String orderDate, Double orderTotal, Map<String, List<String>> orderItems) {
        this.orderID = orderID;
        this.orderDate = LocalDateTime.parse(orderDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.orderTotal = orderTotal;
        this.orderItems = orderItems;
    }

    public String getOrderID() { return orderID; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public Double getOrderTotal() { return orderTotal; }

    public ObservableList<String> getOrderItems() {
        return FXCollections.observableArrayList(orderItems.keySet());
    }

    public String getQuantityForProduct(String productName) {
        List<String> itemDetails = orderItems.get(productName);
        return itemDetails.getFirst();
    }

    public String getPriceForProduct (String productName){
        List<String> itemDetails = orderItems.get(productName);
        return itemDetails.get(1);

    }
}
