package cg.egjs.autorent.model;

public class Agence {
    private int idAgence;
    private String nom;
    private String ville;
    private String adresse;
    private String telephone;
    private String email;
    private double tauxPenalite;

    public Agence() {}

    public Agence(int idAgence, String nom, String ville, String adresse,
                  String telephone, String email, double tauxPenalite) {
        this.idAgence     = idAgence;
        this.nom          = nom;
        this.ville        = ville;
        this.adresse      = adresse;
        this.telephone    = telephone;
        this.email        = email;
        this.tauxPenalite = tauxPenalite;
    }

    // Getters & Setters
    public int    getIdAgence()      { return idAgence; }
    public void   setIdAgence(int v) { this.idAgence = v; }
    public String getNom()           { return nom; }
    public void   setNom(String v)   { this.nom = v; }
    public String getVille()         { return ville; }
    public void   setVille(String v) { this.ville = v; }
    public String getAdresse()       { return adresse; }
    public void   setAdresse(String v){ this.adresse = v; }
    public String getTelephone()     { return telephone; }
    public void   setTelephone(String v){ this.telephone = v; }
    public String getEmail()         { return email; }
    public void   setEmail(String v) { this.email = v; }
    public double getTauxPenalite()  { return tauxPenalite; }
    public void   setTauxPenalite(double v){ this.tauxPenalite = v; }

    @Override
    public String toString() {
        return nom + " — " + ville;
    }
}
