package com.example.project.pages;

import com.example.project.items.User;
import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.SQLController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.example.project.utilities.CustomComponent.*;

public class ManageUserPage extends StackPane {
    private static final SQLController sqlController = SQLController.getController();
    private static final CurrentUser currentUser = CurrentUser.getCurrentUser();

    public ManageUserPage() {
        //UI components
        CustomLabel pageTitle = new CustomLabel("Admin Role Management");
        CustomTextField searchBar = new CustomTextField("Search");
        CustomButton addAsAdminButton = new CustomButton("Add as Admin");
        CustomButton removeAsAdminButton = new CustomButton("Remove as Admin");
        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Users", "Admin", "User");
        roleFilter.getSelectionModel().selectFirst();
        TableView<User> userTable = new TableView<>();

        ObservableList<User> userList = sqlController.getAllUsers();
        FilteredList<User> filteredUserList = new FilteredList<>(userList);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> adminCol = new TableColumn<>("Role");
        adminCol.setCellValueFactory(user -> {
            boolean isAdmin = user.getValue().getIsAdmin();
            String role = isAdmin ? "Admin" : "User";
            return new SimpleStringProperty(role);
        });

        Map<User, CheckBox> selectedUsers = new HashMap<>();
        TableColumn<User, CheckBox> selectCol = new TableColumn<>("");
        selectCol.setCellFactory(col -> {
            return new TableCell<User, CheckBox>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        if (checkBox.isSelected()) {
                            selectedUsers.put(user, checkBox);
                        } else {
                            selectedUsers.remove(user);
                        }

                        checkBox.getStyleClass().add("check-box");
                    });
                }

                @Override
                protected void updateItem(CheckBox item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(checkBox);
                    }
                }
            };
        });

        selectCol.setMaxWidth(600);

        AtomicReference<Predicate<User>> userFilterRestriction = new AtomicReference<>(user -> true);
        AtomicReference<Predicate<User>> searchFilterRestriction = new AtomicReference<>(user -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            userFilterRestriction.set(user ->  user.getUsername().contains(newValue));
            filteredUserList.setPredicate(userFilterRestriction.get().and(searchFilterRestriction.get()));
        });

        roleFilter.setOnAction(actionEvent -> {
            String choice = roleFilter.getValue();
            searchFilterRestriction.set(user -> switch (choice) {
                case "Admin" -> user.getIsAdmin();
                case "User" -> !user.getIsAdmin();
                default -> true;
            });
            filteredUserList.setPredicate(userFilterRestriction.get().and(searchFilterRestriction.get()));
        });

        addAsAdminButton.setOnAction(actionEvent -> {
            selectedUsers.forEach((user, value) -> {
                sqlController.updateUserRole(user.getUsername(), true);
                user.setIsAdmin(true);
                value.setSelected(false);
            });
            selectedUsers.clear();
            userTable.refresh();
        });

        removeAsAdminButton.setOnAction(actionEvent -> {
            selectedUsers.forEach((user, value) -> {
                if (!Objects.equals(user.getUsername(), currentUser.getCurrentUserName())) {
                    sqlController.updateUserRole(user.getUsername(), false);
                    user.setIsAdmin(false);
                }
                value.setSelected(false);
            });
            selectedUsers.clear();
            userTable.refresh();
        });


        //layout
        userTable.getColumns().addAll(usernameCol, adminCol, selectCol);
        userTable.setItems(filteredUserList);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        HBox actionButtonPane = new HBox(10, addAsAdminButton, removeAsAdminButton);
        AnchorPane filterPane = new AnchorPane(roleFilter, searchBar);
        AnchorPane.setRightAnchor(searchBar, 20.0);
        AnchorPane.setRightAnchor(roleFilter, 280.0);
        VBox fullPage = new VBox(10, pageTitle, filterPane, userTable, actionButtonPane);

        //css Styling
        this.getStylesheets().add(getClass().getResource("/css/tableview.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/css/userPage.css").toExternalForm());
        this.getStyleClass().add("root");
        pageTitle.getStyleClass().add("page-title");
        roleFilter.getStyleClass().add("user-filter-box");
        searchBar.getStyleClass().add("search-bar");
        removeAsAdminButton.getStyleClass().add("action-button");
        addAsAdminButton.getStyleClass().add("action-button");

        this.getChildren().add(fullPage);
    }
}
