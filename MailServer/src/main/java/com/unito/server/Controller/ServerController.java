package com.unito.server.Controller;

import com.unito.server.DataModel.ServerModel;
import com.unito.server.Message;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    @FXML private TableView<Message> table;
    @FXML private TableColumn<Message, String> user;
    @FXML private TableColumn<Message, String> message;
    private final ObservableList<Message> data = FXCollections.observableArrayList();
    private final ServerModel model = ServerModel.getInstance(); // Istanza di ServerModel

    public void initialize(URL location, ResourceBundle resources){
        message.setPrefWidth(380);
        user.setPrefWidth(120);

        message.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getMessage()));
        user.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getUser()));

        // Collegare la lista alla TableView
        table.setItems(ServerModel.getMessages());
    }
}

