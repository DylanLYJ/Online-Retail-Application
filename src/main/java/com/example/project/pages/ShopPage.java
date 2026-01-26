package com.example.project.pages;

import com.example.project.GameZone;
import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.ProductManager;
import com.example.project.items.Product;
import com.example.project.utilities.SQLController;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.example.project.utilities.CustomComponent.*;

public class ShopPage extends StackPane {
    private static final ProductManager productManager = ProductManager.getManager();
    private static final SQLController sqlController = SQLController.getController();
    //a list of tags to filter the product
    private static final ObservableList<String> selectedTags = FXCollections.observableArrayList();
    private static final CurrentUser currentUser = CurrentUser.getCurrentUser();

    //initial default filters that will be modified
    private static final AtomicReference<Predicate<Product>> tagRestriction = new AtomicReference<>(product -> true);
    private static final AtomicReference<Predicate<Product>> categoryRestriction = new AtomicReference<>(product -> true);
    private static final AtomicReference<Predicate<Product>> searchRestriction = new AtomicReference<>(product -> true);
    private static final AtomicReference<Predicate<Product>> minPriceRestriction = new AtomicReference<>(product -> true);
    private static final AtomicReference<Predicate<Product>> maxPriceRestriction = new AtomicReference<>(product -> true);

    private static final ObservableList<Product> productList = productManager.getProductList(); //active list of current product
    private static final FilteredList<Product> filteredList = new FilteredList<>(productList); //filtered based on predicate above

    public ShopPage() {
        //Label
        CustomLabel currentProductCategory = new CustomLabel("All Products");
        CustomLabel priceFilterSeperator = new CustomLabel(" - ");

        //Clickable / input
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All Products", "Merchandise", "Game", "Hardware");
        CustomTextField minPriceFilter = new CustomTextField("min price", "double");
        CustomTextField maxPriceFilter = new CustomTextField("max price", "double");
        CustomTextField searchBar = new CustomTextField("search");
        CustomButton openTagFilterButton = new CustomButton("Tag Filter");

        //layout
        HBox priceFilterSectionBox = new HBox(minPriceFilter, priceFilterSeperator , maxPriceFilter);
        TilePane productContainer = new TilePane();
        ScrollPane productScroll = new ScrollPane(productContainer);
        ScrollPane tagFilterContainerScroll = new ScrollPane();
        HBox topSectionBox = new HBox(searchBar, categoryFilter, openTagFilterButton, priceFilterSectionBox);
        VBox fullPage = new VBox(5, currentProductCategory, topSectionBox, tagFilterContainerScroll, productScroll);

        //constraint and configuration
        categoryFilter.setValue("All Products");
        tagFilterContainerScroll.setVisible(false);
        tagFilterContainerScroll.setManaged(false);

        //initiate display for products
        //if list of product change, clear and refill the tilePane (product Container)
        ListChangeListener<Product> filterListener = change -> {
            productContainer.getChildren().clear();
            filteredList.forEach(product -> productContainer.getChildren().add(createProductTile(product)));
        };
        filteredList.addListener(filterListener);
        filterListener.onChanged(null); //run once manually

        //if the tags selected change, update the product predicate (filter condition)
        selectedTags.addListener((ListChangeListener<String>) change -> {
            tagRestriction.set(product -> new HashSet<>(product.getProductTags(sqlController)).containsAll(selectedTags));
            refreshFilteredList();
        });

        //whenever category update, update the tag list of the scrollpane & flowpane too (tags are different across categories)
        ChangeListener<String> tagListUpdateListener = (observable, oldValue, newValue) -> {
            String filter = "All Products".equals(newValue) ? null : newValue;
            selectedTags.clear();
            tagFilterContainerScroll.setContent(null);
            tagFilterContainerScroll.setContent(createTagFilterSelection(filter));
        };

        categoryFilter.valueProperty().addListener(tagListUpdateListener);
        tagListUpdateListener.changed(categoryFilter.valueProperty(), null, categoryFilter.getValue());

        //update predicate based on category combobox
        categoryFilter.setOnAction(actionEvent -> {
            String selectedFilter = categoryFilter.getValue();
            currentProductCategory.setText(selectedFilter);

            switch (selectedFilter) {
                case "All Products":
                    categoryRestriction.set(product -> true);
                    break;
                case "Merchandise":
                    categoryRestriction.set(product -> product.getCategory().equals("Merchandise"));
                    break;
                case "Game":
                    categoryRestriction.set(product -> product.getCategory().equals("Game"));
                    break;
                case "Hardware":
                    categoryRestriction.set(product -> product.getCategory().equals("Hardware"));
                    break;
            }
            refreshFilteredList();
        });
        categoryFilter.getOnAction().handle(new ActionEvent());

        //update predicate each time searchbar changes
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> { //filterRestriction.set(1, newValue);
            searchRestriction.set(product -> product.getProductName().contains(newValue)); //filter product to contain the search bar content
            refreshFilteredList();
        });

