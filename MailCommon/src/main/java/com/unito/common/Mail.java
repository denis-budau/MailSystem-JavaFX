package com.unito.common;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Mail implements Serializable {
    private int id;
    private String from;
    private String to;
    private String object;
    private String content;
    private Date date;

    public Mail(int ID, String from, String to, String object, String content, Date date) {
        this.id = ID;
        this.from = from;
        this.to = to;
        this.object = object;
        this.content = content;
        this.date = date;
    }

    public Mail(String from, String to, String object, String content) {
        this.from = from;
        this.to = to;
        this.object = object;
        this.content = content;
        this.date = new Date();
    }

    public int getID() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String subject) {
        this.object = subject;
    }

    public String getContent() {
        return content;
    }

    public String getContentNoLineBreaks() {
        String content = this.getContent();
        if (content.startsWith("- Messaggio inoltrato -")) {
            String contentNoLineBreaks = content.replaceAll("[\\r\\n]+", "");
            return contentNoLineBreaks;
        } else {
            return content;
        }
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return dateFormat.format(this.date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return object + "\n" + "Da: " + from + "\n" + this.getDate();
    }
}