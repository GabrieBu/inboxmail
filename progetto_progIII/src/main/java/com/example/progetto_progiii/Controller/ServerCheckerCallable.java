package com.example.progetto_progiii.Controller;

import com.example.progetto_progiii.Model.Inbox;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerCheckerCallable implements Callable<Void> {
    private volatile boolean running = true;
    private final int port;
    private final Inbox inbox;
    private Boolean SavedConnection=false;

    public ServerCheckerCallable(int port, Inbox inbox) {
        this.port = port;
        this.inbox= inbox;
    }

    public Void call() {
        while (running) {
            try (Socket socket = new Socket("localhost ", port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                System.out.println("Connesso al server con successo");
                updateServerConnection(true);
            } catch (Exception e) {
                System.out.println("Errore durante la connessione al server: " + e.getMessage());
                updateServerConnection(false);
            }
            // Timer di 2 secondi b
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrotto: " + e.getMessage());
                break;
            }
        }
        return null;
    }


    private void updateServerConnection (Boolean Connection){
        if (Connection!=SavedConnection) {
            System.out.println("Stato connessione aggiornato: " + (Connection ? "Online" : "Offline"));
            // Implementazione per aggiornare l'inbox
        }
    }
}

