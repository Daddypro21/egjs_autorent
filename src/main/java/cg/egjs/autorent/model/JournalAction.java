package cg.egjs.autorent.model;

import java.time.LocalDateTime;

public class JournalAction {
    private int           idLog;
    private String        action;
    private String        details;
    private LocalDateTime dateHeure;
    private String        adresseIP;
    private Integer       idUtilisateur;

    public JournalAction() {}

    public JournalAction(String action, String details, Integer idUtilisateur) {
        this.action          = action;
        this.details         = details;
        this.idUtilisateur   = idUtilisateur;
        this.dateHeure       = LocalDateTime.now();
    }

    // Getters & Setters
    public int           getIdLog()               { return idLog; }
    public void          setIdLog(int v)           { this.idLog = v; }
    public String        getAction()               { return action; }
    public void          setAction(String v)       { this.action = v; }
    public String        getDetails()              { return details; }
    public void          setDetails(String v)      { this.details = v; }
    public LocalDateTime getDateHeure()            { return dateHeure; }
    public void          setDateHeure(LocalDateTime v){ this.dateHeure = v; }
    public String        getAdresseIP()            { return adresseIP; }
    public void          setAdresseIP(String v)    { this.adresseIP = v; }
    public Integer       getIdUtilisateur()        { return idUtilisateur; }
    public void          setIdUtilisateur(Integer v){ this.idUtilisateur = v; }
}
