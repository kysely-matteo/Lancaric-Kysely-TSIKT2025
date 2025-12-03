module sk.front.front {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    // Jackson modules
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires org.slf4j;



    opens sk.front.front to javafx.fxml;
    opens sk.front.front.model to com.fasterxml.jackson.databind, javafx.base;
    opens sk.front.front.controller to javafx.fxml;

    exports sk.front.front;
    exports sk.front.front.model to com.fasterxml.jackson.databind;
    exports sk.front.front.controller;
}