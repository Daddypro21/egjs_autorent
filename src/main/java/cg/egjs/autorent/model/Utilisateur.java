package cg.egjs.autorent.model;

import java.time.LocalDateTime;

public class Utilisateur {
    private int              idUtilisateur;
    private String           nom;
    private String           prenom;
    private String           email;
    private String           motDePasseHash;
    private RoleUtilisateur  role;
    private String           telephone;
    private String           adresse;
    private String           numPermis;
    private LocalDateTime    dateCreation;
    private boolean          actif;
    private int              tentativesConnexion;
    private int              idAgence;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email,
                       String motDePasseHash, RoleUtilisateur role, int idAgence) {
        this.nom           = nom;
        this.prenom        = prenom;
        this.email         = email;
        this.motDePasseHash = motDePasseHash;
        this.role          = role;
        this.idAgence      = idAgence;
        this.actif         = true;
        this.tentativesConnexion = 0;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean estActif()          { return actif; }
    public boolean estAdmin()          { return role == RoleUtilisateur.ADMINISTRATEUR; }
    public boolean estGestionnaire()   { return role == RoleUtilisateur.GESTIONNAIRE || estAdmin(); }
    public boolean estClient()         { return role == RoleUtilisateur.CLIENT; }

    // Getters & Setters
    public int    getIdUtilisateur()           { return idUtilisateur; }
    public void   setIdUtilisateur(int v)      { this.idUtilisateur = v; }
    public String getNom()                     { return nom; }
    public void   setNom(String v)             { this.nom = v; }
    public String getPrenom()                  { return prenom; }
    public void   setPrenom(String v)          { this.prenom = v; }
    public String getEmail()                   { return email; }
    public void   setEmail(String v)           { this.email = v; }
    public String getMotDePasseHash()          { return motDePasseHash; }
    public void   setMotDePasseHash(String v)  { this.motDePasseHash = v; }
    public RoleUtilisateur getRole()           { return role; }
    public void   setRole(RoleUtilisateur v)   { this.role = v; }
    public String getTelephone()               { return telephone; }
    public void   setTelephone(String v)       { this.telephone = v; }
    public String getAdresse()                 { return adresse; }
    public void   setAdresse(String v)         { this.adresse = v; }
    public String getNumPermis()               { return numPermis; }
    public void   setNumPermis(String v)       { this.numPermis = v; }
    public LocalDateTime getDateCreation()     { return dateCreation; }
    public void   setDateCreation(LocalDateTime v){ this.dateCreation = v; }
    public void   setActif(boolean v)          { this.actif = v; }
    public int    getTentativesConnexion()     { return tentativesConnexion; }
    public void   setTentativesConnexion(int v){ this.tentativesConnexion = v; }
    public int    getIdAgence()                { return idAgence; }
    public void   setIdAgence(int v)           { this.idAgence = v; }

    @Override
    public String toString() { return getNomComplet() + " (" + role + ")"; }
}
