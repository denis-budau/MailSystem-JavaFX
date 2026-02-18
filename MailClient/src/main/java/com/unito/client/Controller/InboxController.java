package com.unito.client.Controller;

import com.unito.client.ClientRequest;
import com.unito.client.DataModel.Model;
import com.unito.client.ThreadPoolManager;
import com.unito.client.ViewSwitcher;
import com.unito.common.*;
import com.unito.common.UserSession;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InboxController implements Initializable {
    private User user;
    @FXML private Button reply;
    @FXML private Button replyAll;
    @FXML private Button delete;
    @FXML private Button newMail;
    @FXML private Button logout;
    @FXML private Button forward;
    @FXML private ListView emailList;
    @FXML private Label notify;
    @FXML private Label error;
    @FXML private Label connect;
    @FXML private Label noConnect;
    @FXML private Label name;
    Model model = Model.getInstance();
    private final ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
    private java.util.concurrent.ScheduledFuture<?> refreshTask;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configura l'utente e la visualizzazione della mail
        name.textProperty().bind(model.getEmailAddressProperty());
        user = UserSession.getCurrentUser();
        reply.setDisable(true);
        replyAll.setDisable(true);
        delete.setDisable(true);
        forward.setDisable(true);

        // Esegui il refresh periodico ogni 4 secondi
        try {
            initializeUser(user);
        } catch (InterruptedException e) {
            e.printStackTrace();  // Gestione dell'eccezione
        }

        // Ascolta la selezione della mail
        emailList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Mail>() {
            @Override
            public void changed(ObservableValue<? extends Mail> observableValue, Mail mail, Mail selectedMail) {
                if (selectedMail != null) {
                    model.setSelectedMail(selectedMail);
                    handleMailSelection();
                }
            }
        });
    }

    // Inizializzazione dell'utente e schedulazione del refresh
    public void initializeUser(User user) throws InterruptedException {
        this.user = user;
        model.setActiveProperty(true);

        refreshTask = exec.scheduleAtFixedRate(() -> {
            try {
                ClientRequest refreshRequest = new ClientRequest(user, "refresh", 12345);
                threadPoolManager.submitRequest(refreshRequest);

                Object result = refreshRequest.get(); // Ottieni l'output

                if (result instanceof List<?>) {
                    System.out.println("yaa");
                    Platform.runLater(() -> {
                        notify.setVisible(true);
                        noConnect.setVisible(false);
                        error.setVisible(false);
                        connect.setVisible(true);
                    });
                } else if(result instanceof String) {
                    System.out.println(result);
                    Platform.runLater(() -> {
                        connect.setVisible(false);
                        noConnect.setVisible(true);
                        error.setText("The server is currently unavailable.");
                        error.setVisible(true);
                        notify.setVisible(false);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    connect.setVisible(false);
                    noConnect.setVisible(true);
                });
                e.printStackTrace();
            }
        }, 0, 4, TimeUnit.SECONDS);

        user.inboxProperty().addListener(new ListChangeListener<Mail>() {
            @Override
            public void onChanged(Change<? extends Mail> change) {
                boolean shouldNotify = false;
                if (user.getMailsId().isEmpty() || user.getInbox().isEmpty()) {
                    user.updateIds();
                } else if (user.getMailsId().size() < user.getInbox().size()) {
                    int nmail = user.getInbox().size() - user.getMailsId().size();
                    user.updateIds();
                    shouldNotify = true;
                }
                boolean finalShouldNotify = shouldNotify;
                Platform.runLater(() -> {
                    emailList.getItems().setAll(user.getInbox());
                    if (finalShouldNotify) {
                        notify.setText("NEW MAIL!!");
                        notify.setVisible(true);
                    }
                });
            }
        });
    }


    @FXML
    public void handleLogout() {
        block();
        ClientRequest logoutRequest = new ClientRequest(user, "logout", 12345);
        model.setActiveProperty(false);
        // Invio la richiesta al pool di thread per l'esecuzione
        threadPoolManager.submitRequest(logoutRequest);
        ViewSwitcher.getInstance(null).switchTo("view/login-view.fxml", "Login");
    }

    public void block() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel(true);
        }

        // Ferma completamente l'esecutore
        if (exec != null && !exec.isShutdown()) {
            exec.shutdownNow();
        }
    }

    @FXML
    public void handleMailSelection() {
        // Recupera la mail selezionata
        Mail currentMail = model.getSelectedMail();
        reply.setDisable(false);
        if(!currentMail.getTo().contains(";")) {
            replyAll.setDisable(true);
        }
        else{ replyAll.setDisable(false); }
        delete.setDisable(false);
        forward.setDisable(false);
        if (currentMail != null) {
            ViewSwitcher.getInstance(null).changeTo("view/mailDetails-view.fxml", "Mail details", false, false, false);
        }
        if (currentMail.getID() == user.getLastId()) {
            notify.setVisible(false);
        }
    }

    @FXML
    public void handleReply() {
        ViewSwitcher.getInstance(null).changeTo("view/newMail-view.fxml", "Reply", true, false, false);
    }

    @FXML
    public void handleReplyAll() {
        ViewSwitcher.getInstance(null).changeTo("view/newMail-view.fxml", "Reply all", false, true, false);
    }

    @FXML
    public void handleForward() {
        ViewSwitcher.getInstance(null).changeTo("view/newMail-view.fxml", "Forward", false, false, true);
    }

    @FXML
    public void handleDelete() {
        Mail currentMail = model.getSelectedMail();
        if (currentMail != null) {
            ClientRequest deleteRequest = new ClientRequest(user, currentMail, "delete", 12345);
            threadPoolManager.submitRequest(deleteRequest);
        }
    }

    @FXML
    public void handleNewMail() {
        ViewSwitcher.getInstance(null).changeTo("view/newMail-view.fxml", "New Mail", false, false, false);
    }
}