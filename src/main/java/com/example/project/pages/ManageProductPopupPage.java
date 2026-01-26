package com.example.project.pages;

import com.example.project.GameZone;
import com.example.project.utilities.CustomComponent.*;
import com.example.project.utilities.ProductManager;
import com.example.project.utilities.SQLController;
import com.example.project.items.Product;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ManageProductPopupPage extends StackPane {
    private static final ProductManager productManager = ProductManager.getManager();
    private static final SQLController sqlController = SQLController.getController();
    private static ObservableList<String> appliedTags;
    private static ImageView productImageDisplay;

    public ManageProductPopupPage(Product product) {
        //clickable
        Button selectImage = new Button("📂" + " Select Image");
        String buttonText;
        if (product != null) {
            buttonText = "Edit Product";
        } else {
            buttonText = "Add Product";
        }
        CustomButton finalizeButton = new CustomButton(buttonText);

        //label
        CustomLabel addProductNameLabel = new CustomLabel("Name:");
        CustomLabel addProductPriceLabel = new CustomLabel("Price:");
        CustomLabel addProductStockLabel = new CustomLabel("Stock:");
        CustomLabel addProductCategoryLabel = new CustomLabel("Category:");
        CustomLabel addProductDescriptionLabel = new CustomLabel("Product Description:");
        CustomLabel errorMessage = new CustomLabel("Product Name already exists or is empty");
        CustomLabel errorMessage2 = new CustomLabel("Product Price must be a positive number");
        CustomLabel errorMessage3 = new CustomLabel("Product Stock must be a positive number");
        CustomLabel productTagsLabel = new CustomLabel("Tags associated with this product");

        errorMessage.setVisible(false);
        errorMessage2.setVisible(false);
        errorMessage3.setVisible(false);
        errorMessage.setManaged(false);
        errorMessage2.setManaged(false);
        errorMessage3.setManaged(false);

        //inputs
        CustomTextField productNameInput = new CustomTextField("Product Name", 40);
        CustomTextField productPriceInput = new CustomTextField("Product Price", "double");
        CustomTextField productStockInput = new CustomTextField("Product Stock", "integer");
        RadioButton merchandise = new RadioButton("Merchandise");
        RadioButton game = new RadioButton("Game");
        RadioButton hardware = new RadioButton("Hardware");
        ToggleGroup categoryGroup = new ToggleGroup();
        categoryGroup.getToggles().add(merchandise);
        categoryGroup.getToggles().add(game);
        categoryGroup.getToggles().add(hardware);
        categoryGroup.selectToggle(game);
        CustomButton productTagsSelectionButton = new CustomButton("+");
        CustomTextArea productDescriptionInput = new CustomTextArea("Product Description...", 300);
        Button returnButton = new Button("<");

        if (product != null) {
            //filter out the tags that is deleted
            appliedTags = product.getProductTags(sqlController);
            ObservableList<String> tags = sqlController.getTags(product.getCategory());
            appliedTags.removeIf(tag -> !tags.contains(tag));

            //initialize the textfield to be edited
            productNameInput.setText(product.getProductName());
            productPriceInput.setText(product.getProductPrice().toString());
            productStockInput.setText(product.getProductStock().toString());
            productDescriptionInput.setText(product.getDescription());
            productImageDisplay = product.getProductImage(400, 400, productManager.getProductDirectoryPath());

            if (product.getCategory().equals("Game")) {
                game.setSelected(true);
            } else if (product.getCategory().equals("Hardware")) {
                hardware.setSelected(true);
            } else {
                merchandise.setSelected(true);
            }
        } else {
            appliedTags = FXCollections.observableArrayList();
            productImageDisplay = new ImageView(new Image("file:" + productManager.getProductDirectoryPath() + "noImage.jpg"));
        }


        //tags will be cleared when category change
        categoryGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            appliedTags.clear();
        });

        //a pane that store labels of applied tags
        FlowPane tagsContainer = new FlowPane();
        //update flow pane everytime applied tag change
        ListChangeListener<String> tagUpdateListener = new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                tagsContainer.getChildren().clear();
                for (String tag : appliedTags) {
                    CustomLabel tagLabel = new CustomLabel(tag);
                    tagsContainer.getChildren().add(tagLabel);

                    //css styling
                    tagLabel.getStyleClass().add("tag-label");
                }
                if (appliedTags.isEmpty()) {
                    CustomLabel noTagsLabel = new CustomLabel("No tag applied");
                    tagsContainer.getChildren().add(noTagsLabel);
                    noTagsLabel.getStyleClass().add("no-tags");
                }

            }
        };
        ScrollPane tagsScroll = new ScrollPane(tagsContainer);
        appliedTags.addListener(tagUpdateListener);
        tagUpdateListener.onChanged(null);

        //by default, image will have either the original product image or default noImage.jpg
        AtomicReference<String> productImageName;
        if (product != null) {
            productImageName = new AtomicReference<>(product.getProductImageName());
        } else {
            productImageName = new AtomicReference<>("noImage.jpg");
        }

        //selected image will be displayed and throw into image directory
        AtomicReference<File> selectedImage = new AtomicReference<>(new File(productManager.getProductDirectoryPath() + File.separator + productImageName.get())); //potential bug original productImageName.get() is noImage.jpg
        selectImage.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")); //image file allowed
            selectedImage.set(fileChooser.showOpenDialog(selectImage.getScene().getWindow()));
            if (selectedImage.get() != null) {
                Image image = new Image(selectedImage.get().toURI().toString());
                if (image.isError()) { //image load fail
                    productImageDisplay.setImage(new Image("file:" + productManager.getProductDirectoryPath() + "noImage.jpg"));
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error loading image, please select another one");
                    alert.showAndWait();
                } else {
                    productImageDisplay.setImage(image);
                    productImageName.set(selectedImage.get().getName());
                    productImageDisplay.setFitWidth(400);
                    productImageDisplay.setFitHeight(400); //limit the image
                }

            } else {
                selectedImage.set(new File(productManager.getProductDirectoryPath() + productImageName.get()));
            }
        });

        finalizeButton.setOnAction(event -> {
            boolean repeatedProduct = sqlController.checkRepeated(productNameInput.getText(), "product", "productname");
            boolean invalidPrice = isNegativeDouble(productPriceInput.getText());
            boolean invalidStock = isNegativeDouble(productStockInput.getText());
            boolean emptyName = productNameInput.getText().isEmpty();
            String productName = productNameInput.getText();
            String productCategory = ((RadioButton) categoryGroup.getSelectedToggle()).getText();
            String productDescription = !productDescriptionInput.getText().trim().isEmpty() ? productDescriptionInput.getText() : "No description";
            double productPrice;
            int productStock;

            //price is valid, stock is valid, no repeated or empty product, if updating product, it cannot be the same name
            if (!(invalidPrice || invalidStock || emptyName ||(repeatedProduct && (product == null || !Objects.equals(productName, product.getProductName()))))) {
                productPrice = Double.parseDouble(productPriceInput.getText());
                productStock = Integer.parseInt(productStockInput.getText());
                if (product != null) {
                    //product is specified, update it
                    productManager.updateProduct(product, productName, productPrice, productCategory, productImageName.get(), productStock, productDescription, appliedTags);
                    GameZone.showManageProductScene();

                } else {
                    //product doesn't exist, add it
                    productManager.addProduct(productName, productPrice, productCategory, productImageName.get(), productStock, productDescription, appliedTags);
                    GameZone.showManageProductScene();
                }
            } else {
                //show error prompt if invalid input exist
                if (product != null) {
                    showErrorPrompt((((repeatedProduct) && !Objects.equals(productName, product.getProductName())) || productNameInput.getText().isEmpty()), errorMessage);
                } else {
                    showErrorPrompt((repeatedProduct || productNameInput.getText().isEmpty()), errorMessage);
                }
                showErrorPrompt(invalidPrice, errorMessage2);
                showErrorPrompt(invalidStock, errorMessage3);
            }
            productManager.addProductImage(productImageName.get(), selectedImage.get());
        });

        //popup to toggle tags
        productTagsSelectionButton.setOnAction(event -> {
            double width = productTagsSelectionButton.getScene().getWindow().getWidth();
            double height = productTagsSelectionButton.getScene().getWindow().getHeight();
            double x = productTagsSelectionButton.getScene().getWindow().getX();
            double y = productTagsSelectionButton.getScene().getWindow().getY();
            createSelectTagsPopup(((RadioButton) categoryGroup.getSelectedToggle()).getText(), width, height).show(productTagsSelectionButton.getScene().getWindow(), x, y);
        });

        returnButton.setOnAction(event -> {
            GameZone.showManageProductScene();
        });

        //layout
        HBox radioButtonSection = new HBox (5, game, hardware, merchandise);
        VBox productCategorySection = new VBox(10, addProductCategoryLabel, radioButtonSection);
        HBox productStockSection = new HBox(5, addProductStockLabel, productStockInput);
        HBox productPriceSection = new HBox(10, addProductPriceLabel, productPriceInput, productStockSection);
        VBox productDescriptionSection = new VBox(5, addProductDescriptionLabel, productDescriptionInput);
        VBox productImageSection = new VBox(10, productImageDisplay, selectImage, errorMessage3);
        HBox productNameSection = new HBox(5, addProductNameLabel, productNameInput);
        HBox productTagLabelSection = new HBox(5, productTagsLabel, productTagsSelectionButton);
        VBox productTagSection = new VBox(3, productTagLabelSection, tagsScroll);
        VBox productDetailsSection = new VBox(7, productNameSection, errorMessage, productPriceSection, errorMessage2,
        productDescriptionSection, productCategorySection, productTagSection,
        finalizeButton);
        HBox productManageScene = new HBox(5, productImageSection, productDetailsSection);
        VBox fullPage = new VBox(5, returnButton, productManageScene);

        //styling
        RadioButton[] radioButtons = {hardware, game, merchandise};
        CustomLabel[] labels = {addProductNameLabel, addProductCategoryLabel, addProductPriceLabel, addProductDescriptionLabel, addProductStockLabel};

        HBox.setHgrow(productDetailsSection, Priority.ALWAYS);
        HBox.setHgrow(productPriceInput, Priority.ALWAYS);
        HBox.setHgrow(tagsScroll, Priority.ALWAYS);

        this.getStylesheets().add(getClass().getResource("/css/update&addProduct.css").toExternalForm());
        productDetailsSection.getStyleClass().add("product-details-section");

        for (RadioButton button : radioButtons) {
            button.getStyleClass().add("radio-button");
        }

        for (CustomLabel label : labels) {
            label.getStyleClass().add("normal-label");
        }

        productPriceInput.getStyleClass().add("price-input-field");
        productStockInput.getStyleClass().add("stock-input-field");
        productNameInput.getStyleClass().add("name-input-field");
        productDescriptionInput.getStyleClass().add("textarea-input-field");
        returnButton.getStyleClass().add("return-button");
        finalizeButton.getStyleClass().add("finalize-button");
        selectImage.getStyleClass().add("select-image-button");
        productTagsLabel.getStyleClass().add("small-label");
        tagsScroll.getStyleClass().add("tag-scroll");
        productTagsSelectionButton.getStyleClass().add("add-tags-button");
        errorMessage.getStyleClass().add("error-message");
        errorMessage2.getStyleClass().add("error-message");
        errorMessage3.getStyleClass().add("error-message");
        this.getStyleClass().add("root");

        this.getChildren().addAll(fullPage);
        this.setPadding(new Insets(30));
    }

    //check if double is valid (positive)
    public boolean isNegativeDouble(String priceInput) {
        try {
            return !(Double.parseDouble(priceInput) >= 0);
        } catch (Exception e) {
            return true;
        }
    }

    //if certain condition met, show a specified error message
    public void showErrorPrompt(boolean condition, CustomLabel errorMessage) {
        if (condition) {
            //only set visible if it is not visible
            if (!errorMessage.isVisible()) {
                errorMessage.setVisible(true);
                errorMessage.setManaged(true);

                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(event2 -> {
                    errorMessage.setVisible(false);
                    errorMessage.setManaged(false);
                });
                pause.play();
            }
        }
    }

    public Popup createSelectTagsPopup(String category, double stageWidth, double stageHeight) {
        Popup popup = new Popup();
        ObservableList<String> tagSelection = sqlController.getTags(category);

        //tags to select from section
        CustomLabel tagSelectionBoxLabel = new CustomLabel("Tags Selection:");
        VBox tagSelectionBox = new VBox(5);
        ScrollPane tagsContainer = new ScrollPane(tagSelectionBox);
        VBox tagSelectionSection = new VBox(5, tagSelectionBoxLabel, tagsContainer);
        for (String tag : tagSelection) {
            createTagSection(tag, category, tagSelection,tagSelectionBox);
        }

        //the applied tags section
        VBox appliedTagSection = new VBox(5);
        CustomLabel appliedTagBoxLabel = new CustomLabel("Applied tags: ");
        ListView<String> appliedTagList = new ListView<>(appliedTags);
        appliedTagSection.getChildren().addAll(appliedTagBoxLabel, appliedTagList);

        //insert new custom tag section
        CustomTextField newTagInput = new CustomTextField("new tag name", 25);
        CustomButton addTagButton = new CustomButton("+");
        addTagButton.setOnAction(event -> {
            String newTag = newTagInput.getText();
            if (!newTag.isEmpty() && !tagSelection.contains(newTag)) { //if input is not empty and doesn't exist
                appliedTags.add(newTag); //apply it to the product on the spot
                createTagSection(newTag, category, appliedTags,tagSelectionBox);
                sqlController.insertTag(newTag, category);
                tagSelection.add(newTag); //put the tag pane into the tag list (VBox)
            }
            newTagInput.clear();
        });

        CustomButton returnButton = new CustomButton("Close");
        returnButton.setOnAction(event -> {
            popup.hide();
        });

        //popup layout
        Rectangle overlayRectangle = new Rectangle();
        overlayRectangle.setFill(Color.BLACK);
        overlayRectangle.setOpacity(0.5);
        overlayRectangle.setWidth(stageWidth);
        overlayRectangle.setHeight(stageHeight);

        HBox addTagBox = new HBox(5, newTagInput, addTagButton);
        HBox tagContainer = new HBox(5, tagSelectionSection, appliedTagSection);
        VBox fullPage = new VBox(5, tagContainer, addTagBox, returnButton);
        StackPane fullPopup = new StackPane(fullPage);
        popup.getContent().add(overlayRectangle);
        popup.getContent().add(fullPopup);
        popup.setAutoHide(true);

        //styling
        fullPopup.setTranslateX(stageWidth / 3.8);
        fullPopup.setTranslateY(stageHeight / 6);
        HBox.setHgrow(appliedTagSection, Priority.ALWAYS);
        HBox.setHgrow(tagSelectionSection, Priority.ALWAYS);

        fullPopup.getStylesheets().add(getClass().getResource("/css/update&addProduct.css").toExternalForm());
        fullPopup.getStyleClass().add("popup");
        appliedTagSection.getStyleClass().add("applied-tags-section");
        tagSelectionSection.getStyleClass().add("tag-selection-section");
        returnButton.getStyleClass().add("return-button");
        appliedTagList.getStyleClass().add("list-view");
        appliedTagBoxLabel.getStyleClass().add("popup-label");
        tagSelectionBoxLabel.getStyleClass().add("popup-label");
        addTagButton.getStyleClass().add("add-tags-button");
        newTagInput.getStyleClass().add("tag-input-field");
        tagsContainer.getStyleClass().add("tags-container-scroll");

        return popup;
    }

    //create a HBox with toggle-able tag and delete button
    public void createTagSection(String tag, String category, ObservableList<String> tagSelection , VBox tagsContainer) {
        Button newTagButton = new Button(tag);

        if (appliedTags.contains(tag)) { //when initializing, set the visual to toggled
            newTagButton.setStyle("-fx-background-color: rgba(0, 160, 255, 0.8);");
        }

        newTagButton.setOnAction(event -> { //toggle will change colour
            if (appliedTags.contains(tag)) {
                appliedTags.remove(tag);
                newTagButton.setStyle("-fx-background-color: transparent;");

            } else {
                appliedTags.add(tag);
                newTagButton.setStyle("-fx-background-color: rgba(0, 160, 255, 0.8);");
            }
        });
        CustomButton deleteTagButton = new CustomButton("✖");

        HBox tagSection = new HBox(5, newTagButton, deleteTagButton);
        tagsContainer.getChildren().addAll(tagSection);

        deleteTagButton.setOnAction(event -> { //remove the tag from the pane, product and sql
            tagSelection.remove(tag);
            sqlController.deleteTag(tag, category);
            tagsContainer.getChildren().remove(tagSection);
            appliedTags.remove(tag);
        });

        newTagButton.getStyleClass().add("toggle-tag");
        deleteTagButton.getStyleClass().add("delete-tag");

    }
}
