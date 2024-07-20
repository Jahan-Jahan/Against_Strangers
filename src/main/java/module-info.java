module org.example.against_strangers {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens org.example.against_strangers to javafx.fxml;
    exports org.example.against_strangers;
}