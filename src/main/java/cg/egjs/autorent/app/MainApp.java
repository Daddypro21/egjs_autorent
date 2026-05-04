package cg.egjs.autorent.app;

import cg.egjs.autorent.config.DatabaseConnection;
import cg.egjs.autorent.util.ThemeManager;
import cg.egjs.autorent.view.auth.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.logging.Logger;

/**
 * Point d'entrée de l'application JavaFX EGJS AutoRent.
 * Lance la fenêtre de connexion avec la charte graphique rouge/noir.
 */
public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Fenêtre sans bordures système (titlebar custom)
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("EGJS AutoRent — Système de Gestion");
        stage.setMinWidth(960);
        stage.setMinHeight(620);

        // Taille par défaut (environ 13")
        stage.setWidth(1280);
        stage.setHeight(800);

        // Centrer à l'écran
        stage.centerOnScreen();

        // Charger l'écran de connexion
        LoginView loginView = new LoginView(stage);
        Scene scene = loginView.buildScene();

        // Appliquer le thème sombre par défaut
        ThemeManager.getInstance().applyTheme(scene, ThemeManager.Theme.DARK);

        stage.setScene(scene);
        stage.show();

        LOGGER.info("EGJS AutoRent démarré.");
    }

    @Override
    public void stop() {
        DatabaseConnection.getInstance().closeConnection();
        LOGGER.info("Application fermée proprement.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
