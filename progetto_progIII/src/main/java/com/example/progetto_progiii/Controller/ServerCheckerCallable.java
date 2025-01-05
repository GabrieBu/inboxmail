package com.example.progetto_progiii.Controller;

import com.google.gson.JsonObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

public class ServerCheckerCallable implements Callable<Void> {
    private volatile boolean running = true;
    private final int port;
    private final BooleanProperty connectionState = new SimpleBooleanProperty();
    private Boolean stateConnection = false;
    private TextField textField;
    private Circle statusCirlce;

    public ServerCheckerCallable(int port, TextField textFieldProva, Circle statusCircle) {
        this.port = port;
        textField = textFieldProva;
        this.statusCirlce = statusCircle;

        connectionState.addListener((observable, oldValue, newValue) -> {
            statusCircle.setFill(newValue ? Color.GREEN : Color.RED);
        });

        // initial fill color
        statusCircle.setFill(Color.RED);
    }

    public Void call() {
        while (running) {
            try (Socket socket = new Socket("localhost", port)){
                //ping the server
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "ping");
                writer.println(jsonObject);
                socket.close();
                updateServerConnection(true);
            } catch (Exception e) {
                updateServerConnection(false);
            }
            // Timer di 10 secondi
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrotto: " + e.getMessage());
                // chiusura client pefforza
                break;
            }
        }
        return null;
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

