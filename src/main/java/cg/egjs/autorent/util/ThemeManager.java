package cg.egjs.autorent.util;

import javafx.scene.Scene;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Gestionnaire de thème jour/nuit.
 * Commute entre les feuilles CSS dark et light sur toute l'application.
 */
public class ThemeManager {

    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());
    private static ThemeManager instance;
    private Theme currentTheme = Theme.DARK;

    public enum Theme { DARK, LIGHT }

    private static final String CSS_DARK  = "/cg/egjs/autorent/css/autorent-dark.css";
    private static final String CSS_LIGHT = "/cg/egjs/autorent/css/autorent-light.css";

    private ThemeManager() {}

    public static synchronized ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    /**
     * Applique un thème à une scène JavaFX.
     * Efface les anciens stylesheets et charge le nouveau.
     */
    public void applyTheme(Scene scene, Theme theme) {
        scene.getStylesheets().clear();
        String cssPath = (theme == Theme.DARK) ? CSS_DARK : CSS_LIGHT;
        try {
            String url = Objects.requireNonNull(
                getClass().getResource(cssPath)).toExternalForm();
            scene.getStylesheets().add(url);
            currentTheme = theme;
            LOGGER.info("Thème appliqué : " + theme);
        } catch (NullPointerException e) {
            LOGGER.warning("CSS introuvable : " + cssPath);
        }
    }

    /** Bascule entre les thèmes sombre et clair */
    public void toggleTheme(Scene scene) {
        applyTheme(scene, currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK);
    }

    public Theme getCurrentTheme() { return currentTheme; }
    public boolean isDark() { return currentTheme == Theme.DARK; }
}
