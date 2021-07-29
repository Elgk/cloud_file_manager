package client;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/manager.fxml"));
        primaryStage.setScene(new Scene(root, 650, 400));
        primaryStage.setTitle("Network Storage Manager");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
