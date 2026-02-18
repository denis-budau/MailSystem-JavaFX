package com.unito.client.Controller;

import com.unito.common.*;
import com.unito.client.ClientRequest;
import com.unito.client.DataModel.Model;
import com.unito.client.ThreadPoolManager;
import com.unito.client.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController  {
    @FXML private TextField address;
    @FXML private Label negativeResponse;
    Model model = Model.getInstance();
    private final ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();

    public boolean validate(String address) {
        return address.matches("^[a-zA-Z0-9._%+-]+@gmail\\.[a-zA-Z]{2,}$");
    }

    @FXML
    public void handleLogin() {
       String Address = address.textProperty().get();
       model.setEmailAddressProperty(Address);
       try {
          if(!validate(Address)) {
              negativeResponse.setText("Invalid email. Try again");
              return;
          }
          LoginResponse();
       }
       catch (Exception e) {
           e.printStackTrace(); // Mostra il traceback
           negativeResponse.setText("An error occurred during the login process. Please try again later."); // Mostra un messaggio di errore generico all'utente
       }
    }

    private void LoginResponse() throws InterruptedException{
        String Address = address.textProperty().get();
        User user = new User(Address);
        UserSession.setCurrentUser(user);
        ClientRequest loginRequest = new ClientRequest(user, "login", 12345);
        // Invio la richiesta al pool di thread per l'esecuzione
        threadPoolManager.submitRequest(loginRequest);

        // Esegui in un thread separato per non bloccare l'interfaccia
        threadPoolManager.submit(() -> {
            Object result = loginRequest.get();
            Platform.runLater(() -> {
                if (result instanceof Boolean && (Boolean) result) {
                    ViewSwitcher.getInstance(null).switchTo("view/inbox-view.fxml", "Inbox");
                } else if (result instanceof String) {
                    negativeResponse.setText((String) result);
                } else {
                    negativeResponse.setText("This address not exist");
                }
            });
        });
    }
}
