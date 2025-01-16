package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class ServerCheckerCallable implements Callable<Void> {
    private volatile boolean running = true;
    private final int port = 8189;
    private final BooleanProperty connectionState = new SimpleBooleanProperty();
    private TextField textField;
    private Circle statusCirlce;
    private final Inbox inbox;

    public ServerCheckerCallable(TextField textFieldProva, Circle statusCircle, Inbox inbox) {
        textField = textFieldProva;
        this.statusCirlce = statusCircle;
        this.inbox = inbox;

        connectionState.addListener((observable, oldValue, newValue) -> {
            statusCircle.setFill(newValue ? Color.GREEN : Color.RED);
        });

        // initial fill color
        statusCircle.setFill(Color.RED);
    }

    public Void call() {
        while (running) {
            try (Socket socket = new Socket("localhost", port);
                 OutputStream outputStream = socket.getOutputStream();
                 ServerSocket clientSock = new ServerSocket(socket.getLocalPort())) {

                PrintWriter writer = new PrintWriter(outputStream, true);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "request");
                jsonObject.addProperty("user", this.inbox.getUserMail());
                jsonObject.addProperty("port", clientSock.getLocalPort());
                writer.println(jsonObject);

                try (Socket sockIn = clientSock.accept()){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(sockIn.getInputStream()));

                    String response = reader.readLine();
                    System.out.println("Server Response: " + response);
                    if (response != null) {
                        updateServerConnection(true);
                        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                        handleServerResponse(jsonResponse);
                    }
                    else{
                        updateServerConnection(false);
                    }
                }
                catch(IOException f){
                    System.out.println("f" + f);
                }
            } catch (Exception e) {
                System.out.println(e);
                updateServerConnection(false);
            }
            //forse si può fare unico switch
            try {
                Thread.sleep(20000); //ogni 20 sec
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrotto: " + e.getMessage());
                // chiusura client pefforza
                break;
            }
        }
        return null;
    }

    private void handleServerResponse(JsonObject jsonResponse) {
        JsonArray jsonArray = jsonResponse.get("inbox").getAsJsonArray();
        for(JsonElement jsonElement : jsonArray){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            processIncomingMessage(jsonObject);
        }
    }


    private void processIncomingMessage(JsonObject jsonMessage) {
        Platform.runLater(() -> {
            Inbox.Mail newMail = parseMessageToMail(jsonMessage);
            if (newMail != null) {
                inbox.getMails().add(0, newMail);
            }
        });
    }

    private Inbox.Mail parseMessageToMail(JsonObject mail) {
        try{
            JsonArray jsonArray = mail.get("to").getAsJsonArray();
            String[] arrayTo = new String[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                arrayTo[i] = jsonArray.get(i).getAsString();
            }
            LocalDateTime date = LocalDateTime.parse(mail.get("date").getAsString());

            return new Inbox.Mail(
                    mail.get("from").getAsString(),
                    arrayTo,
                    mail.get("subject").getAsString(),
                    mail.get("body").getAsString(),
                    date
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateServerConnection (Boolean connection){
        if (connection != connectionState.get()) {
            connectionState.set(connection);
            textField.setEditable(true);
            textField.setText(connectionState.get() ? "Online" : "Offline");
            textField.setEditable(false);
        }
    }
}

