package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;

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
                // Read the response from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();
                System.out.println("Risposta dal server: " + response);
                socket.close(); // close connection
                if(response.equals("authenticated")){
                    //access to inbox
                    stage.setScene(sceneInbox);
                    // changing min height and width when the scene changes
                    stage.setMinHeight(730);
                    stage.setMinWidth(950);
                    stage.setTitle("Inbox - " + typedMail);
                }
                else{
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
