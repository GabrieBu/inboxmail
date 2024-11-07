package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;

public class ViewInboxController{
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
    public void initModel(Inbox inbox) {
        if(this.inbox != null){
            throw new IllegalStateException("The inbox has already been initialized");
        }
        this.inbox = inbox;
        System.out.println("Model Inbox has been initialized [ViewInboxController]");
        textFieldUsermail.textProperty().bind(inbox.userMailProperty());

        inbox.loadMails();

        ObservableList<Inbox.Mail> mailObservableList = inbox.getMails();
        listViewMails.setItems(mailObservableList);

        listViewMails.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Inbox.Mail mail, boolean empty) {
                super.updateItem(mail, empty);
                if (empty || mail == null) {
                    setText(null);
                } else {
                    setText(mail.getSubject()); // Display subject, or customize as needed
                }
            }
        });
    }

    public void showWritePanel(ActionEvent actionEvent) {
        composePanel.setVisible(true);
    }

    public void sendEmail(ActionEvent actionEvent) {
        List<String> recipients = Arrays.asList(toTextField.getText().split(","));;
        Inbox.Mail new_mail = new Inbox.Mail(textFieldUsermail.textProperty().get(), recipients, subjectTextField.textProperty().get(), bodyTextArea.getText());
        inbox.addMail(new_mail);
        System.out.println("New mail has been sent");
        toTextField.clear();
        subjectTextField.clear();
        bodyTextArea.clear();
        composePanel.setVisible(false);
    }
}
