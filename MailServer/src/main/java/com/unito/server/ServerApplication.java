package com.unito.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApplication extends Application {
    private final ExecutorService workers = Executors.newFixedThreadPool(10);
    private boolean running = true;
    private ServerSocket listener;

    @Override
    public void start(Stage stage) {
        Runnable server = () -> {
            try {
                listener = new ServerSocket(12345);
                System.out.println("Listening on port 12345...");
                while (running)
                    workers.execute(new ServerHandler(listener.accept()));
            } catch (IOException e) {
            } catch (RejectedExecutionException e) {
                System.out.println("Task rejected by ExecutorService: " + e.getMessage());
            }
        };

        Runnable gui = () -> {
            try {
                URL url = this.getClass().getClassLoader().getResource("view/server-view.fxml");
                Parent root = FXMLLoader.load(url);
                Scene scene = new Scene(root);

                stage.setTitle("Server");
                stage.setScene(scene);
                stage.setWidth(500);
                stage.setHeight(400);
                stage.show();
            } catch (IOException e) {
                System.out.println("Error loading FXML file");
            }
        };
        workers.execute(server);
        gui.run();
    }

    @Override
    public void stop() {
        running = false;
        System.out.println("Server shutting down...");

        try {
            if (listener != null && !listener.isClosed()) {
                listener.close(); // 👉 Sblocca il listener.accept()
                System.out.println("ServerSocket closed.");
            }
        } catch (IOException e) {
            System.out.println("Errore chiudendo il ServerSocket: " + e.getMessage());
        }

        workers.shutdown();
        try {
            if (!workers.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}