package com.example.progetto_progiii.Controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunnableReceive implements Runnable {
    private String unpack(String jsonReq){
        JsonObject jsonObject = JsonParser.parseString(jsonReq).getAsJsonObject();
        return jsonObject.get("type").getAsString();
    }
    @Override
    public void run() {


        try (ServerSocket provasoc = new ServerSocket( 8190)){

                Socket clientSocket = provasoc.accept();
                System.out.println("Connessione accettata da " + clientSocket.getInetAddress());

                // Esempio di lettura dati dal client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Ricevuto: " + inputLine);
                }

            /*BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String serverRecString = reader.readLine();
            String boh = unpack(serverRecString);
            System.out.println(boh);
            sock.close();*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {

        }


    }
}
