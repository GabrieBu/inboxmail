package com.example.progetto_progiii;

import com.example.progetto_progiii.Controller.ViewAuthController;
import com.example.progetto_progiii.Controller.ViewInboxController;
import com.example.progetto_progiii.Model.Inbox;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

        contrAuth.initModel(inbox, stage, sceneInbox);
        contrInbox.initModel(inbox);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
