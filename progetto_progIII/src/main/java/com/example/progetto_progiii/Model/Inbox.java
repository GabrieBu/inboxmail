package com.example.progetto_progiii.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Inbox {
    private final StringProperty userMail = new SimpleStringProperty();
    private final ObservableList<Mail> inbox;

    public ObservableList<Mail> getMails() {
        return inbox;
    }

    public Inbox() {
        this.inbox = FXCollections.observableArrayList();
    }

    public StringProperty userMailProperty() {return userMail;}

    public String getUserMail() {return userMail.get();}


    public void setUserMail(String userMail) {this.userMail.set(userMail);}


    public static class Mail{
        private String from;
        private String[] to;
        private String subject;
        private String body;
        private final LocalDateTime date_time;

        public Mail(String from, String[] to, String subject, String body, LocalDateTime dateTime) {
            this.from = from;
            this.to = to; //to_check
            this.subject = subject;
            this.body = body;
            this.date_time = dateTime;
        }

        public String getFrom() {
            return from;
        }

        public String[] getTo() {
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

        public void setTo(String[] to) {
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
