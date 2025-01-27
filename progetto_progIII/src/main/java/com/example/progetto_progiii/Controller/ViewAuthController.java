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
        labelError.setVisible(false); //to be sure
        System.out.println("Inbox has been initialized [ViewAuthController]");
    }

    private boolean validate(String email){
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }

    private String pack(String typedMail, int port){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "authentication"); //dispatcher will dispatch request to the correct task
        jsonObject.addProperty("typed_mail_user", typedMail);
        jsonObject.addProperty("port", port); //port on which the client will wait (auth true or false)
        return jsonObject.toString();
    }

    public void handlerOpenInbox(ActionEvent actionEvent) {
        String typedMail = textboxEmail.getText(); //email address typed by user
        if (validate(typedMail)) {
            inbox.setUserMail(typedMail);
            try ( ServerSocket serverSocket = new ServerSocket(0);
                  Socket socket = new Socket("localhost", 8189);) {
                int portClient = serverSocket.getLocalPort();

                // authentication request
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true); // true for auto-flushing
                writer.println(pack(typedMail, portClient));
                socket.close();
                Socket incomingAuth = serverSocket.accept(); //wait for the feedback

                BufferedReader reader = new BufferedReader(new InputStreamReader(incomingAuth.getInputStream()));
                String responseLine = reader.readLine();
                incomingAuth.close();

                if (responseLine == null) { //if server doesn't send anything
                    labelError.setVisible(true);
                    labelError.setText("Server is not responding. Please try again later.");
                    return;
                }

                JsonObject response = JsonParser.parseString(responseLine).getAsJsonObject();
                if (response.get("authenticated").getAsBoolean()) { //true or false
                    viewInboxController.initModel(inbox);
                    stage.setScene(sceneInbox);
                    stage.setMinHeight(730);
                    stage.setMinWidth(950);
                    stage.setTitle("Inbox - " + typedMail);
                    stage.setOnCloseRequest(event -> viewInboxController.shutdown(event)); //handler shutdown
                } else {
                    labelError.setVisible(true);
                    labelError.setText("Email address not authenticated in the system. Retry.");
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