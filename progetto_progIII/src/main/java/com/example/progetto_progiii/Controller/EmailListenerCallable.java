package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class EmailListenerCallable implements Callable<Void> {
    private volatile boolean running = true;
    private final Inbox inbox;

    public EmailListenerCallable(Inbox inbox) {
        this.inbox = inbox;
    }

    public void stop() {
        running = false; // Signal the listener to stop
    }

    public Void call() {
        int port = inbox.getPortClient();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Wait for server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String response = reader.readLine();
                    System.out.println(response);
                    processIncomingMessage(response);
                    clientSocket.close();
                } catch (Exception e) {
                    if (running) { // Ignore exceptions during shutdown
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void processIncomingMessage(String message) {
        JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
        String typeReq = jsonMessage.get("type").getAsString();
        if(typeReq.equals("send") || typeReq.equals("reply") || typeReq.equals("reply_all")) {
            Platform.runLater(() -> {
                Inbox.Mail newMail = parseMessageToMail(jsonMessage);
                if (newMail != null) {
                    inbox.getMails().add(0, newMail);
                }
            });
        }
        else if(typeReq.equals("send_error")) {
            //fare qualcosa per displayare
            System.out.println(jsonMessage);
        }
    }

    private Inbox.Mail parseMessageToMail(JsonObject mail) {
        JsonElement emailElement = mail.get("mail");
        try {
            JsonObject email = emailElement.getAsJsonObject();
            List<String> listTo = new LinkedList<>();
            email.get("to").getAsJsonArray().forEach(to -> listTo.add(to.getAsString()));
            LocalDateTime date = LocalDateTime.parse(email.get("date").getAsString());

            return new Inbox.Mail(
                    email.get("from").getAsString(),
                    listTo,
                    email.get("subject").getAsString(),
                    email.get("body").getAsString(),
                    date
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
