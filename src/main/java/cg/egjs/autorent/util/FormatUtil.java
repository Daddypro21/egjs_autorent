package cg.egjs.autorent.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilitaires de formatage pour l'affichage (devise XAF, dates).
 */
public class FormatUtil {

    private static final Locale LOCALE_CG = new Locale("fr", "CG");

    private FormatUtil() {}

    /** Formate un montant en XAF : ex. 75 000 XAF */
    public static String formatXAF(double montant) {
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_CG);
        nf.setMaximumFractionDigits(0);
        return nf.format(montant) + " XAF";
    }

    /** Formate un kilométrage : ex. 12 500 km */
    public static String formatKm(int km) {
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_CG);
        return nf.format(km) + " km";
    }
}
