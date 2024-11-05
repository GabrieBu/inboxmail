package com.example.progetto_progiii.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.LinkedList;
import java.util.List;
import java.time.LocalDateTime;

public class Inbox {
    static int currentIdMail = 0;
    static int currentSelectedMail = 0; // clicked last time on this object displayed
    private final StringProperty userMail = new SimpleStringProperty();
    private LinkedList<Mail> inbox;

    public Inbox(String userMail) {
        setUserMail(userMail);
        this.inbox = new LinkedList<Mail>();
    }

    public Inbox() {
        this.inbox = new LinkedList<Mail>();
    }

    public StringProperty userMailProperty() {return userMail;}

    public String getUserMail() {return userMail.get();}

    public void setUserMail(String userMail) {this.userMail.set(userMail);}

    public List<Mail> getInbox() {
        return inbox;
    }

    public Mail getMail(int id){
        return inbox.get(id);
    }

    public boolean addMail(Mail mail){
        this.inbox.addFirst(mail); //O(1) time complexity
        return true;
    }

    public boolean removeMail(int id){
        if(id >= 0 && id < currentIdMail){
            for (Mail mail : inbox) {
                if (mail.getId() == id) {
                    inbox.remove(mail);
                    return true;
                }
            }
        }
        return false;
    }

    public void loadMails(){
        List<String> to = new LinkedList<>();
        to.add("abc@gmail.com");
        for(int i = 0; i < 10; i++){
            Mail newMail = new Mail(this.getUserMail(), to, "Subject" + i, "Body" + i);
            inbox.add(newMail);
        }
    }



    public static class Mail{
        private final int idMail;
        private String from;
        private List<String> to;
        private String subject;
        private String body;
        private final LocalDateTime date_time;

        public Mail(String from, List<String> to, String subject, String body) {
            this.idMail = currentIdMail;
            this.from = from;
            this.to = to; //to_check
            this.subject = subject;
            this.body = body;
            this.date_time = LocalDateTime.now();
            currentIdMail++;
        }

        public int getId() {
            return idMail;
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

        public String getBody(String body) {
            return body;
        }

        public LocalDateTime getDate(String body) {
            return date_time;
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
