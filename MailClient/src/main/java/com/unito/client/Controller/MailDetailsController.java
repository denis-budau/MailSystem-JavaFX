package com.unito.client.Controller;

import com.unito.client.DataModel.Model;
import com.unito.common.Mail;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MailDetailsController implements Initializable {
    @FXML private Label from;
    @FXML private Label to;
    @FXML private Label object;
    @FXML private Label content;
    @FXML private Label date;
    Model model = Model.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Mail currentMail = model.getSelectedMail();
        if(currentMail != null) {
            from.setText(currentMail.getFrom());
            to.setText(currentMail.getTo());
            object.setText(currentMail.getObject());
            content.setText(currentMail.getContent());
            date.setText(currentMail.getDate());}
    }
}