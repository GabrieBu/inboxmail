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

    private String pack(String typedMail, int port){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "authentication");
        jsonObject.addProperty("typed_mail_user", typedMail);
        jsonObject.addProperty("port", String.valueOf(port));
        System.out.println(jsonObject);
        return jsonObject.toString();
    }

    public void handlerOpenInbox(ActionEvent actionEvent) {
        String typedMail = textboxEmail.getText();

        if (validate(typedMail)) {
            inbox.setUserMail(typedMail);
            System.out.println("User mail auth: " + inbox.getUserMail());
            try (Socket socket = new Socket("localhost", 8189)) {
                int portClient = socket.getLocalPort();
                System.out.println("Client is sending through: " + portClient);

                // Send authentication request
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
                writer.println(pack(typedMail, portClient));

                // Read server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String responseLine = reader.readLine();
                if (responseLine == null) {
                    System.err.println("Error: No response from server.");
                    labelError.setVisible(true);
                    labelError.setText("Server error. Please try again later.");
                    return;
                }

                JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();
                if (response.get("authenticated").getAsBoolean()) {
                    JsonElement inboxElement = response.get("inbox");

                    // Process inbox if "inbox" is valid JSON
                    if (inboxElement.isJsonPrimitive() && inboxElement.getAsJsonPrimitive().isString()) {
                        String inboxJsonString = inboxElement.getAsString();
                        JsonArray inboxArray = JsonParser.parseString(inboxJsonString).getAsJsonArray();
                        for (JsonElement emailElement : inboxArray) {
                            JsonObject email = emailElement.getAsJsonObject();
                            List<String> listTo = new LinkedList<>();
                            email.get("to").getAsJsonArray().forEach(to -> listTo.add(to.getAsString()));

                            Inbox.Mail mail = new Inbox.Mail(
                                    email.get("from").getAsString(),
                                    listTo,
                                    email.get("subject").getAsString(),
                                    email.get("body").getAsString(),
                                    LocalDateTime.now()
                            );
                            this.inbox.getMails().add(mail);
                        }
                    }

                    System.out.println("Authentication successful. Listening on port: " + portClient);
                    //startEmailListener(portClient);

                    // Change scene to Inbox
                    stage.setScene(sceneInbox);
                    stage.setMinHeight(730);
                    stage.setMinWidth(950);
                    stage.setTitle("Inbox - " + typedMail);
                } else {
                    labelError.setVisible(true);
                    labelError.setText("Email address not authenticated. Retry.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                labelError.setVisible(true);
                labelError.setText("Connection error. Please try again later.");
            }
        } else {
            labelError.setVisible(true);
            labelError.setText("Invalid email address.\nCorrect format: abc123@provider.com");
        }
    }


    // Method to listen for incoming emails on the dynamically assigned port
    private void startEmailListener(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Wake up!");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String incomingEmail = reader.readLine();

                    System.out.println("Incoming email: " + incomingEmail);
                    //JsonObject emailJson = JsonParser.parseString(incomingEmail).getAsJsonObject();
                    // Add logic to process the email and update inbox
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
