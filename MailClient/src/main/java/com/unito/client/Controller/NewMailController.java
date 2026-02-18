package com.unito.client.Controller;

import com.fasterxml.jackson.core.JsonToken;
import com.unito.client.ClientRequest;
import com.unito.client.DataModel.Model;
import com.unito.client.ThreadPoolManager;
import com.unito.client.ViewSwitcher;
import com.unito.common.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NewMailController implements Initializable {
    private User user;
    String address;
    private Mail currentMail;
    private String getTo;
    public boolean isReply;
    public boolean isReplyAll;
    public boolean isForward;
    Model model = Model.getInstance();
    private final ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();

    @FXML private Label from;
    @FXML private TextField to;
    @FXML private Label toReply;
    @FXML private TextField object;
    @FXML private Label objectReply;
    @FXML private TextArea content;
    @FXML private Label negativeResponseTo;
    @FXML private Label negativeResponseObject;
    @FXML private Label negativeResponseContent;
    @FXML private Button send;

    public void initialize(URL location, ResourceBundle resources) {
        currentMail = model.getSelectedMail();
        user = UserSession.getCurrentUser();
        address = model.getEmailAddressProperty().get();
        from.setText(address);
    }

    public void configure(boolean isReply, boolean isReplyAll, boolean isForward) {
        this.isReply = isReply;
        this.isReplyAll = isReplyAll;
        this.isForward = isForward;
        if (isReply) {
            if(currentMail != null) {
                to.setVisible(false);
                object.setVisible(false);
                toReply.setVisible(true);
                objectReply.setVisible(true);
                toReply.setText(currentMail.getFrom());
                objectReply.setText(currentMail.getObject());
            }
            else{
                System.err.println("Selezionare una mail per rispondere");
            }
        }
        if(isReplyAll){
            if(currentMail != null) {
                to.setVisible(false);
                object.setVisible(false);
                toReply.setVisible(true);
                objectReply.setVisible(true);
                System.out.println(currentMail.getTo());
                System.out.println(user.getAddress());
                if (currentMail.getTo().contains(user.getAddress())) {
                    getTo = currentMail.getFrom() + ";" + currentMail.getTo();
                    getTo = getTo.replace(";" +user.getAddress(), "");
                    System.out.println(getTo);
                    toReply.setText(getTo);
                }
                objectReply.setText(currentMail.getObject());
            }
            else{
                System.err.println("Selezionare una mail per rispondere");
            }
        }
        if(isForward) {
            if (currentMail != null) {
                object.setText(currentMail.getObject());
                content.setText("- Messaggio inoltrato -" + "\n"
                        + "From: " + currentMail.getFrom() + "\n"
                        + "Date: " + currentMail.getDate() + "\n"
                        + "Object: " + currentMail.getObject() + "\n"
                        + "To: " + currentMail.getTo() + "\n"
                        + "Content:" + "\n" + currentMail.getContent() + "\n"
                        + " --- ");
            } else {
                System.err.println("Selezionare una mail per rispondere");
            }
        }
    }

    public void handleSendMail() {
        if ((!isReply && !isReplyAll && !isForward) && (to.getText().isEmpty() || object.getText().isEmpty() || content.getText().isEmpty())) {
            if(to.getText().isEmpty()) {
                negativeResponseTo.setText("the field to is missing");
                negativeResponseTo.setVisible(to.getText().isEmpty());
            } else if (!to.getText().contains(";")){
                if(!validate(to.getText())){
                    negativeResponseTo.setText("Invalid email. Try again");
                    negativeResponseTo.setVisible(true);
                }
            }
            negativeResponseContent.setVisible(content.getText().isEmpty());
            negativeResponseObject.setVisible(object.getText().isEmpty());
        }
        else {
            if(!isReply && !isReplyAll) {
                if(to.getText().isEmpty()) {
                    negativeResponseTo.setText("the field to is missing");
                    negativeResponseTo.setVisible(to.getText().isEmpty());
                    return;
                } else if(!to.getText().contains(";")){
                    if(!validate(to.getText())) {
                        negativeResponseTo.setText("Invalid email. Try again");
                        negativeResponseTo.setVisible(true);
                        return;
                    }
                }
            }
            if (content.getText().isEmpty()) {
                negativeResponseContent.setVisible(content.getText().isEmpty());
                // negativeResponseObject.setVisible(object.getText().isEmpty());
                return; // Esce senza inviare richiesta
            }
            ClientRequest sendRequest = new ClientRequest(this.user, getSelectedMail(), "send", 12345);
            threadPoolManager.submitRequest(sendRequest);
            Object result = sendRequest.get();
            Platform.runLater(() -> {
                if (result instanceof Boolean && (Boolean) result) {
                    closeWindow();
                } else if (result instanceof String) {
                    negativeResponseTo.setText((String) result); // Mostra l'errore specifico
                    negativeResponseTo.setVisible(true);
                } else {
                    negativeResponseTo.setText("This address is not registred");
                    negativeResponseTo.setVisible(true);
                }
            });
        }
    }

    private void closeWindow() {
        // Ottieni il Stage dalla scena
        Stage stage = (Stage) send.getScene().getWindow();
        if (stage != null) {
            stage.close();  // Chiudi la finestra
        }
    }

    Mail getSelectedMail() {
        if(isReply || isReplyAll) return new Mail(from.getText(), toReply.getText(), objectReply.getText(), content.getText());
        else return new Mail(from.getText(), to.getText(), object.getText(), content.getText());
    }

    public boolean validate(String address) {
        return address.matches("^[a-zA-Z0-9._%+-]+@gmail\\.[a-zA-Z]{2,}$");
    }
}
