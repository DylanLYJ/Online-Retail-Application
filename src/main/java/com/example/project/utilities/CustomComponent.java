package com.example.project.utilities;
import javafx.scene.control.*;
import java.util.Objects;

public class CustomComponent {
    public static class CustomLabel extends Label {
        public CustomLabel(String text) {
            super(text);
            this.setMaxWidth(Double.MAX_VALUE);
            this.setWrapText(true);
        }
    }

    public static class CustomTextField extends TextField {
        public CustomTextField(String text) {
            super();
            this.setPromptText(text);
        }

        //for limited character
        public CustomTextField(String text, int maxChar) {
            super();
            this.setPromptText(text);
            this.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= maxChar ? change : null));
        }

        public CustomTextField(String text, String restriction) {
            super();
            this.setPromptText(text);
            TextFormatter<String> condition = new TextFormatter<>(param -> {
                String inputText = param.getControlNewText();
                if (Objects.equals(restriction, "double")) {
                    //condition: no character other than number, except . (dot), after dot only 2 number is allowed
                    if (inputText.matches("\\d*(\\.\\d{0,2})?")) {
                        return param;
                    } else {
                        return null;
                    }
                } else if (Objects.equals(restriction, "integer")) {
                    //allow only numbers
                    if (inputText.matches("[0-9]*")) {
                        return param;
                    } else {
                        return null;
                    }
                }
                return param;
            });
            //apply it to the returned text
            this.setTextFormatter(condition);
        }
    }

    public static class CustomPasswordField extends PasswordField {
        public CustomPasswordField(String text) {
            super();
            this.setPromptText(text);
        }
    }

    public static class CustomButton extends Button {
        public CustomButton(String text) {
            super(text);
            this.setMaxWidth(Double.MAX_VALUE);
        }
    }

    public static class CustomTextArea extends TextArea {
        public CustomTextArea(String text, int maxChar) {
            super();
            this.setPromptText(text);
            this.setWrapText(true);
            this.setPrefRowCount(5);
            //disregard any input after .length() > max character
            this.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= maxChar ? change : null));
        }
    }

}
