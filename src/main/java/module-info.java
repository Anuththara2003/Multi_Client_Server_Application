module lk.ijse.gdse71.groupchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens lk.ijse.gdse71.groupchat to javafx.fxml;
    exports lk.ijse.gdse71.groupchat;
}