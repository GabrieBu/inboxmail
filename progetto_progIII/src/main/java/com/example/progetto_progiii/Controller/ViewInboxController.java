package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ViewInboxController{
    private Inbox inbox;

    private final ObservableList<Inbox.Mail> mailObservableList = FXCollections.observableArrayList(); //remove final later

    @FXML
    private ListView<Inbox.Mail> listViewMails;

    @FXML
    private Button writeButton;

    @FXML
    private DialogPane dialogPane;

    @FXML
    private Label labelUserMail;

    @FXML
    public void initModel(Inbox inbox) {
        if(this.inbox != null){
            throw new IllegalStateException("The inbox has already been initialized");
        }
        this.inbox = inbox;
        System.out.println("Model Inbox has been initialized [ViewInboxController]");
        labelUserMail.textProperty().bind(inbox.userMailProperty());
        inbox.userMailProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal) {
                labelUserMail.setText(newVal);
                // System.out.println("User mail Inbox: " + inbox.getUserMail());
            }
        });

        inbox.loadMails();

        mailObservableList.addAll(inbox.getInbox());
        listViewMails.setItems(mailObservableList);

        listViewMails.setCellFactory(param -> new ListCell<Inbox.Mail>() {
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


    public void showDialog(ActionEvent actionEvent) {
        dialogPane.setVisible(true);
    }

    public void closeDialog(ActionEvent actionEvent) {
        dialogPane.setVisible(false);
    }
}
