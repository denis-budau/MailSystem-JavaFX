package com.unito.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.unito.common.Mail;
import com.unito.server.DataModel.ServerModel;

public class ServerHandler implements Runnable {
    ServerModel model = ServerModel.getInstance();
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String user;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Apre gli stream per la comunicazione con il client
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            // Prova a leggere la richiesta
            String request = (String)in.readObject();
            System.out.println(request);

            switch (request) {
                case "login":
                    handleLogin();
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "send":
                    handleSend();
                    break;
                case "refresh":
                    handleRefresh();
                    break;
                case "delete":
                    handleDelete();
                    break;
                default:
                    out.writeObject("Comando non riconosciuto");
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (user != null && ServerModel.isUserConnected(user)) {
                System.out.println("Connessione terminata. Rimuovo utente: " + user);
                ServerModel.removeConnectedUser(user);
                model.addMessage(user, "Connection lost, user removed from connected list");
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLogin() throws IOException, ClassNotFoundException {
        // Legge l'username inviato dal client
        String username = (String) in.readObject();
        System.out.println("Login for: " + username);

        // Verifica se l'utente esiste
        if (ServerModel.getRegisteredUsers().contains(username)) {
            System.out.println("Login done for: " + username);
            out.writeObject(true);
            ServerModel.addConnectedUser(username);
            model.addMessage(username, "Login");
        } else {
            System.out.println(username + "not registered ");
            out.writeObject(false);
        }
    }

    private void handleLogout() throws IOException, ClassNotFoundException {
        String username =(String) in.readObject();
        System.out.println("Logout for: " + username);
        ServerModel.removeConnectedUser(username);
        model.addMessage(username, "Logout");
        System.out.println("Logout done for: " + username);
        out.writeObject(true);
    }

    private void handleSend() throws IOException, ClassNotFoundException {
        Mail mail = (Mail) in.readObject();
        String[] to = mail.getTo().split(";");
        System.out.println("Sent mail from " + mail.getFrom() + " to " + mail.getTo());

        if (MailHandler.saveMails(mail)) {
            model.addMessage(mail.getFrom(), "sent mail to " + mail.getTo());

            for (String toUser : to) {
                if (ServerModel.getRegisteredUsers().contains(toUser)) {
                    model.addMessage(toUser, "receive mail from " + mail.getFrom());
                }
            }
            System.out.println("Mail sent correcty");
            out.writeObject(true);
        } else {
            model.addMessage(mail.getFrom(), "fails to spent mail to " + mail.getTo());
            System.out.println("fails to spent mail to " + mail.getTo());
            out.writeObject(false);
        }
    }

    private synchronized void handleRefresh() throws IOException, ClassNotFoundException {
        String address = (String) in.readObject();
        System.out.println(address);
        if(!ServerModel.getConnectedUsers().contains(address))
            ServerModel.addConnectedUser(address);
        System.out.println(ServerModel.getConnectedUsers());
        List<Mail> newMails = new ArrayList<>();
        List<Integer> mailsId = (List<Integer>) in.readObject();
        List<Mail> inboxMails = new ArrayList<>(MailHandler.importEmails(address));

        System.out.println("Refresh for: " + address);

        if (!mailsId.isEmpty()) {
            if (inboxMails.size() != mailsId.size()) {
                for (Mail mail : inboxMails) {
                    if (!mailsId.contains(mail.getID())) {
                        newMails.add(mail);
                    }
                }
            }
        } else {
            newMails = inboxMails;
        }

        out.writeObject(newMails);
        System.out.println("Invio nuove mail al client: " + newMails.size() + " mail trovate");
    }

    private void handleDelete() throws IOException, ClassNotFoundException {
        String address = (String) in.readObject();
        int id = (int) in.readObject();
        System.out.println("Delete mail with ID: " + id);

        if (MailHandler.removeMail(address, id)) {
            System.out.println("Mail deleted correctly.");
            out.writeObject(true);
            model.addMessage(address, "Delete mail with id: " + id);
        } else {
            System.out.println("Error with elimination of mail.");
            out.writeObject(false);
            model.addMessage(address, "Impossible to delete mail with id: " + id);
        }
    }
}