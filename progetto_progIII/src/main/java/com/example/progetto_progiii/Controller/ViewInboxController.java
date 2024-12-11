package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewInboxController{
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private Inbox inbox;
    private Future<?> listenerFuture;
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();

    @FXML
    private TextField toTextField;

    @FXML
    private TextField subjectTextField;

    @FXML
    private TextArea bodyTextArea;

    @FXML
    private ListView<Inbox.Mail> listViewMails;

    @FXML
    private VBox composePanel;

    @FXML
    private TextField textFieldUsermail;

    @FXML
    private TextField displayTo;

    @FXML
    private TextField displayFrom;

    @FXML
    private TextField displayDate;

    @FXML
    private TextArea displayBody;

    @FXML
    private Label labelErrorTo;

    @FXML
    private Label labelErrorSubject;

    @FXML
    public void initModel(Inbox inbox) {
        if(this.inbox != null){
            throw new IllegalStateException("The inbox has already been initialized");
        }
        this.inbox = inbox;
        try (ServerSocket serverSocket = new ServerSocket(0);) {
            this.inbox.setPortClient(serverSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Socket socket = new Socket("localhost", 8189);) {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
            writer.println(pack(this.inbox.getUserMail(), this.inbox.getPortClient()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        textFieldUsermail.textProperty().bind(inbox.userMailProperty());

        ObservableList<Inbox.Mail> mailObservableList = inbox.getMails();
        listViewMails.setItems(mailObservableList);

        listViewMails.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Inbox.Mail mail, boolean empty) {
                super.updateItem(mail, empty);
                if (empty || mail == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label senderLabel = new Label(mail.getFrom());
                    Label subjectLabel = new Label(" - " + mail.getSubject());
                    Label dateLabel = new Label(" " + mail.getDateFormatted());
                    senderLabel.setStyle("-fx-font-weight: bold;");
                    dateLabel.setStyle("-fx-font-weight: bold;");
                    HBox content = new HBox(senderLabel, subjectLabel, dateLabel);
                    content.setSpacing(5);
                    setGraphic(content);
                    setText(null);
                }
            }
        });


        listViewMails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Inbox.Mail>() {
            @Override
            public void changed(ObservableValue<? extends Inbox.Mail> observableValue, Inbox.Mail inbox, Inbox.Mail t1) {
                Inbox.Mail currentMail = listViewMails.getSelectionModel().getSelectedItem();
                if(currentMail != null) { //check se qui o model
                    displayTo.setText(currentMail.getSubject());
                    displayBody.setText(currentMail.getBody());
                    displayDate.setText(currentMail.getDateFormatted());
                    displayFrom.setText("From: " + currentMail.getFrom());
                }
            }
        });

        startListener();
    }

    private String pack(String typedMail, int port){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "handshake");
        jsonObject.addProperty("typed_mail_user", typedMail);
        jsonObject.addProperty("port", String.valueOf(port));
        return jsonObject.toString();
    }

    private boolean validate(String email){
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }

    public void handlerSend(ActionEvent actionEvent) {
        labelErrorTo.setVisible(false);
        labelErrorSubject.setVisible(false);
        String[] recipients = toTextField.getText().split(", ");
        JsonArray toArray = new JsonArray();
        for(String recipient : recipients){
            if(!validate(recipient)) {
                labelErrorTo.setVisible(true);
                labelErrorTo.setText("Invalid email address within recipients");
                return;
            }
            toArray.add(recipient);
        }

        if(subjectTextField.getText().isEmpty()){
            labelErrorTo.setVisible(false);
            labelErrorSubject.setVisible(true);
            labelErrorSubject.setText("Subject can not be empty");
            return;
        }

        JsonObject jsonObject = new JsonObject();
        JsonObject mailJsonObj = new JsonObject();
        jsonObject.addProperty("type", "send");
        mailJsonObj.addProperty("from", textFieldUsermail.textProperty().get());
        mailJsonObj.add("to", toArray);
        mailJsonObj.addProperty("subject", subjectTextField.textProperty().get());
        mailJsonObj.addProperty("body", bodyTextArea.getText());
        mailJsonObj.addProperty("date", LocalDateTime.now().toString());
        jsonObject.add("mail", mailJsonObj);

        try {
            Socket socket = new Socket("localhost", 8189);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
            writer.println(jsonObject);
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        toTextField.clear();
        subjectTextField.clear();
        bodyTextArea.clear();
        labelErrorTo.setVisible(false);
        labelErrorSubject.setVisible(false);
        composePanel.setVisible(false);
    }

    public void showWritePanel(ActionEvent actionEvent) {
        composePanel.setVisible(true);
    }

    public void closePanelSendEmail(ActionEvent actionEvent) {
        composePanel.setVisible(false);
    }

    public void deleteMail(ActionEvent actionEvent) {
        int indexToRemove = listViewMails.getSelectionModel().getSelectedIndex();
        String user = textFieldUsermail.textProperty().get();
        inbox.getMails().remove(indexToRemove);
        try {
            Socket socket = new Socket("localhost", 8189);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "delete");
            jsonObject.addProperty("user", user);
            jsonObject.addProperty("index_to_remove", String.valueOf(indexToRemove));
            writer.println(jsonObject);
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void shutdown() {
        stopListener();
    }

    public void stopListener() {
        if (listenerFuture != null && !listenerFuture.isDone()) {
            listenerFuture.cancel(true); // interrupt the listener thread
        }
        listenerExecutor.shutdown();
        try {
            if (!listenerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                listenerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            listenerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void startListener() {
        if (this.inbox == null) {
            throw new IllegalStateException("Inbox must be initialized before starting the listener.");
        }
        if (this.inbox.getPortClient() <= 0) {
            throw new IllegalStateException("Invalid portClient value. Listener cannot start.");
        }

        EmailListenerCallable emailListener = new EmailListenerCallable(inbox);
        listenerFuture = listenerExecutor.submit(emailListener);
    }

    public void handlerReply(ActionEvent actionEvent) {
        showWritePanel(actionEvent);

        Inbox.Mail currentMail = listViewMails.getSelectionModel().getSelectedItem();
        JsonArray toArray = new JsonArray();
        toArray.add(currentMail.getFrom());
        JsonObject jsonObject = new JsonObject();
        JsonObject mailJsonObj = new JsonObject();
        jsonObject.addProperty("type", "reply");
        mailJsonObj.addProperty("from", this.inbox.getUserMail());
        mailJsonObj.add("to", toArray);
        mailJsonObj.addProperty("subject", currentMail.getSubject() + "REPLY");
        mailJsonObj.addProperty("body", currentMail.getBody());
        mailJsonObj.addProperty("date", LocalDateTime.now().toString());
        jsonObject.add("mail", mailJsonObj);

        try{
            Socket socket = new Socket("localhost", 8189);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
            writer.println(jsonObject);
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void handlerReplyAll(ActionEvent actionEvent) {
        Inbox.Mail currentMail = listViewMails.getSelectionModel().getSelectedItem();
        JsonArray toArray = convertToJsonArray(currentMail.getTo(), this.inbox.getUserMail());
        toArray.add(currentMail.getFrom());
        JsonObject jsonObject = new JsonObject();
        JsonObject mailJsonObj = new JsonObject();
        jsonObject.addProperty("type", "reply_all");
        mailJsonObj.addProperty("from", this.inbox.getUserMail());
        mailJsonObj.add("to", toArray);
        mailJsonObj.addProperty("subject", currentMail.getSubject() + "REPLYALL");
        mailJsonObj.addProperty("body", currentMail.getBody());
        mailJsonObj.addProperty("date", LocalDateTime.now().toString());
        jsonObject.add("mail", mailJsonObj);

        try{
            Socket socket = new Socket("localhost", 8189);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
            writer.println(jsonObject);
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public JsonArray convertToJsonArray(List<String> toList, String currentUserMail) {
        JsonArray jsonArray = new JsonArray();

        if (toList != null) {
            for (String str : toList) {
                System.out.println(str + "added!");
                if (!currentUserMail.equals(str)) {
                    jsonArray.add(str);
                }
            }
        }
        return jsonArray;
    }

    public void handlerForward(ActionEvent actionEvent) {

    }
}
