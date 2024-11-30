package com.example.progetto_progiii.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Inbox {
    private int currentIdMail;
    static int currentSelectedMail = -1; // clicked last time on this object displayed (to check -1)
    private final StringProperty userMail = new SimpleStringProperty();
    private final ObservableList<Mail> inbox;

    public ObservableList<Mail> getMails() {
        return inbox;
    }

    public Inbox(String userMail) {
        setUserMail(userMail);
        this.inbox = FXCollections.observableArrayList();
        currentIdMail = this.inbox.size();
    }

    public Inbox() {
        this.inbox = FXCollections.observableArrayList();
    }

    public StringProperty userMailProperty() {return userMail;}

    public String getUserMail() {return userMail.get();}

    public int getCurrentIdMail() {return currentIdMail;}

    public void setUserMail(String userMail) {this.userMail.set(userMail);}

    public Mail getMail(int id){
        return inbox.get(id);
    }

    public Mail getCurrentSelectedMail() {
        return inbox.get(currentSelectedMail);
    }

    public void addMail(Mail mail){
        this.inbox.add(mail); //O(1) time complexity
    }

    public static class Mail{
        private String from;
        private List<String> to;
        private String subject;
        private String body;
        private final LocalDateTime date_time;

        public Mail(String from, List<String> to, String subject, String body, LocalDateTime dateTime) {
            this.from = from;
            this.to = to; //to_check
            this.subject = subject;
            this.body = body;
            this.date_time = dateTime;
        }

        public String getFrom() {
            return from;
        }

        public List<String> getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public LocalDateTime getDate() {
            return date_time;
        }

        public String getDateFormatted() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return date_time.format(formatter);
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setTo(List<String> to) {
            this.to = to; //to_check
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void setBody(String body) {
            this.body = body;
        }
    } //TO CHECK WHETHER PUBLIC IS CORRECT
}
