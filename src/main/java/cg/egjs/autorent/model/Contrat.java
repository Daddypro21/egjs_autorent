package cg.egjs.autorent.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Contrat {
    private int           idContrat;
    private LocalDate     dateDebut;
    private LocalDate     dateFin;
    private LocalDate     dateRetourReelle;
    private double        montantTotal;
    private StatutContrat statut;
    private String        cheminPDF;
    private LocalDateTime dateCreation;
    private int           idClient;
    private int           idVehicule;
    private int           idAgence;

    // Objets liés (non persistés, chargés à la demande)
    private Utilisateur   client;
    private Vehicule      vehicule;

    public Contrat() {}

    public Contrat(LocalDate dateDebut, LocalDate dateFin,
                   int idClient, int idVehicule, int idAgence) {
        this.dateDebut   = dateDebut;
        this.dateFin     = dateFin;
        this.idClient    = idClient;
        this.idVehicule  = idVehicule;
        this.idAgence    = idAgence;
        this.statut      = StatutContrat.EN_COURS;
    }

    /**
     * Calcule le montant total du contrat.
     * Délégué au MODÈLE (non au controller) — correction audit v2.
     * @param prixJourVehicule prix journalier du véhicule en XAF
     * @return montant total en XAF
     */
    public double calculerMontant(double prixJourVehicule) {
        long nbJours = ChronoUnit.DAYS.between(dateDebut, dateFin);
        if (nbJours <= 0) throw new IllegalArgumentException(
            "La date de fin doit être postérieure à la date de début.");
        this.montantTotal = nbJours * prixJourVehicule;
        return this.montantTotal;
    }

    /**
     * Vérifie si le contrat est en retard.
     * @param dateRetour date de retour réelle
     * @return true si retard détecté
     */
    public boolean verifierRetard(LocalDate dateRetour) {
        return dateRetour.isAfter(this.dateFin);
    }

    /**
     * Calcule le nombre de jours de retard.
     * @param dateRetour date de retour réelle
     * @return nombre de jours de retard (0 si pas de retard)
     */
    public long calculerJoursRetard(LocalDate dateRetour) {
        if (!verifierRetard(dateRetour)) return 0;
        return ChronoUnit.DAYS.between(this.dateFin, dateRetour);
    }

    public long getNbJoursLocation() {
        return ChronoUnit.DAYS.between(dateDebut, dateFin);
    }

    // Getters & Setters
    public int           getIdContrat()             { return idContrat; }
    public void          setIdContrat(int v)        { this.idContrat = v; }
    public LocalDate     getDateDebut()             { return dateDebut; }
    public void          setDateDebut(LocalDate v)  { this.dateDebut = v; }
    public LocalDate     getDateFin()               { return dateFin; }
    public void          setDateFin(LocalDate v)    { this.dateFin = v; }
    public LocalDate     getDateRetourReelle()      { return dateRetourReelle; }
    public void          setDateRetourReelle(LocalDate v){ this.dateRetourReelle = v; }
    public double        getMontantTotal()          { return montantTotal; }
    public void          setMontantTotal(double v)  { this.montantTotal = v; }
    public StatutContrat getStatut()                { return statut; }
    public void          setStatut(StatutContrat v) { this.statut = v; }
    public String        getCheminPDF()             { return cheminPDF; }
    public void          setCheminPDF(String v)     { this.cheminPDF = v; }
    public LocalDateTime getDateCreation()          { return dateCreation; }
    public void          setDateCreation(LocalDateTime v){ this.dateCreation = v; }
    public int           getIdClient()              { return idClient; }
    public void          setIdClient(int v)         { this.idClient = v; }
    public int           getIdVehicule()            { return idVehicule; }
    public void          setIdVehicule(int v)       { this.idVehicule = v; }
    public int           getIdAgence()              { return idAgence; }
    public void          setIdAgence(int v)         { this.idAgence = v; }
    public Utilisateur   getClient()                { return client; }
    public void          setClient(Utilisateur v)   { this.client = v; }
    public Vehicule      getVehicule()              { return vehicule; }
    public void          setVehicule(Vehicule v)    { this.vehicule = v; }

    @Override
    public String toString() {
        return "Contrat #" + idContrat + " — " + dateDebut + " au " + dateFin;
    }
}
