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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewAuthController{
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private Inbox inbox;
    private Stage stage;
    private Scene sceneInbox; // Reference to the Inbox scene

    @FXML
    private TextField textboxEmail;

    @FXML
    private Label labelError;

    @FXML
    public void initModel(Inbox inbox, Stage stage, Scene sceneInbox) {
        if(this.inbox != null){
            throw new IllegalStateException("The inbox has already been initialized");
        }
        this.inbox = inbox;
        this.stage = stage;
        this.sceneInbox = sceneInbox;
        labelError.setVisible(false);
        System.out.println("Model Inbox has been initialized [ViewAuthController]");
    }

    private boolean validate(String email){
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }

    private String pack(String typedMail){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("typed_mail_user", typedMail);

        return jsonObject.toString();
    }

    public void handlerOpenInbox(ActionEvent actionEvent) {
        String typedMail = textboxEmail.getText();

        if(validate(typedMail)) {
            inbox.setUserMail(typedMail);
            System.out.println("User mail auth: " + inbox.getUserMail());
            try {
                Socket socket = new Socket("localhost", 8189);
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
                writer.println(pack(typedMail));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String responseLine = reader.readLine();
                socket.close();
                JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();
                if (response.get("authentication").getAsBoolean()) {
                    JsonElement inboxElement = response.get("inbox");
                    // Check if "inbox" is a string containing JSON
                    if (inboxElement.isJsonPrimitive() && inboxElement.getAsJsonPrimitive().isString()) {
                        String inboxJsonString = inboxElement.getAsString();
                        // Parse the string into a JsonArray
                        JsonArray inbox = JsonParser.parseString(inboxJsonString).getAsJsonArray();

                        // Process the JsonArray
                        for (JsonElement emailElement : inbox) {
                            JsonObject email = emailElement.getAsJsonObject();
                            JsonArray toArray = email.get("to").getAsJsonArray(); // Get "to" as JsonArray directly
                            List<String> listTo = new LinkedList<>();
                            for(JsonElement elemTo : toArray){
                                System.out.println(elemTo.getAsString());
                                listTo.add(elemTo.getAsString());
                            }
                            Inbox.Mail mail = new Inbox.Mail(email.get("id").getAsInt(), email.get("from").getAsString(), listTo, email.get("subject").getAsString(), email.get("body").getAsString(), LocalDateTime.now());
                            this.inbox.getMails().add(mail);
                        }
                    } else {
                        System.err.println("Error: 'inbox' is not a valid JSON string.");
                    }
                    stage.setScene(sceneInbox);
                    // changing min height and width when the scene changes
                    stage.setMinHeight(730);
                    stage.setMinWidth(950);
                    stage.setTitle("Inbox - " + typedMail);
                } else {
                    labelError.setVisible(true);
                    labelError.setText("Email address not preliminary authenticated! Retry");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            labelError.setVisible(true);
            labelError.setText("Invalid email address.\nCorrect format: abc123@provider.com");
        }
    }
}
