package com.unito.common;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class User implements Serializable {
    private final String address;
    private transient ObservableList<Mail> inbox = FXCollections.observableArrayList();
    private final transient List<Integer> mailsId = new ArrayList<>();

    public User(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void addID(int id){
        mailsId.add(id);
    }

    public void updateIds(){
        for(Mail mail : inbox){
            if(!mailsId.contains(mail.getID())){
                mailsId.add(mail.getID());
            }
        }
    }

    public List<Integer> getMailsId(){
        return mailsId;
    }

    public int getLastId(){
        List<Integer> ID = getMailsId();
        return ID.get(ID.size()-1);
    }

    //property
    public ObservableList<Mail> inboxProperty(){
        return inbox;
    }

    public void setInbox(List<Mail> mails){
        if(mails != null || !(mails.isEmpty())){
            inbox.addAll(mails);
        }
    }

    public List<Mail> getInbox(){
        List<Mail> mails = new ArrayList<>();
        for(Mail mail : inbox){
            mails.add(mail);
        }

        Collections.sort(mails, (new Comparator<Mail>() {
            public int compare(Mail i1, Mail i2) {
                return i1.getID() - i2.getID();
            }
        }).reversed());
        return mails;
    }

    public void removeMail(Mail mail){
        mailsId.remove(mailsId.indexOf(mail.getID()));
        inbox.remove(mail);
    }
}
