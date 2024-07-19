module org.example.airplanewar {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens org.example.airplanewar to javafx.fxml;
    exports org.example.airplanewar;
}