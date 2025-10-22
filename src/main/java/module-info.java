module com.example.ortohero {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;


    opens com.example.ortohero to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.example.ortohero;
}