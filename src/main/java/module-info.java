module com.example.loginapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;

    opens com.example.loginapp to javafx.fxml;
    exports com.example.loginapp;
}