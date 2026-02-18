package com.unito.client;

import com.unito.common.Mail;
import com.unito.common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ClientRequest extends Thread {
    private User user;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Object result;
    private String request;
    private Mail mail;
    int port;

    public ClientRequest(User user, String request, int port) {
        this.user = user;
        this.request = request;
        this.port = port;
        System.out.println("ClientRequest - Utente ricevuto: " + user.getAddress());
    }

    public ClientRequest(User user, Mail mail, String request, int port) {
        this.user = user;
        this.request = request;
        this.port = port;
        this.mail = mail;
    }

    public void run() {
        try {
            // Ogni volta che facciamo una richiesta, apriamo una nuova connessione
            socket = new Socket(InetAddress.getLocalHost().getHostName(), port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(request);
            System.out.println("Invio richiesta: " + request);

            switch (request) {
                case "login":
                    out.writeObject(user.getAddress());
                    result = in.readObject();
                    if (!(boolean) result) {
                        System.out.println("Login failed");
                    }
                    break;
                case "logout":
                    out.writeObject(user.getAddress());
                    result = in.readObject();
                    if ((boolean) result) {
                        System.out.println("Logout successful");
                    }
                    break;
                case "send":
                    out.writeObject(mail);
                    result = in.readObject();
                    if ((boolean) result) {
                        System.out.println("Mail sent correctly");
                    }
                    break;
                case "refresh":
                    out.writeObject(user.getAddress());
                    out.writeObject(user.getMailsId());
                    System.out.println("MailsId: " + user.getMailsId());
                    result = in.readObject();
                    if (result instanceof String) {
                        System.out.println("error");
                        break;
                    }
                    user.setInbox((List<Mail>) result);
                    break;
                case "delete":
                    out.writeObject(user.getAddress());
                    out.writeObject(mail.getID());
                    result = in.readObject();
                    System.out.println("User Address: " + user.getAddress());
                    if ((boolean) result) {
                        user.removeMail(mail);
                        System.out.println("Delete successful");
                    }
                    break;
                default:
                    System.out.println("Request not recognized: " + request);
            }
        } catch (java.net.ConnectException ce) {
            System.out.println("Il server non e disponibile al momento.");
            result = "Errore: il server non e disponibile. Riprova piu tardi.";
        } catch (java.net.SocketException se) {
            System.out.println("Connessione interrotta bruscamente.");
            result = "Errore: la connessione con il server è stata interrotta.";
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            result = "Errore di connessione! " + e.getMessage();
        } finally {
            synchronized (this) {
                notifyAll();
            }
            try {
                // Chiudere la connessione dopo l'uso, come in HTTP
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("Socket chiuso correttamente.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public synchronized Object get() {
        while (result == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // buona pratica: ripristina lo stato di interruzione
                System.out.println("Thread interrotto mentre attendeva il risultato.");
            }
        }
        return result;
    }
}
