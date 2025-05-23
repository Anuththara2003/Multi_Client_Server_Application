package lk.ijse.gdse71.groupchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/view/Server.fxml"));
        stage.setScene(new javafx.scene.Scene(parent));
        stage.show();
    }
}
