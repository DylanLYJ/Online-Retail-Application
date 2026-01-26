package com.example.project.pages;

import com.example.project.utilities.SQLController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.concurrent.atomic.AtomicReference;

import static com.example.project.utilities.CustomComponent.*;

public class SalesDataPage extends StackPane {
    public final static SQLController sqlController = SQLController.getController();
    private static String currentSaleTableDisplay; //period based or product based data is currently showing

    public SalesDataPage() {
        //labels
        CustomLabel title = new CustomLabel("DashBoard");
        CustomLabel salesAverageLabel = new CustomLabel("Average");
        CustomLabel salesAverageAmountLabel = new CustomLabel("");
        CustomLabel salesSumLabel = new CustomLabel("Sum");
        CustomLabel salesSumAmountLabel = new CustomLabel("");
        CustomLabel highestSalesLabel = new CustomLabel("Highest Sales");
        CustomLabel highestSalesDataLabel = new CustomLabel("");
        CustomLabel lowestSalesLabel = new CustomLabel("Lowest Sales");
        CustomLabel lowestSalesDataLabel = new CustomLabel("");
        CustomLabel startFromDateLabel_productSales = new CustomLabel("Start From");
        CustomLabel endAtDateLabel_productSales = new CustomLabel("End At");
        CustomLabel startFromDateLabel_DateSales = new CustomLabel("Start From");
        CustomLabel endAtDateLabel_DateSales = new CustomLabel("End At");
        CustomLabel periodFormLabel = new CustomLabel("Period: ");
        ComboBox<String> dataDateForm = new ComboBox<>();
        dataDateForm.getItems().addAll("Day", "Month", "Year");
        dataDateForm.getSelectionModel().selectFirst();
        CustomButton productSalesButton = new CustomButton("Product Sales");
        CustomButton dateSalesButton = new CustomButton("Date Sales");
        Button clearDateFilterButton = new Button("Clear Date Filter");

        //inputs
        DatePicker startFromDatePicker_productSales = new DatePicker();
        DatePicker endAtDatePicker_productSales = new DatePicker();
        DatePicker startFromDatePicker_dateSales = new DatePicker();
        DatePicker endAtDatePicker_dateSales = new DatePicker();
        startFromDatePicker_productSales.getEditor().setDisable(true);
        endAtDatePicker_productSales.getEditor().setDisable(true);
        startFromDatePicker_dateSales.getEditor().setDisable(true);
        endAtDatePicker_dateSales.getEditor().setDisable(true);

        //tableviews
        //filter for date (initially no filter)
        AtomicReference<String> startDate = new AtomicReference<>("1000-01-01");
        AtomicReference<String> endDate = new AtomicReference<>("9999-12-31");

        //list & table for sales per a certain period (day, month or year)
        ObservableList<ObservableList<Object>> salesPerTimeData = sqlController.getSalesPerTimeData(dataDateForm.getValue(), startDate.get(), endDate.get());
        TableView<ObservableList<Object>> salesPerTimeTable = new TableView<>();
        String[] columnTitlesDateSales = {"Period", "Total Orders", "Merch (RM)", "Game (RM)", "Hardware (RM)", "Earnings (RM)"};
        for (int i = 0; i < columnTitlesDateSales.length; i++) {
            final int colIndex = i;
            //observable list is fetched in order with the column titles
            TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(columnTitlesDateSales[i]);
            col.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().get(colIndex)));
            salesPerTimeTable.getColumns().add(col);
        }
        salesPerTimeTable.setItems(salesPerTimeData);

        //list & table for sales of each product
        ObservableList<ObservableList<Object>> salesPerProductData = sqlController.getSalesPerProductData(startDate.get(), endDate.get());
        TableView<ObservableList<Object>> salesPerProductTable = new TableView<>();
        String[] columnTitlesProductSales = {"Product", "Total Ordered", "Total Earnings"};
        for (int i = 0; i < columnTitlesProductSales.length; i++) {
            final int colIndex = i;
            //observable list is fetched in order with the column titles
            TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(columnTitlesProductSales[i]);
            col.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().get(colIndex)));
            salesPerProductTable.getColumns().add(col);
        }
        salesPerProductTable.setItems(salesPerProductData);

        //actions
        ListChangeListener<ObservableList<Object>> calculateDataListener =  change -> {
            if (currentSaleTableDisplay != null) { //if table is being displayed
                double sum = 0;
                double count = 0;
                double min = Double.POSITIVE_INFINITY;
                double max = 0;
                String minLabel = "";
                String maxLabel = "";
                int indexToSum = currentSaleTableDisplay.equals("product") ? 2 : 5;

                //use the product data if the current display is product, otherwise use the other
                TableView<ObservableList<Object>> listToCalculate = currentSaleTableDisplay.equals("product") ? salesPerProductTable : salesPerTimeTable;

                //for each row, calculate sum, min, max and average
                for (ObservableList<Object> row : listToCalculate.getItems()) {
                    double totalEarning = Double.parseDouble(row.get(indexToSum).toString());
                    sum += totalEarning;

                    //keep track of the lowest earning
                    if (totalEarning < min) {
                        min = totalEarning;
                        minLabel = row.getFirst().toString();
                    }

                    //keep track of the highest earning
                    if (totalEarning > max) {
                        max = totalEarning;
                        maxLabel = row.getFirst().toString();
                    }
                    count++;
                }

                double average = sum / count;

                //format them and display if table have content (count > 0)
                if (count > 0) {
                    salesAverageAmountLabel.setText(String.format("%.2f", average));
                    salesSumAmountLabel.setText(String.format("%.2f", sum));
                    lowestSalesDataLabel.setText(String.format("%s: %.2f", minLabel, min));
                    highestSalesDataLabel.setText(String.format("%s: %.2f", maxLabel, max));
                } else {
                    salesAverageAmountLabel.setText("No data");
                    salesSumAmountLabel.setText("No data");
                    lowestSalesDataLabel.setText("No data");
                    highestSalesDataLabel.setText("No data");
                }
            }
        };

        //everytime the table content update, change the calculated value
        salesPerTimeData.addListener(calculateDataListener);
        salesPerProductData.addListener(calculateDataListener);

        //get sales data depend on the period, data is fetch from sql each time date detail chagnes
        startFromDatePicker_dateSales.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                startDate.set(newValue.toString());
                salesPerTimeData.setAll(sqlController.getSalesPerTimeData(dataDateForm.getValue(), startDate.get(), endDate.get()));
            }
        });

        endAtDatePicker_dateSales.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                endDate.set(newValue.toString());
                salesPerTimeData.setAll(sqlController.getSalesPerTimeData(dataDateForm.getValue(), startDate.get(), endDate.get()));
            }
        });

        dataDateForm.setOnAction(event -> {
            salesPerTimeData.setAll(sqlController.getSalesPerTimeData(dataDateForm.getValue(), startDate.get(), endDate.get()));
        });

        startFromDatePicker_productSales.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                startDate.set(newValue.toString());
                salesPerProductData.setAll(sqlController.getSalesPerProductData(startDate.get(), endDate.get()));
            }
        });

        endAtDatePicker_productSales.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                endDate.set(newValue.toString());
                salesPerProductData.setAll(sqlController.getSalesPerProductData(startDate.get(), endDate.get()));
            }
        });

        clearDateFilterButton.setOnAction(event -> {
            //reset back to default date
            startDate.set("1000-01-01");
            endDate.set("9999-12-31");
            //reset the input
            endAtDatePicker_dateSales.setValue(null);
            startFromDatePicker_dateSales.setValue(null);
            startFromDatePicker_productSales.setValue(null);
            endAtDatePicker_productSales.setValue(null);
            //update data list
            salesPerProductData.setAll(sqlController.getSalesPerProductData(startDate.get(), endDate.get()));
            salesPerTimeData.setAll(sqlController.getSalesPerTimeData(dataDateForm.getValue(), startDate.get(), endDate.get()));
        });

        //layout
        HBox dateFormFilterBox = new HBox(5, periodFormLabel, dataDateForm);
        VBox productSalesBox = new VBox(5, startFromDateLabel_productSales, startFromDatePicker_productSales, endAtDateLabel_productSales, endAtDatePicker_productSales);
        VBox dateSalesBox = new VBox(5, startFromDateLabel_DateSales, startFromDatePicker_dateSales, endAtDateLabel_DateSales, endAtDatePicker_dateSales, dateFormFilterBox);
        VBox averageCalculatedBox = new VBox(salesAverageLabel, salesAverageAmountLabel);
        VBox sumCalculatedBox = new VBox(salesSumLabel, salesSumAmountLabel);
        VBox minMaxCalculatedBox = new VBox(highestSalesLabel, highestSalesDataLabel, lowestSalesLabel, lowestSalesDataLabel    );
        HBox calculatedDataPane = new HBox(10, averageCalculatedBox, sumCalculatedBox, minMaxCalculatedBox);
        VBox salesDataPane = new VBox();
        VBox filterPane = new VBox(8, title, dateSalesButton, dateSalesBox, productSalesButton, productSalesBox, clearDateFilterButton);
        VBox fullDataGrid = new VBox(15, calculatedDataPane, salesDataPane);
        HBox fullPage = new HBox(20, filterPane, fullDataGrid);
        productSalesBox.setVisible(false);
        productSalesBox.setManaged(false);
        dateSalesBox.setVisible(false);
        dateSalesBox.setManaged(false);

        //select which table to show & their filter option
        //period data (sales based on period of time)
        dateSalesButton.setOnMouseClicked(event -> {
            dateSalesBox.setVisible(true);
            dateSalesBox.setManaged(true);
            productSalesBox.setVisible(false);
            productSalesBox.setManaged(false);

            //if another table is in display, swap it
            if (!(salesDataPane.getChildren().contains(dateSalesButton))) {
                currentSaleTableDisplay = "period"; //swap the current display table indicator
                salesDataPane.getChildren().clear();
                salesDataPane.getChildren().add(salesPerTimeTable);
                calculateDataListener.onChanged(null); //fire the data calculation to update the calculated data

                //add & remove highlighted background
                if (!dateSalesButton.getStyleClass().contains("highlight-background")) {
                    productSalesButton.getStyleClass().remove("highlight-background");
                    dateSalesButton.getStyleClass().add("highlight-background");
                }
            }
        });

        //product data (sales based per product
        productSalesButton.setOnMouseClicked(event -> {
            productSalesBox.setVisible(true);
            productSalesBox.setManaged(true);
            dateSalesBox.setVisible(false);
            dateSalesBox.setManaged(false);

            //if another table is in display, swap it
            if (!(salesDataPane.getChildren().contains(salesPerProductTable))) {
                currentSaleTableDisplay = "product"; //swap the current display table indicator
                salesDataPane.getChildren().clear();
                salesDataPane.getChildren().add(salesPerProductTable);
                calculateDataListener.onChanged(null); //fire the data calculation to update the calculated data

                //add & remove highlighted background
                if (!productSalesButton.getStyleClass().contains("highlight-background")) {
                    productSalesButton.getStyleClass().add("highlight-background");
                    dateSalesButton.getStyleClass().remove("highlight-background");
                }
            }
        });

        //styling
        this.getStylesheets().add(getClass().getResource("/css/tableview.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/css/salesPage.css").toExternalForm());

        CustomLabel[] labels = new CustomLabel[] {
                salesAverageLabel, salesSumLabel,
                highestSalesLabel, lowestSalesLabel,
        };

        CustomLabel[] smallLabels = new CustomLabel[] {
                startFromDateLabel_productSales, endAtDateLabel_productSales,
                startFromDateLabel_DateSales, endAtDateLabel_DateSales
        };

        CustomLabel[] calculatedDataLabels = new CustomLabel[] {
                salesAverageAmountLabel, salesSumAmountLabel, highestSalesDataLabel, lowestSalesDataLabel
        };

        VBox[] calculatedDataPaneList = new VBox[] {
                minMaxCalculatedBox, averageCalculatedBox, sumCalculatedBox
        };

        for (CustomLabel customLabel : labels) {
            customLabel.getStyleClass().add("normal-labels");
        }

        for (CustomLabel customLabel : calculatedDataLabels) {
            customLabel.getStyleClass().add("data-labels");
        }

        for (VBox vBox : calculatedDataPaneList) {
            vBox.getStyleClass().add("calculated-data-pane");
        }

        for (CustomLabel smallLabel : smallLabels) {
            smallLabel.getStyleClass().add("small-labels");
        }

        salesPerProductTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        salesPerTimeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HBox.setHgrow(fullDataGrid, Priority.ALWAYS);
        HBox.setHgrow(salesPerProductTable, Priority.ALWAYS);
        HBox.setHgrow(salesPerTimeTable, Priority.ALWAYS);
        VBox.setVgrow(salesPerProductTable, Priority.ALWAYS);
        VBox.setVgrow(salesPerTimeTable, Priority.ALWAYS);
        VBox.setVgrow(salesDataPane, Priority.ALWAYS);

        this.getStyleClass().add("root");
        filterPane.getStyleClass().add("filter-pane");
        title.getStyleClass().add("page-title");
        dateSalesButton.getStyleClass().add("swap-table-button");
        productSalesButton.getStyleClass().add("swap-table-button");
        dataDateForm.getStyleClass().add("date-form-combobox");
        periodFormLabel.getStyleClass().add("combobox-label");
        clearDateFilterButton.getStyleClass().add("clear-button");

        this.getChildren().add(fullPage);
    }
}
