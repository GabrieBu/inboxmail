package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewInboxController{
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private Inbox inbox;

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
        System.out.println("Model Inbox has been initialized [ViewInboxController]");
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
                displayTo.setText(currentMail.getSubject());
                displayBody.setText(currentMail.getBody());
                displayDate.setText(currentMail.getDateFormatted());
                displayFrom.setText("From: " + currentMail.getFrom());
            }
        });

        listViewMails.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("clicked on " + listViewMails.getSelectionModel().getSelectedItem().getSubject());
            }
        });
    }

    private boolean validate(String email){
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }


    public boolean sendEmail(ActionEvent actionEvent) {
        labelErrorTo.setVisible(false);
        labelErrorSubject.setVisible(false);
        String[] recipients = toTextField.getText().split(", ");
        JsonArray toArray = new JsonArray();
        for(String recipient : recipients){
            if(!validate(recipient)) {
                labelErrorTo.setVisible(true);
                labelErrorTo.setText("Invalid email address within recipients");
                return false;
            }
            toArray.add(recipient);
        }

        if(subjectTextField.getText().isEmpty()){
            labelErrorTo.setVisible(false);
            labelErrorSubject.setVisible(true);
            labelErrorSubject.setText("Subject can not be empty");
            return false;
        }

        /*int id_mail = 1;
        Inbox.Mail new_mail = new Inbox.Mail(id_mail, textFieldUsermail.textProperty().get(), recipients, subjectTextField.textProperty().get(), bodyTextArea.getText(), LocalDateTime.now());
        inbox.addMail(new_mail);*/

        JsonObject jsonObject = new JsonObject();
        JsonObject mailJsonObj = new JsonObject();
        jsonObject.addProperty("type", "send");
        jsonObject.addProperty("id", String.valueOf(this.inbox.getCurrentIdMail()));
        mailJsonObj.addProperty("from", textFieldUsermail.textProperty().get());
        mailJsonObj.add("to", toArray);
        mailJsonObj.addProperty("subject", subjectTextField.textProperty().get());
        mailJsonObj.addProperty("body", bodyTextArea.getText());
        mailJsonObj.addProperty("date", LocalDateTime.now().toString());
        jsonObject.add("mail", mailJsonObj);
        System.out.println(jsonObject);
        try {
            Socket socket = new Socket("localhost", 8189);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
            writer.println(jsonObject);
            System.out.println(jsonObject.toString());
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
        return true;
    }

    public void showWritePanel(ActionEvent actionEvent) {
        composePanel.setVisible(true);
    }

    public void closePanelSendEmail(ActionEvent actionEvent) {
        composePanel.setVisible(false);
    }
}
