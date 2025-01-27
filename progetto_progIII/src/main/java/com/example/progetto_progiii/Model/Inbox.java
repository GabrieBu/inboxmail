package com.example.progetto_progiii.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Inbox {
    private long idLastMail;
    private final StringProperty userMail = new SimpleStringProperty();
    private final ObservableList<Mail> inbox;

    public Inbox() {
        this.inbox = FXCollections.observableArrayList();
        idLastMail =  Long.MIN_VALUE;
    }

    public ObservableList<Mail> getMails() {
        return inbox;
    }

    public long getIdLastMail() {
        return idLastMail;
    }

    public void setIdLastMail(long idLastMail) {
        this.idLastMail = idLastMail;
    }

    /* Property */
    public StringProperty userMailProperty() {return userMail;}

    public String getUserMail() {return userMail.get();}

    public void setUserMail(String userMail) {this.userMail.set(userMail);}
    /* ** */

    public static class Mail{
        private long id;
        private final String from;
        private final String[] to;
        private final String subject;
        private final String body;
        private final LocalDateTime date_time;

        public Mail(String from, String[] to, String subject, String body, LocalDateTime dateTime) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.date_time = dateTime;
        }

        public Mail(long id, String from, String[] to, String subject, String body, LocalDateTime dateTime) {
            this.id = id;
            this.from = from;
            this.to = to; //to_check
            this.subject = subject;
            this.body = body;
            this.date_time = dateTime;
        }

        public long getId(){
            return id;
        }

        public void setId(long id){
            this.id = id;
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
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            if (date_time.toLocalDate().equals(LocalDate.now())) {
                DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm");
                return date_time.format(timeOnlyFormatter);
            } else {
                return date_time.format(fullFormatter);
            }
        }
    }
}
