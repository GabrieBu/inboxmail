package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.*;
import java.net.*;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewAuthController{
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private Inbox inbox;
    private Stage stage;
    private Scene sceneInbox; // Reference to the Inbox scene
    private ViewInboxController viewInboxController;

    @FXML
    private TextField textboxEmail;

    @FXML
    private Label labelError;

    @FXML
    public void initModel(Inbox inbox, Stage stage, Scene sceneInbox, ViewInboxController viewInboxController) {
        if(this.inbox != null){
            throw new IllegalStateException("The inbox has already been initialized");
        }
        this.inbox = inbox;
        this.stage = stage;
        this.sceneInbox = sceneInbox;
        this.viewInboxController = viewInboxController;
        labelError.setVisible(false);
        System.out.println("Model Inbox has been initialized [ViewAuthController]");
    }

    private boolean validate(String email){
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }

    private String pack(String typedMail, int port){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "authentication");
        jsonObject.addProperty("typed_mail_user", typedMail);
        jsonObject.addProperty("port", port);

        System.out.println(jsonObject);
        return jsonObject.toString();
    }

    public void handlerOpenInbox(ActionEvent actionEvent) {
        String typedMail = textboxEmail.getText();
        if (validate(typedMail)) {
            inbox.setUserMail(typedMail);
            try ( ServerSocket serverSocket = new ServerSocket(0);) {

                int portClient = serverSocket.getLocalPort();
                Socket socket = new Socket("localhost", 8189);

                // Authentication request
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
                writer.println(pack(typedMail, portClient));
                socket.close();
                Socket incomingAuth = serverSocket.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(incomingAuth.getInputStream()));
                String responseLine = reader.readLine();
                incomingAuth.close();

                if (responseLine == null) {
                    System.err.println("Error: No response from server.");
                    labelError.setVisible(true);
                    labelError.setText("Server error. Please try again later.");
                    return;
                }

                JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();
                if (response.get("authenticated").getAsBoolean()) {
                    JsonElement inboxElement = response.get("inbox");
                    if (inboxElement.isJsonPrimitive() && inboxElement.getAsJsonPrimitive().isString()) {
                        String inboxJsonString = inboxElement.getAsString();
                        JsonArray inboxArray = JsonParser.parseString(inboxJsonString).getAsJsonArray();
                        for (JsonElement emailElement : inboxArray) {
                            JsonObject email = emailElement.getAsJsonObject();
                            List<String> listTo = new LinkedList<>();
                            email.get("to").getAsJsonArray().forEach(to -> listTo.add(to.getAsString()));
                            LocalDateTime date = LocalDateTime.parse(email.get("date").getAsString());

                            Inbox.Mail mail = new Inbox.Mail(
                                    email.get("from").getAsString(),
                                    listTo,
                                    email.get("subject").getAsString(),
                                    email.get("body").getAsString(),
                                    date
                            );
                            this.inbox.getMails().add(mail);
                        }
                    }

                    // initialize the other view
                    viewInboxController.initModel(inbox);
                    stage.setScene(sceneInbox);
                    stage.setMinHeight(730);
                    stage.setMinWidth(950);
                    stage.setTitle("Inbox - " + typedMail);
                } else {
                    labelError.setVisible(true);
                    labelError.setText("Email address not authenticated. Retry.");
                }
            } catch (IOException e) {
                labelError.setVisible(true);
                labelError.setText("Connection error. Please try again later.");
            }
        } else {
            labelError.setVisible(true);
            labelError.setText("Invalid email address.\nCorrect format: abc123@provider.com");
        }
    }
}