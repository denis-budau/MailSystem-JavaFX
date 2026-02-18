package com.unito.client.DataModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import com.unito.common.Mail;

public class Model {
    private static Model instance;
    private final SimpleStringProperty emailAddressProperty;
    private final SimpleStringProperty negativeResponseProperty;
    private final ObjectProperty<Mail> selectedMailProperty;
    private final SimpleBooleanProperty activeProperty;


    public SimpleStringProperty getEmailAddressProperty() { return emailAddressProperty; }
    public SimpleStringProperty getNegativeResponse() { return negativeResponseProperty; }
    public SimpleBooleanProperty getActiveProperty() { return activeProperty; }
    public Mail getSelectedMail() { return selectedMailProperty.get(); }

    public void setSelectedMail(Mail mail) { this.selectedMailProperty.set(mail); }
    public void setEmailAddressProperty(String address) { this.emailAddressProperty.set(address); }
    public void setActiveProperty(boolean active){ this.activeProperty.set(active);}

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public Model() {
        emailAddressProperty = new SimpleStringProperty();
        negativeResponseProperty = new SimpleStringProperty();
        selectedMailProperty = new SimpleObjectProperty<>();
        activeProperty = new SimpleBooleanProperty();
    }
}