package cg.egjs.autorent.model;

import java.time.LocalDate;

public class Maintenance {
    private int       idMaintenance;
    private String    type;
    private LocalDate dateEntree;
    private LocalDate dateSortie;
    private double    cout;
    private String    description;
    private int       idVehicule;

    public Maintenance() {}

    public Maintenance(String type, LocalDate dateEntree,
                       double cout, String description, int idVehicule) {
        this.type        = type;
        this.dateEntree  = dateEntree;
        this.cout        = cout;
        this.description = description;
        this.idVehicule  = idVehicule;
    }

    public boolean estEnCours() {
        return this.dateSortie == null;
    }

    /** Clôture la maintenance avec la date de sortie */
    public void cloturer(LocalDate dateSortie) {
        if (dateSortie.isBefore(this.dateEntree))
            throw new IllegalArgumentException(
                "La date de sortie ne peut pas être avant la date d'entrée.");
        this.dateSortie = dateSortie;
    }

    // Getters & Setters
    public int       getIdMaintenance()         { return idMaintenance; }
    public void      setIdMaintenance(int v)    { this.idMaintenance = v; }
    public String    getType()                  { return type; }
    public void      setType(String v)          { this.type = v; }
    public LocalDate getDateEntree()            { return dateEntree; }
    public void      setDateEntree(LocalDate v) { this.dateEntree = v; }
    public LocalDate getDateSortie()            { return dateSortie; }
    public void      setDateSortie(LocalDate v) { this.dateSortie = v; }
    public double    getCout()                  { return cout; }
    public void      setCout(double v)          { this.cout = v; }
    public String    getDescription()           { return description; }
    public void      setDescription(String v)   { this.description = v; }
    public int       getIdVehicule()            { return idVehicule; }
    public void      setIdVehicule(int v)       { this.idVehicule = v; }

    @Override
    public String toString() {
        return type + " — Entrée: " + dateEntree +
               (dateSortie != null ? " / Sortie: " + dateSortie : " (en cours)");
    }
}
