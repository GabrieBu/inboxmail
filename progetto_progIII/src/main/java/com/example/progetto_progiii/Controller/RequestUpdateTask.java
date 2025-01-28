package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class RequestUpdateTask implements Runnable {
    private final BooleanProperty connectionState = new SimpleBooleanProperty();
    private final TextField textField;
    private final Inbox inbox;
    private final Label labelShowError;

    public RequestUpdateTask(TextField textFieldProva, Circle statusCircle, Label labelShowError, Inbox inbox) {
        textField = textFieldProva;
        this.inbox = inbox;
        this.labelShowError = labelShowError;

        connectionState.addListener((observable, oldValue, newValue) -> {
            statusCircle.setFill(newValue ? Color.GREEN : Color.RED); // listen for changes, if newValue is true then
                                                                      // fill with green else red
        });
        // initial fill color
        statusCircle.setFill(Color.RED);
    }

    public void run() {
        while (true) {
            try (Socket socket = new Socket("localhost", 8189);
                    ServerSocket clientSock = new ServerSocket(socket.getLocalPort())) {
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "request");
                jsonObject.addProperty("user", this.inbox.getUserMail());
                jsonObject.addProperty("port", clientSock.getLocalPort());
                jsonObject.addProperty("last_id_received", this.inbox.getIdLastMail());
                writer.println(jsonObject);

                try (Socket sockIn = clientSock.accept()) { // wait for inbox changes
                    BufferedReader reader = new BufferedReader(new InputStreamReader(sockIn.getInputStream()));
                    String response = reader.readLine();
                    if (response != null) { // if servers send something
                        updateServerConnection(true);
                        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                        handleServerResponse(jsonResponse);
                    } else {
                        updateServerConnection(false);
                    }
                } catch (IOException f) {
                    labelShowError.setText("Can't accept connection from server, try to restart application!");
                    updateServerConnection(false); // server is down
                }
            } catch (Exception e) {
                updateServerConnection(false); // server is down
            }

            try {
                Thread.sleep(5000); // ogni 5 sec
            } catch (InterruptedException e) {
                labelShowError.setText("Can't update mails from server. Try to restart application!");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleServerResponse(JsonObject jsonResponse) {
        JsonArray jsonArray = jsonResponse.get("inbox").getAsJsonArray();
        long last_id_received = this.inbox.getIdLastMail();
        if (!jsonArray.isEmpty()) { // not necessary to modify last_id_received (it will be always minimum if there
                                    // aren't any mails)
            long new_max = Long.MIN_VALUE;
            for (JsonElement jsonMail : jsonArray) {
                JsonObject jsonObjectMail = jsonMail.getAsJsonObject(); // get mails
                processIncomingMessage(jsonObjectMail);
                if (jsonObjectMail.get("id").getAsLong() > new_max)
                    new_max = jsonObjectMail.get("id").getAsLong();

                if (jsonObjectMail.get("id").getAsLong() > last_id_received) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("New mail received");
                    alert.setHeaderText("You have received" + (jsonObjectMail.get("id").getAsLong() - last_id_received)
                            + "new mail!");
                    alert.setContentText(
                            "You have received a new mails from " + jsonObjectMail.get("from").getAsString());
                    alert.showAndWait();
                }

            }
            this.inbox.setIdLastMail(new_max);
        }
    }

    private void processIncomingMessage(JsonObject jsonMessage) {
        Platform.runLater(() -> {
            Inbox.Mail newMail = parseMessageToMail(jsonMessage);
            if (newMail != null) {
                this.inbox.getMails().add(0, newMail);
            }
        });
    }

    private Inbox.Mail parseMessageToMail(JsonObject mail) {
        try {
            JsonArray jsonArray = mail.get("to").getAsJsonArray();
            String[] arrayTo = new String[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                arrayTo[i] = jsonArray.get(i).getAsString();
            }
            LocalDateTime date = LocalDateTime.parse(mail.get("date").getAsString());
            return new Inbox.Mail(
                    mail.get("id").getAsLong(),
                    mail.get("from").getAsString(),
                    arrayTo,
                    mail.get("subject").getAsString(),
                    mail.get("body").getAsString(),
                    date);
        } catch (Exception e) {
            System.out.println("Mail not valid");
            return null;
        }
    }

    private void updateServerConnection(Boolean connection) {
        if (connection != connectionState.get()) { // if connection changes
            connectionState.set(connection); // update property
            textField.setEditable(true);
            textField.setText(connectionState.get() ? "Online" : "Offline");
            textField.setEditable(false);
        }
    }
}
