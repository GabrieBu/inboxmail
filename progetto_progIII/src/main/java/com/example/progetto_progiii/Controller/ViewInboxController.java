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
import javafx.scene.shape.Circle;
import javafx.stage.WindowEvent;
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
import javafx.collections.FXCollections;

public class ViewInboxController{
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private Inbox inbox;
    private Future<?> listenerFuture;
    private final ExecutorService listenerExecutor = Executors.newFixedThreadPool(2);
    private String STATE_FUNC = "";
    @FXML
    private TextField toTextField;

    @FXML
    private TextField textFieldProva;

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
    private Circle statusCircle;


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
        FXCollections.reverse(mailObservableList);
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
        ServerConnection(textFieldProva, statusCircle);
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
                labelErrorTo.setText("Invalid email addresses within recipients");
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
        System.out.println("State function: " + STATE_FUNC);

        switch(STATE_FUNC){
            case "send":
                jsonObject.addProperty("type", "send");
                mailJsonObj.addProperty("from", textFieldUsermail.textProperty().get());
                mailJsonObj.add("to", toArray);
                mailJsonObj.addProperty("subject", subjectTextField.textProperty().get());
                mailJsonObj.addProperty("body", bodyTextArea.getText());
                mailJsonObj.addProperty("date", LocalDateTime.now().toString());
                jsonObject.add("mail", mailJsonObj);
                break;
            case "reply":
                jsonObject.addProperty("type", "reply");
                mailJsonObj.addProperty("from", this.inbox.getUserMail());
                mailJsonObj.add("to", toArray);
                mailJsonObj.addProperty("subject", subjectTextField.getText());
                mailJsonObj.addProperty("body", bodyTextArea.getText());
                mailJsonObj.addProperty("date", LocalDateTime.now().toString());
                jsonObject.add("mail", mailJsonObj);
                break;
            case "reply_all":
                jsonObject.addProperty("type", "reply_all");
                mailJsonObj.addProperty("from", this.inbox.getUserMail());
                mailJsonObj.add("to", toArray);
                mailJsonObj.addProperty("subject", subjectTextField.getText());
                mailJsonObj.addProperty("body", bodyTextArea.getText());
                mailJsonObj.addProperty("date", LocalDateTime.now().toString());
                jsonObject.add("mail", mailJsonObj);

                break;
            case "forward":
                jsonObject.addProperty("type", "forward");
                mailJsonObj.addProperty("from", this.inbox.getUserMail());
                mailJsonObj.add("to", toArray);
                mailJsonObj.addProperty("subject", subjectTextField.getText());
                mailJsonObj.addProperty("body", bodyTextArea.getText());
                mailJsonObj.addProperty("date", LocalDateTime.now().toString());
                jsonObject.add("mail", mailJsonObj);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + STATE_FUNC);
        }

        writeOnSocket(jsonObject);

        toTextField.clear();
        subjectTextField.clear();
        bodyTextArea.clear();
        labelErrorTo.setVisible(false);
        labelErrorSubject.setVisible(false);
        composePanel.setVisible(false);
    }

    public void showWritePanel(ActionEvent actionEvent) {
        this.setSTATE_FUNC("send");
        clearAndEnable();
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
    public void shutdown(WindowEvent event) {
        //inviare un messaggio al server per far cancellare il valore dalla hashmap
        try {
            stopListener();      // Ferma thread del listener
            stopServerCheckConnection(); // Chiude eventuali connessioni aperte al server
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Chiude l'app JavaFX e forza l'uscita dalla JVM
            Platform.exit();
            System.exit(0);
        }
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

    public void ServerConnection(TextField textFieldProva, Circle statusCircle) {
        if (this.inbox == null) {
            throw new IllegalStateException("Inbox must be initialized before starting the listener.");
        }

        ServerCheckerCallable ServerChecker= new ServerCheckerCallable(8189, textFieldProva, statusCircle);
        listenerFuture=listenerExecutor.submit(ServerChecker);
    }

    public void stopServerCheckConnection() {

    }

    public void handlerReply(ActionEvent actionEvent) {
        clearAndEnable();
        composePanel.setVisible(true);
        Inbox.Mail currentMail = listViewMails.getSelectionModel().getSelectedItem();

        if(currentMail != null) {
            this.setSTATE_FUNC("reply");
            toTextField.setText(currentMail.getFrom());
            toTextField.setEditable(false);
            subjectTextField.setText("Re:" + currentMail.getSubject());
            subjectTextField.setEditable(false);
        }
        //reply
        //bodyTextArea.setText(currentMail.getFrom()+": "+ currentMail.getBody()+"\n\n\n");
        bodyTextArea.setText(currentMail.getBody()+"\n\n\n");

    }

    private static void writeOnSocket(JsonObject jsonObject){
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

    private void clearAndEnable(){
        toTextField.clear();
        toTextField.setEditable(true);
        subjectTextField.clear();
        subjectTextField.setEditable(true);
        bodyTextArea.clear();
        bodyTextArea.setEditable(true);
    }

    public void handlerReplyAll(ActionEvent actionEvent) {
        clearAndEnable();
        composePanel.setVisible(true);

        Inbox.Mail currentMail = listViewMails.getSelectionModel().getSelectedItem();
        if(currentMail != null) {
            this.setSTATE_FUNC("reply_all");
            List<String> recipients = currentMail.getTo();
            StringBuilder recipientListBuilder = new StringBuilder();
            for (String recipient : recipients) {
                if (!recipient.equals(this.inbox.getUserMail())) {
                    recipientListBuilder.append(recipient).append(", ");
                }
            }
            recipientListBuilder.append(currentMail.getFrom());
            String recipientList = recipientListBuilder.toString();
            toTextField.setText(recipientList);
            toTextField.setEditable(false);
            subjectTextField.setText("Re: " + currentMail.getSubject());
            subjectTextField.setEditable(false);
        }
        bodyTextArea.setText(currentMail.getBody()+"\n\n\n ");

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
        this.setSTATE_FUNC("forward");
        clearAndEnable();
        composePanel.setVisible(true);
        Inbox.Mail currentMail = listViewMails.getSelectionModel().getSelectedItem();
        if (currentMail != null) {
            bodyTextArea.setText("From: " + currentMail.getFrom() + "\n\n\n" + currentMail.getBody());
            subjectTextField.setText("Fw: " + currentMail.getSubject());
            subjectTextField.setEditable(false);
            bodyTextArea.setEditable(false);
            toTextField.clear();
        }
    }

    public void setSTATE_FUNC(String STATE_FUNC) {
        this.STATE_FUNC = STATE_FUNC;
    }
}
