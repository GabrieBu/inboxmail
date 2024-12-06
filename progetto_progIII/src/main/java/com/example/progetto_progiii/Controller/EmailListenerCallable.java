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
            System.out.println("Client istening on port " + port);
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Wait for a client
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String response = reader.readLine();
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
        Platform.runLater(() -> {
            Inbox.Mail newMail = parseMessageToMail(message);
            if (newMail != null) {
                inbox.getMails().add(newMail);
            }
        });
    }

    private Inbox.Mail parseMessageToMail(String message) {
        JsonObject mail = JsonParser.parseString(message).getAsJsonObject();
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
