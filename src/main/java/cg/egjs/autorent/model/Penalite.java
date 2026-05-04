package cg.egjs.autorent.model;

import java.time.LocalDateTime;

public class Penalite {
    private int           idPenalite;
    private int           joursRetard;
    private double        tauxJour;
    private double        montant;
    private LocalDateTime dateCalcul;
    private boolean       regle;
    private int           idContrat;

    public Penalite() {}

    public Penalite(int joursRetard, double tauxJour, int idContrat) {
        this.joursRetard = joursRetard;
        this.tauxJour    = tauxJour;
        this.idContrat   = idContrat;
        this.regle       = false;
        this.dateCalcul  = LocalDateTime.now();
        this.calculer();
    }

    /** Calcule le montant : jours × taux journalier */
    public void calculer() {
        this.montant = this.joursRetard * this.tauxJour;
    }

    /** Marque la pénalité comme réglée */
    public void appliquer() {
        this.regle = true;
    }

    // Getters & Setters
    public int           getIdPenalite()          { return idPenalite; }
    public void          setIdPenalite(int v)     { this.idPenalite = v; }
    public int           getJoursRetard()         { return joursRetard; }
    public void          setJoursRetard(int v)    { this.joursRetard = v; }
    public double        getTauxJour()            { return tauxJour; }
    public void          setTauxJour(double v)    { this.tauxJour = v; }
    public double        getMontant()             { return montant; }
    public void          setMontant(double v)     { this.montant = v; }
    public LocalDateTime getDateCalcul()          { return dateCalcul; }
    public void          setDateCalcul(LocalDateTime v){ this.dateCalcul = v; }
    public boolean       isRegle()                { return regle; }
    public void          setRegle(boolean v)      { this.regle = v; }
    public int           getIdContrat()           { return idContrat; }
    public void          setIdContrat(int v)      { this.idContrat = v; }

    @Override
    public String toString() {
        return "Pénalité : " + joursRetard + " jour(s) × " +
               tauxJour + " XAF = " + montant + " XAF";
    }
}
