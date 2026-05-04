package cg.egjs.autorent.model;

import java.time.LocalDateTime;

public class Vehicule {
    private int            idVehicule;
    private String         marque;
    private String         modele;
    private int            annee;
    private String         immatriculation;
    private double         prixJour;
    private StatutVehicule statut;
    private int            kilometrage;
    private String         photoPath;
    private LocalDateTime  dateAjout;
    private int            idAgence;

    public Vehicule() {}

    public Vehicule(String marque, String modele, int annee,
                    String immatriculation, double prixJour, int idAgence) {
        this.marque          = marque;
        this.modele          = modele;
        this.annee           = annee;
        this.immatriculation = immatriculation;
        this.prixJour        = prixJour;
        this.statut          = StatutVehicule.DISPONIBLE;
        this.idAgence        = idAgence;
    }

    /** Vérifie si le véhicule est disponible à la location */
    public boolean estDisponible() {
        return this.statut == StatutVehicule.DISPONIBLE;
    }

    /** Change le statut du véhicule */
    public void changerStatut(StatutVehicule nouveauStatut) {
        this.statut = nouveauStatut;
    }

    public String getDesignation() {
        return marque + " " + modele + " (" + annee + ")";
    }

    // Getters & Setters
    public int            getIdVehicule()           { return idVehicule; }
    public void           setIdVehicule(int v)      { this.idVehicule = v; }
    public String         getMarque()               { return marque; }
    public void           setMarque(String v)       { this.marque = v; }
    public String         getModele()               { return modele; }
    public void           setModele(String v)       { this.modele = v; }
    public int            getAnnee()                { return annee; }
    public void           setAnnee(int v)           { this.annee = v; }
    public String         getImmatriculation()      { return immatriculation; }
    public void           setImmatriculation(String v){ this.immatriculation = v; }
    public double         getPrixJour()             { return prixJour; }
    public void           setPrixJour(double v)     { this.prixJour = v; }
    public StatutVehicule getStatut()               { return statut; }
    public void           setStatut(StatutVehicule v){ this.statut = v; }
    public int            getKilometrage()          { return kilometrage; }
    public void           setKilometrage(int v)     { this.kilometrage = v; }
    public String         getPhotoPath()            { return photoPath; }
    public void           setPhotoPath(String v)    { this.photoPath = v; }
    public LocalDateTime  getDateAjout()            { return dateAjout; }
    public void           setDateAjout(LocalDateTime v){ this.dateAjout = v; }
    public int            getIdAgence()             { return idAgence; }
    public void           setIdAgence(int v)        { this.idAgence = v; }

    @Override
    public String toString() { return getDesignation() + " — " + immatriculation; }
}
