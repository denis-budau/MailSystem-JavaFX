package com.unito.server.DataModel;

import com.unito.server.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServerModel {
    private static ServerModel instance;
    public static List<String> registeredUsers;  // Utenti registrati
    public static List<String> connectedUsers;  // Utenti connessi
    public static ObservableList<Message> messages;

    public static ServerModel getInstance() {
        if (instance == null) {
            instance = new ServerModel();
        }
        return instance;
    }

    public ServerModel() {
        registeredUsers = new ArrayList<>();
        connectedUsers = new ArrayList<>();
        messages = FXCollections.observableArrayList();;
        loadUsersFromFile("src/main/resources/data");  // Legge gli utenti dal file
    }

    public static void loadUsersFromFile(String directoryPath) {
        File directory = new File(directoryPath);

        File[] files = directory.listFiles();
        // Aggiungi le email alla lista registrata (nome file senza l'estensione .csv)
        if (files != null) {
            for (File file : files) {
                String email = file.getName().replace(".csv", "");  // Estrae l'email dal nome del file (rimuove ".csv")
                registeredUsers.add(email);
            }
        } else { System.err.println("Errore: la cartella non esiste o non contiene file CSV.");}
    }

    // Metodo per aggiungere un utente connesso
    public static synchronized void addConnectedUser(String username) {
        if (!connectedUsers.contains(username)) {
            connectedUsers.add(username);
            System.out.println("Utente connesso: " + username);
        }
    }

    // Metodo per rimuovere un utente dalla lista
    public static synchronized void removeConnectedUser(String username) {
        if (connectedUsers.contains(username)) {
            connectedUsers.remove(username);
            System.out.println("Utente disconnesso: " + username);
        }
    }

    // Metodo per ottenere la lista degli utenti connessi
    public static synchronized List<String> getConnectedUsers() {
        return new ArrayList<>(connectedUsers);  // restituisce una copia per evitare modifiche esterne
    }

    // Metodo per verificare se un utente è connesso
    public static synchronized boolean isUserConnected(String username) {
        return connectedUsers.contains(username);
    }

    // Metodo per aggiungere nuovi messaggi
    public void addMessage(String user, String message) {
        messages.add(new Message(user, message));
    }

    // Metodo per ottenere la lista degli utenti registrati
    public static List<String> getRegisteredUsers() {
        return registeredUsers;
    }

    // Metodo per ottenere la lista dei messaggi
    public static ObservableList<Message> getMessages() {
        return messages;
    }
}