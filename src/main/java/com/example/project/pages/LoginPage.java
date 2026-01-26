package com.example.project.pages;

import com.example.project.GameZone;
import com.example.project.utilities.CurrentUser;
import com.example.project.utilities.SQLController;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.Objects;
import java.util.function.Predicate;

import static com.example.project.utilities.CustomComponent.*;

public class LoginPage extends StackPane {
    private static final SQLController sqlController = SQLController.getController();

    //layout & functionalities
    public LoginPage() {
        //clickable
        CustomButton loginButton = new CustomButton("Login");
        Label switchPanel = new Label("Create a new account");
        Label switchPanel2 = new Label("Back to login");
        CustomButton signUpButton = new CustomButton("Sign Up");

        //texts
        CustomLabel signUpLabel = new CustomLabel("Sign Up");
        CustomLabel loginLabel = new CustomLabel("Login");
        CustomLabel aboutLabel = new CustomLabel("GameZone Featured");
        CustomLabel usernameLabel = new CustomLabel("Username:");
        CustomLabel passwordLabel = new CustomLabel("Password:");
        CustomLabel usernameLabel2 = new CustomLabel("Username:");
        CustomLabel passwordLabel2 = new CustomLabel("Password:");
        CustomLabel confirmPasswordLabel = new CustomLabel("Confirm Password:");
        CustomLabel item3Label = new CustomLabel("Copies of classic games");
        CustomLabel item2Label = new CustomLabel("Quality Hardware Devices");
        CustomLabel item1Label = new CustomLabel("Merchandises for various games and shows");
        CustomLabel errorMessage1 = new CustomLabel("Username cannot be empty or exceed 32 characters.");
        CustomLabel errorMessage2 = new CustomLabel("Confirmed password does not match.");
        CustomLabel errorMessage3 = new CustomLabel("Username is already taken.");
        CustomLabel errorMessage4 = new CustomLabel("Password must be at least 8 characters, including an uppercase, lowercase, and a number.");
        CustomLabel errorMessage5 = new CustomLabel("Username or Password is incorrect.");

        //Images
        ImageView item1 =  new ImageView(new Image(getClass().getResourceAsStream("/images/aboutItem1.jpg")));
        ImageView item2 =  new ImageView(new Image(getClass().getResourceAsStream("/images/aboutItem2.jpg")));
        ImageView item3 =  new ImageView(new Image(getClass().getResourceAsStream("/images/aboutItem3.jpg")));

        //inputs
        CustomTextField usernameInput = new CustomTextField("Enter Username");
        CustomPasswordField passwordInput = new CustomPasswordField("Enter Password");
        CustomTextField createUsernameInput = new CustomTextField("Enter Username");
        CustomPasswordField createPasswordInput = new CustomPasswordField("Enter Password");
        CustomPasswordField confirmPasswordInput = new CustomPasswordField("Confirm Password");

        //layout
        VBox signupSection = new VBox(10, signUpLabel, usernameLabel2, createUsernameInput, passwordLabel2, createPasswordInput, confirmPasswordLabel,confirmPasswordInput, switchPanel2, signUpButton);
        VBox loginSection = new VBox(10, loginLabel, usernameLabel, usernameInput, passwordLabel, passwordInput, switchPanel, loginButton);
        VBox aboutItem = new VBox(item1, item1Label);
        VBox aboutItem2 = new VBox(item2, item2Label);
        VBox aboutItem3 = new VBox(item3, item3Label);
        HBox aboutWrapper = new HBox(15, aboutItem2, aboutItem3);
        VBox aboutSection = new VBox(20, aboutLabel, aboutItem, aboutWrapper);
        StackPane loginSignupWrapper = new StackPane(loginSection, signupSection);
        GridPane fullPanel = new GridPane();

        //gathering components
        CustomLabel[] header = {signUpLabel, loginLabel, aboutLabel};
        CustomLabel[] labels = {usernameLabel, passwordLabel, confirmPasswordLabel, usernameLabel2, passwordLabel2};
        Label[] clickableLabels = {switchPanel, switchPanel2};
        CustomButton[] buttons = {loginButton, signUpButton};
        CustomLabel[] imageLabel = {item1Label, item2Label, item3Label};
        CustomLabel[] errorMessage = {errorMessage1, errorMessage2, errorMessage3, errorMessage4, errorMessage5};
        TextField[] userInputs = {createUsernameInput, createPasswordInput, confirmPasswordInput, usernameInput, passwordInput};

        //layout setup
        fullPanel.add(loginSignupWrapper,0,0);
        fullPanel.add(aboutSection,1,0);

        //constraints & configuration
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column1.setPercentWidth(40);
        column2.setPercentWidth(60);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(100);
        fullPanel.getColumnConstraints().addAll(column1,column2);
        fullPanel.getRowConstraints().addAll(row1);

        aboutItem.setAlignment(Pos.CENTER);
        item1.fitHeightProperty().bind(this.heightProperty().multiply(0.35));
        item1.fitWidthProperty().bind(this.widthProperty().multiply(0.4));
        item2.fitHeightProperty().bind(this.heightProperty().multiply(0.25));
        item2.fitWidthProperty().bind(this.widthProperty().multiply(0.25));
        item3.fitHeightProperty().bind(this.heightProperty().multiply(0.25));
        item3.fitWidthProperty().bind(this.widthProperty().multiply(0.25));

        this.setPadding(new Insets(60, 30, 60, 30));
        signupSection.setVisible(false);
        fullPanel.setHgap(15);

        //Actions setting
        //switch panel labels action and styling
        for (Label label : clickableLabels) {
            label.setOnMouseClicked(event -> {
                signupSection.setVisible(!signupSection.isVisible()); //sign up section is invisible above login session
                //clear input when switching between login and sign up
                for (TextField field : userInputs) { //reset the user inputs
                    field.clear();
                }
            });
        }

        signUpButton.setOnAction(event -> {
            String username = createUsernameInput.getText();
            String password = createPasswordInput.getText();
            String confirmPassword = confirmPasswordInput.getText();

            boolean userRepeated = sqlController.checkRepeated(username, "users", "username"); //username doesnt exist
            boolean userRequirement = !username.matches(".{1,32}$"); //username is not empty and less than 32 word
            boolean passRequirement = !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"); //password no less than 8, one lower case, one upper case and one number required
            boolean correctConfirmation = !confirmPassword.equals(password); //confirm password = password

            if (!(userRepeated || userRequirement || passRequirement || correctConfirmation)) {
                sqlController.insertUser(username, password, false);
                for (TextField field : userInputs) { //reset the user inputs
                    field.clear();
                }
                showSignUpSuccessPopup();
            }
        });

        loginButton.setOnAction(event -> {
            String username = usernameInput.getText();
            String password = passwordInput.getText();

            //check if user is in sql
            if (sqlController.compareUserTable(username, password, "password")) {
                boolean isAdmin = sqlController.compareUserTable(username, "1", "admin");
                CurrentUser.initializeCurrentUser(username, isAdmin); //initialize user session
                GameZone.launchMainApplication();
            } else {
                if(!loginSection.getChildren().contains(errorMessage5)) {
                    loginSection.getChildren().add(5, errorMessage5);
                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(event2 -> {
                        loginSection.getChildren().remove(errorMessage5);
                    });
                    pause.play();
                }
            }
        });

        //listener setup for textfield error messages prompt
        signUpErrorMessage(createUsernameInput, errorMessage1, input -> !input.matches(".{1,32}$"), signupSection);
        signUpErrorMessage(confirmPasswordInput, errorMessage2, input -> !input.equals(createPasswordInput.getText()), signupSection);
        signUpErrorMessage(createUsernameInput, errorMessage3, input -> sqlController.checkRepeated(input, "users", "username"), signupSection);
        signUpErrorMessage(createPasswordInput, errorMessage4, input -> !input.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"), signupSection);

        //listener setup for restricting spaces from textfield
        for (TextField field : userInputs) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                field.setText(newValue.replace(" ", ""));
            });
        }

        //listener to update confirm password textfield when operating on create password input
        createPasswordInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(confirmPasswordInput.getText())) { //if confirm password = password, remove the error message
                signupSection.getChildren().remove(errorMessage2);
                confirmPasswordInput.setStyle(confirmPasswordInput.getStyle().replace("-fx-border-color: red;", ""));
            }
            //if password is not empty and different with confirm password, show the error if haven't already
            else if (!newValue.equals(confirmPasswordInput.getText()) && !signupSection.getChildren().contains(errorMessage2) && !Objects.equals(confirmPasswordInput.getText(), "")) {
                int index = signupSection.getChildren().indexOf(confirmPasswordInput);
                signupSection.getChildren().add(index + 1, errorMessage2);
                confirmPasswordInput.setStyle(confirmPasswordInput.getStyle() + "-fx-border-color: red; -fx-border-width: 2px;");
            }
        });

        //css styling
        this.getStylesheets().add(getClass().getResource("/css/loginPage.css").toExternalForm());
        fullPanel.getStyleClass().add("full-panel");
        signupSection.getStyleClass().add("signup-section");
        loginSection.getStyleClass().add("login-section");
        aboutSection.getStyleClass().add("about-section");
        item1.getStyleClass().add("item1");

        for (CustomLabel label: header) {
            label.getStyleClass().add("header");
        }

        for (CustomLabel label: labels) {
            label.getStyleClass().add("normal-label");
        }

        for (Label label : clickableLabels) {
            label.getStyleClass().add("clickable-label");
        }

        for (CustomButton button : buttons) {
            button.getStyleClass().add("button");
        }

        for (CustomLabel imageLabels : imageLabel) {
            imageLabels.getStyleClass().add("image-label");
        }

        for (CustomLabel errormessage : errorMessage) {
            errormessage.getStyleClass().add("error-message");
        }


        //background setting
        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/loginBackground.jpg"));
        BackgroundImage backgroundPicture = new BackgroundImage(backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true));
        Background background = new Background(backgroundPicture);
        this.setBackground(background);
        this.getChildren().add(fullPanel);
    }

    //error prompt when user and password did not meet requirement as the user is typing
    public void signUpErrorMessage(TextField textfield, CustomLabel message, Predicate<String> requirement, VBox container) {
        textfield.textProperty().addListener((observable, oldValue, newValue) -> {
            int index = container.getChildren().indexOf(textfield); //index to fit the error message
            boolean condition = (requirement.test(newValue)) && !textfield.getText().isEmpty();

            if (condition) {
                if (!container.getChildren().contains(message)) {
                    container.getChildren().add(index + 1, message);
                    textfield.setStyle(textfield.getStyle() + "-fx-border-color: red; -fx-border-width: 2px;");
                }

            } else {
                if (container.getChildren().contains(message)) {
                    container.getChildren().remove(message);
                    textfield.setStyle(textfield.getStyle().replace("-fx-border-color: red;", ""));
                }
            }
        });
    }

    public void showSignUpSuccessPopup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sign Up Successful");
        alert.setHeaderText(null);
        alert.setContentText("You have successfully signed up!\nPlease proceed to login to your account.");
        alert.showAndWait();
    }
}