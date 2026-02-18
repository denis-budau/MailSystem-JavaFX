package com.unito.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            ViewSwitcher.getInstance(stage);

            URL url = this.getClass().getClassLoader().getResource("view/login-view.fxml");

            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);

            stage.setTitle("Client Login");
            stage.setScene(scene);
            stage.setWidth(640);
            stage.setHeight(400);
            stage.show();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        ThreadPoolManager.getInstance().shutDown();
        System.out.println("Client chiuso correttamente.");
        System.exit(0); // Forza l'uscita di tutti i thread ancora attivi
    }

    public static void main(String[] args) {
        launch();
    }
}