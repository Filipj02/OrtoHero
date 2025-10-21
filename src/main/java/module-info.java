module com.example.ortohero {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ortohero to javafx.fxml;
    exports com.example.ortohero;
}