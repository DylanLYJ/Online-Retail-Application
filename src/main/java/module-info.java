module com.example.coursework {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.example.project to javafx.fxml;
    exports com.example.project;
    exports com.example.project.items;
    opens com.example.project.items to javafx.fxml;
    exports com.example.project.pages;
    opens com.example.project.pages to javafx.fxml;
    exports com.example.project.utilities;
    opens com.example.project.utilities to javafx.fxml;
}