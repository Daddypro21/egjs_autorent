-- ============================================================
--  EGJS AutoRent — Script SQL v2 (final corrigé)
--  Base de données : MySQL 8.0+
--  Agence de location de voitures — Brazzaville, Congo
--  Encodage : UTF-8  |  Devise : XAF (Franc CFA)
--  Corrections v2 :
--    + champ tentativesConnexion dans utilisateur
--    + adresse dans agence
--    + annee + photoPath dans vehicule
--    + ENUMs explicitement typés
--    + cheminPDF dans contrat
--    + dateSortie nullable dans maintenance
--    + index de performance
-- ============================================================

CREATE DATABASE IF NOT EXISTS egjs_autorent
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE egjs_autorent;

-- ============================================================
-- 1. TABLE AGENCE
-- ============================================================
CREATE TABLE agence (
    idAgence        INT            AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(100)   NOT NULL,
    ville           VARCHAR(100)   NOT NULL,
    adresse         VARCHAR(255)   NOT NULL,
    telephone       VARCHAR(20)    NOT NULL,
    email           VARCHAR(100)   NOT NULL,
    tauxPenalite    DECIMAL(10,2)  NOT NULL DEFAULT 5000.00
                    COMMENT 'Pénalité par jour de retard en XAF'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Informations de l agence de location';

-- ============================================================
-- 2. TABLE UTILISATEUR
-- ============================================================
CREATE TABLE utilisateur (
    idUtilisateur        INT           AUTO_INCREMENT PRIMARY KEY,
    nom                  VARCHAR(100)  NOT NULL,
    prenom               VARCHAR(100)  NOT NULL,
    email                VARCHAR(150)  NOT NULL UNIQUE,
    motDePasseHash       VARCHAR(255)  NOT NULL
                         COMMENT 'Hashé avec BCrypt (force 12)',
    role                 ENUM('CLIENT','GESTIONNAIRE','ADMINISTRATEUR')
                         NOT NULL DEFAULT 'CLIENT',
    telephone            VARCHAR(20)   NULL,
    adresse              VARCHAR(255)  NULL,
    numPermis            VARCHAR(50)   NULL
                         COMMENT 'Obligatoire pour role = CLIENT',
    dateCreation         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif                TINYINT(1)    NOT NULL DEFAULT 1
                         COMMENT '0 = compte désactivé',
    tentativesConnexion  INT           NOT NULL DEFAULT 0
                         COMMENT 'Réinitialisé à 0 après connexion réussie. Bloqué à 3.',
    idAgence             INT           NOT NULL,
    CONSTRAINT fk_util_agence  FOREIGN KEY (idAgence)
        REFERENCES agence(idAgence) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_permis_client CHECK (
        role != 'CLIENT' OR numPermis IS NOT NULL
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Tous les utilisateurs : clients, gestionnaires, admins';

-- ============================================================
-- 3. TABLE VEHICULE
-- ============================================================
CREATE TABLE vehicule (
    idVehicule      INT            AUTO_INCREMENT PRIMARY KEY,
    marque          VARCHAR(100)   NOT NULL,
    modele          VARCHAR(100)   NOT NULL,
    annee           INT            NOT NULL
                    COMMENT 'Année de fabrication',
    immatriculation VARCHAR(20)    NOT NULL UNIQUE,
    prixJour        DECIMAL(12,2)  NOT NULL
                    COMMENT 'Prix de location par jour en XAF',
    statut          ENUM('DISPONIBLE','LOUE','EN_MAINTENANCE','HORS_SERVICE')
                    NOT NULL DEFAULT 'DISPONIBLE',
    kilometrage     INT            NOT NULL DEFAULT 0,
    photoPath       VARCHAR(255)   NULL
                    COMMENT 'Chemin relatif vers la photo du véhicule',
    dateAjout       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    idAgence        INT            NOT NULL,
    CONSTRAINT fk_veh_agence  FOREIGN KEY (idAgence)
        REFERENCES agence(idAgence) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_annee_veh  CHECK (annee >= 1990 AND annee <= 2030),
    CONSTRAINT chk_prix_pos   CHECK (prixJour > 0),
    CONSTRAINT chk_km_pos     CHECK (kilometrage >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Parc de véhicules de l agence';

-- ============================================================
-- 4. TABLE CONTRAT
-- ============================================================
CREATE TABLE contrat (
    idContrat           INT            AUTO_INCREMENT PRIMARY KEY,
    dateDebut           DATE           NOT NULL,
    dateFin             DATE           NOT NULL,
    dateRetourReelle    DATE           NULL
                        COMMENT 'NULL tant que le véhicule n est pas restitué',
    montantTotal        DECIMAL(12,2)  NOT NULL DEFAULT 0.00
                        COMMENT 'Calculé par Contrat.calculerMontant() côté Java',
    statut              ENUM('EN_COURS','TERMINE','ANNULE','EN_RETARD')
                        NOT NULL DEFAULT 'EN_COURS',
    cheminPDF           VARCHAR(255)   NULL
                        COMMENT 'Chemin vers le contrat PDF généré',
    dateCreation        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    idClient            INT            NOT NULL,
    idVehicule          INT            NOT NULL,
    idAgence            INT            NOT NULL,
    CONSTRAINT fk_contrat_client   FOREIGN KEY (idClient)
        REFERENCES utilisateur(idUtilisateur) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_contrat_veh      FOREIGN KEY (idVehicule)
        REFERENCES vehicule(idVehicule) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_contrat_agence   FOREIGN KEY (idAgence)
        REFERENCES agence(idAgence) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_dates_contrat   CHECK (dateFin > dateDebut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Contrats de location signés';

-- ============================================================
-- 5. TABLE PENALITE
-- ============================================================
CREATE TABLE penalite (
    idPenalite   INT            AUTO_INCREMENT PRIMARY KEY,
    joursRetard  INT            NOT NULL,
    tauxJour     DECIMAL(12,2)  NOT NULL
                 COMMENT 'Taux XAF appliqué au moment du calcul',
    montant      DECIMAL(12,2)  NOT NULL
                 COMMENT 'joursRetard × tauxJour',
    dateCalcul   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    regle        TINYINT(1)     NOT NULL DEFAULT 0
                 COMMENT '0 = non réglée, 1 = réglée',
    idContrat    INT            NOT NULL UNIQUE
                 COMMENT 'Un contrat = au plus une pénalité',
    CONSTRAINT fk_pen_contrat   FOREIGN KEY (idContrat)
        REFERENCES contrat(idContrat) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_jours_pos    CHECK (joursRetard > 0),
    CONSTRAINT chk_montant_pos  CHECK (montant > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Pénalités de retard calculées automatiquement';

-- ============================================================
-- 6. TABLE MAINTENANCE
-- ============================================================
CREATE TABLE maintenance (
    idMaintenance   INT            AUTO_INCREMENT PRIMARY KEY,
    type            VARCHAR(100)   NOT NULL
                    COMMENT 'Vidange, Pneus, Freins, Révision générale...',
    dateEntree      DATE           NOT NULL,
    dateSortie      DATE           NULL
                    COMMENT 'NULL = véhicule encore en maintenance',
    cout            DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    description     TEXT           NULL,
    idVehicule      INT            NOT NULL,
    CONSTRAINT fk_maint_veh       FOREIGN KEY (idVehicule)
        REFERENCES vehicule(idVehicule) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_dates_maint    CHECK (
        dateSortie IS NULL OR dateSortie >= dateEntree
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Historique des maintenances par véhicule';

-- ============================================================
-- 7. TABLE JOURNAL_ACTION
-- ============================================================
CREATE TABLE journal_action (
    idLog           INT           AUTO_INCREMENT PRIMARY KEY,
    action          VARCHAR(100)  NOT NULL
                    COMMENT 'CONNEXION, CREATION_CONTRAT, RETOUR_VEHICULE, etc.',
    details         TEXT          NULL,
    dateHeure       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    adresseIP       VARCHAR(45)   NULL
                    COMMENT 'IPv4 ou IPv6',
    idUtilisateur   INT           NULL
                    COMMENT 'NULL si action système automatique',
    CONSTRAINT fk_log_util FOREIGN KEY (idUtilisateur)
        REFERENCES utilisateur(idUtilisateur) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Journal de toutes les actions utilisateurs';

-- ============================================================
-- 8. TABLE STATISTIQUE
-- ============================================================
CREATE TABLE statistique (
    idStat              INT            AUTO_INCREMENT PRIMARY KEY,
    mois                INT            NOT NULL,
    annee               INT            NOT NULL,
    nbLocations         INT            NOT NULL DEFAULT 0,
    revenuTotal         DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    tauxDisponibilite   DECIMAL(5,2)   NOT NULL DEFAULT 0.00
                        COMMENT 'Pourcentage moyen de disponibilité sur la période',
    dateGeneration      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    idAgence            INT            NOT NULL,
    CONSTRAINT fk_stat_agence   FOREIGN KEY (idAgence)
        REFERENCES agence(idAgence) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_stat_periode  UNIQUE (mois, annee, idAgence),
    CONSTRAINT chk_mois_val     CHECK (mois BETWEEN 1 AND 12),
    CONSTRAINT chk_annee_stat   CHECK (annee >= 2020)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Statistiques mensuelles de rentabilité';

-- ============================================================
-- INDEX DE PERFORMANCE
-- ============================================================
CREATE INDEX idx_veh_statut       ON vehicule(statut);
CREATE INDEX idx_veh_marque       ON vehicule(marque);
CREATE INDEX idx_contrat_statut   ON contrat(statut);
CREATE INDEX idx_contrat_dates    ON contrat(dateDebut, dateFin);
CREATE INDEX idx_contrat_client   ON contrat(idClient);
CREATE INDEX idx_contrat_veh      ON contrat(idVehicule);
CREATE INDEX idx_log_date         ON journal_action(dateHeure);
CREATE INDEX idx_log_action       ON journal_action(action);
CREATE INDEX idx_util_role        ON utilisateur(role);
CREATE INDEX idx_util_actif       ON utilisateur(actif);

-- ============================================================
-- DONNÉES DE TEST — EGJS AutoRent Brazzaville
-- ============================================================

-- Agence
INSERT INTO agence (nom, ville, adresse, telephone, email, tauxPenalite)
VALUES (
    'EGJS AutoRent',
    'Brazzaville',
    'Avenue de France, Centre-ville, Brazzaville, Congo',
    '+242 06 123 4567',
    'contact@egjs-autorent.cg',
    5000.00
);

-- Administrateur (mdp : Admin@2026)
INSERT INTO utilisateur
    (nom, prenom, email, motDePasseHash, role, idAgence)
VALUES (
    'MBEMBA', 'Elvis',
    'admin@egjs-autorent.cg',
    '$2a$12$KIX3oEXAMPLEHASHadmin00000000000000000000000000000000',
    'ADMINISTRATEUR', 1
);

-- Gestionnaire (mdp : Gest@2026)
INSERT INTO utilisateur
    (nom, prenom, email, motDePasseHash, role, telephone, idAgence)
VALUES (
    'NGANGA', 'Joëlle',
    'gestionnaire@egjs-autorent.cg',
    '$2a$12$KIX3oEXAMPLEHASHgest00000000000000000000000000000000',
    'GESTIONNAIRE', '+242 05 987 6543', 1
);

-- Clients
INSERT INTO utilisateur
    (nom, prenom, email, motDePasseHash, role, telephone, adresse, numPermis, idAgence)
VALUES
    ('MOUKALA',  'Jean-Pierre', 'jp.moukala@gmail.com',
     '$2a$12$KIX3oEXAMPLEHASH00100000000000000000000000000000000',
     'CLIENT', '+242 06 111 2222', 'Bacongo, Brazzaville',    'CG-BZV-2021-00142', 1),
    ('OSSEKE',   'Marie-Claire','mc.osseke@gmail.com',
     '$2a$12$KIX3oEXAMPLEHASH00200000000000000000000000000000000',
     'CLIENT', '+242 05 333 4444', 'Poto-Poto, Brazzaville',  'CG-BZV-2019-00871', 1),
    ('BOUANGA',  'Rodrigue',    'r.bouanga@yahoo.fr',
     '$2a$12$KIX3oEXAMPLEHASH00300000000000000000000000000000000',
     'CLIENT', '+242 06 555 6666', 'Moungali, Brazzaville',   'CG-PNR-2022-00339', 1);

-- Véhicules (prix en XAF/jour)
INSERT INTO vehicule
    (marque, modele, annee, immatriculation, prixJour, statut, kilometrage, idAgence)
VALUES
    ('Toyota',       'Land Cruiser 300',    2023, 'BZV-2023-001',  75000.00, 'DISPONIBLE',     12500, 1),
    ('Toyota',       'Fortuner 4x4',        2022, 'BZV-2022-015',  55000.00, 'DISPONIBLE',     28300, 1),
    ('Mercedes-Benz','Classe E 300',         2023, 'BZV-2023-007',  90000.00, 'LOUE',            8200, 1),
    ('Range Rover',  'Sport HSE',           2022, 'BZV-2022-033', 120000.00, 'DISPONIBLE',     15600, 1),
    ('Toyota',       'Hilux Double Cabine', 2021, 'BZV-2021-044',  45000.00, 'EN_MAINTENANCE', 42000, 1),
    ('KIA',          'Sportage EX',         2023, 'BZV-2023-012',  40000.00, 'DISPONIBLE',      5100, 1),
    ('Ford',         'Ranger Wildtrak',     2022, 'BZV-2022-028',  50000.00, 'DISPONIBLE',     31200, 1);

-- Contrats
INSERT INTO contrat
    (dateDebut, dateFin, dateRetourReelle, montantTotal, statut, idClient, idVehicule, idAgence)
VALUES
    ('2026-04-01','2026-04-05','2026-04-05', 275000.00, 'TERMINE',   3, 2, 1),
    ('2026-04-20','2026-04-25', NULL,         450000.00, 'EN_COURS',  4, 3, 1),
    ('2026-03-10','2026-03-12','2026-03-15',  100000.00, 'EN_RETARD', 3, 7, 1);

-- Pénalité (contrat 3 : 3 jours de retard × 5000 XAF)
INSERT INTO penalite (joursRetard, tauxJour, montant, idContrat)
VALUES (3, 5000.00, 15000.00, 3);

-- Maintenance (Toyota Hilux en cours)
INSERT INTO maintenance (type, dateEntree, dateSortie, cout, description, idVehicule)
VALUES ('Vidange + Filtres', '2026-04-22', NULL, 45000.00,
        'Vidange huile moteur et remplacement filtres air/huile - Kilométrage : 42000 km', 5);

-- Journal (quelques entrées initiales)
INSERT INTO journal_action (action, details, idUtilisateur)
VALUES
    ('CONNEXION',         'Connexion administrateur',           1),
    ('CREATION_CONTRAT',  'Contrat #1 créé pour MOUKALA',       2),
    ('CREATION_CONTRAT',  'Contrat #2 créé pour OSSEKE',        2),
    ('RETOUR_VEHICULE',   'Retour véhicule #7 - pénalité 15000 XAF', 2);

-- ============================================================
-- FIN DU SCRIPT v2
-- ============================================================
