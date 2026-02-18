package com.unito.client;

import com.unito.client.Controller.NewMailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ViewSwitcher {
    private Stage stage; // La finestra principale dell'app
    private static ViewSwitcher instance;

    // Costruttore che imposta Stage
    private ViewSwitcher(Stage stage) {
        this.stage = stage; // Salviamo l'istanza di Stage
    }

    public static ViewSwitcher getInstance(Stage stage) {
        if (instance != null || stage == null)  return instance;
        return instance = new ViewSwitcher(stage);
    }

    private record Pair(Scene scene, Object controller) {}

    private Pair loadSceneFromFXML(String fxml) throws IOException {
        URL url = this.getClass().getClassLoader().getResource(fxml);
        if (url == null) {
            throw new IOException("FXML file not found: " + fxml);
        }

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Object controller = loader.getController();
        Scene content = new Scene(root);

        return new Pair(content, controller);
    }

    public Object switchTo(String fxml, String title) {
        try {
            Pair next = loadSceneFromFXML(fxml);
            stage.setScene(next.scene);
            stage.setTitle(title);
            stage.sizeToScene();
            stage.show();
            return next.controller;
        } catch (IOException e) {
            System.err.println("Error while switching to scene: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void changeTo(String fxml, String title, boolean isReply, boolean isReplyAll, boolean isForward) {
        // Crea il controller della nuova finestra
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxml));

        try {
            // Carica il file FXML per la finestra dei dettagli della mail
            Parent root = loader.load();

            Object controller = loader.getController();

            if(controller instanceof NewMailController) {
                NewMailController newMailController = (NewMailController) controller;
                newMailController.configure(isReply, isReplyAll, isForward);
                System.out.println("reply è: " + isReply);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
