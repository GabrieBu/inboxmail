package com.example.progetto_progiii;

import com.example.progetto_progiii.Controller.ViewAuthController;
import com.example.progetto_progiii.Controller.ViewInboxController;
import com.example.progetto_progiii.Model.Inbox;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoaderAuth = new FXMLLoader(com.example.progetto_progiii.Main.class.getResource("view_auth.fxml"));
        Scene sceneAuth = new Scene(fxmlLoaderAuth.load(), 320, 240);

        FXMLLoader fxmlLoaderInbox = new FXMLLoader(com.example.progetto_progiii.Main.class.getResource("view_inbox.fxml"));
        Scene sceneInbox = new Scene(fxmlLoaderInbox.load(), 800, 600);

        stage.setTitle("Auth");
        stage.setScene(sceneAuth);
        Inbox inbox = new Inbox();
        ViewAuthController contrAuth = fxmlLoaderAuth.getController();
        ViewInboxController contrInbox = fxmlLoaderInbox.getController();

        contrAuth.initModel(inbox, stage, sceneInbox, contrInbox);
        //min width and height will be modified when scene changes
        stage.setMinWidth(400);
        stage.setMinHeight(300);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