        //update predicate each time min price changes
        minPriceFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, ""))  {
                minPriceRestriction.set(product -> product.getProductPrice() >= Double.parseDouble(newValue));
            } else { //reset the predicate if it is empty
                minPriceRestriction.set(product -> true);
            }
            refreshFilteredList();
        });

        //update predicate each time max price changes
        maxPriceFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, "")) {
                maxPriceRestriction.set(product -> product.getProductPrice() <= Double.parseDouble(newValue));
            } else { //reset predicate if empty
                maxPriceRestriction.set(product -> true);
            }
            refreshFilteredList();
        });

        //toggle on and off the tag pane
        openTagFilterButton.setOnAction(event -> {
            tagFilterContainerScroll.setVisible(!tagFilterContainerScroll.isVisible());
            tagFilterContainerScroll.setManaged(!tagFilterContainerScroll.isManaged());
        });

        this.getChildren().add(fullPage);

        //css styling
        this.getStylesheets().add(getClass().getResource("/css/shopPage.css").toExternalForm());
        productContainer.setPadding(new Insets(20));
        VBox.setVgrow(productScroll, Priority.ALWAYS);
        productScroll.setFitToWidth(true);
        productScroll.setFitToHeight(true);
        tagFilterContainerScroll.setFitToWidth(true);

        productContainer.setHgap(20);
        productContainer.setVgap(20);

        this.setId("root");
        currentProductCategory.setId("categoryHeader");
        categoryFilter.getStyleClass().add("category-filter-box");
        topSectionBox.setId("top-HBox");
        searchBar.setId("search-bar");
        openTagFilterButton.setId("tag-filter-button");
        priceFilterSeperator.setId("price-filter-seperator");
        tagFilterContainerScroll.getStyleClass().add("tag-container-scroll");

        ScrollPane[] scrollpanes = {productScroll, tagFilterContainerScroll};
        for (ScrollPane pane : scrollpanes) {
            pane.getStyleClass().add("scrollPane");
        }

        CustomTextField[] textFilter = {minPriceFilter, maxPriceFilter};
        for (CustomTextField filter : textFilter) {
            filter.getStyleClass().add("price-text-field");
        }

    }

    //return clickable tiles that represent a product
    public VBox createProductTile(Product product) {
        VBox productTile = new VBox(5);

        //run image loading on another thread to avoid lag
        ImageView productImage = product.getProductImage_multiThread(170, 170, productManager.getProductDirectoryPath());
        CustomLabel productName = new CustomLabel(product.getProductName());
        CustomLabel productPrice = new CustomLabel("RM" + product.getProductPrice().toString());

        //show a popup that let use add to cart
        productTile.setOnMouseClicked(event -> {
            GameZone.showProductPageScene(product);
        });

        productName.getStyleClass().add("product-name");
        productPrice.getStyleClass().add("product-price");
        productTile.getStyleClass().add("product-tile");

        productName.setWrapText(true);
        productTile.getChildren().addAll(productImage, productName, productPrice);
        return productTile;
    }

    //return a flow pane that contain buttons of tags for toggle
    private FlowPane createTagFilterSelection(String category){
        FlowPane tagsContainer = new FlowPane();
        ObservableList<String> tags = sqlController.getTags(category); //list of tags in the program
        for (String tag : tags) { //create an initially unselected tag (as button)
            Button tagButton = new Button(tag);
            tagsContainer.getChildren().add(tagButton);
            tagButton.setStyle("-fx-background-color: transparent;");

            //toggle tag button to apply filter
            tagButton.setOnAction(event -> {
                boolean isSelected = tagButton.getStyle().contains("rgba(0, 160, 255, 0.8)"); //set colour as logic indicator
                if (isSelected) {
                    selectedTags.remove(tag); //not toggle
                    tagButton.setStyle("-fx-background-color: transparent;");
                } else {
                    selectedTags.add(tag); //toggle
                    tagButton.setStyle("-fx-background-color: rgba(0, 160, 255, 0.8);");
                }
            });

            //css styling
            tagButton.getStyleClass().add("tag-button");
            tagsContainer.getStyleClass().add("tag-container");
        }

        return tagsContainer;
    }

    //refresh the filtered list with new predicate condition
    private void refreshFilteredList() {
        filteredList.setPredicate(maxPriceRestriction.get()
                .and(minPriceRestriction.get())
                .and(searchRestriction.get())
                .and(categoryRestriction.get())
                .and(tagRestriction.get()));
    }

}
