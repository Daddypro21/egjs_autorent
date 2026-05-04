# EGJS AutoRent

Application desktop de gestion de location de voitures — Brazzaville, République du Congo.  
Développée en Java 17 + JavaFX 21, sans framework web ni ORM.

---

## Prérequis

Installe ces 3 outils avant tout :

| Outil | Version | Téléchargement |
|-------|---------|----------------|
| Java JDK | 17+ (LTS) | https://adoptium.net |
| Gradle | 8+ | https://gradle.org/releases |
| XAMPP (MySQL) | 8.0+ | https://apachefriends.org |

> **JavaFX n'est pas à installer séparément** — Gradle le télécharge automatiquement.

### Vérifier les installations

```bash
java --version    # doit afficher java 17.x.x ou supérieur
gradle --version  # doit afficher Gradle 8.x ou supérieur
```

### Ajouter Gradle au PATH (Windows)

1. Décompresser l'archive Gradle dans `C:\Gradle\gradle-8.x`
2. Ouvrir **Paramètres système → Variables d'environnement**
3. Dans **Path**, ajouter : `C:\Gradle\gradle-8.x\bin`
4. Rouvrir le terminal et taper `gradle --version` pour vérifier

---

## Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/<utilisateur>/autorent-fx.git
cd autorent-fx
```

### 2. Démarrer MySQL

Ouvrir le panneau XAMPP et cliquer **Start** en face de **MySQL**.

### 3. Importer la base de données

```bash
mysql -u root -p < autorent_database_v2.sql
```
Appuyer sur Entrée quand le mot de passe est demandé (vide par défaut avec XAMPP).

### 4. Configurer la connexion BDD

Éditer ce fichier si nécessaire :

```
src/main/resources/cg/egjs/autorent/database.properties
```

```properties
db.url=jdbc:mysql://localhost:3306/egjs_autorent?useSSL=false&serverTimezone=Africa/Brazzaville&characterEncoding=UTF-8
db.user=root
db.password=
```

> Laisser `db.password=` vide si XAMPP est utilisé sans mot de passe root.

### 5. Initialiser les mots de passe *(une seule fois)*

```bash
gradle initPasswords
```

Cette commande hash les mots de passe de test avec BCrypt et les enregistre en base.  
**À exécuter une seule fois** après l'import du script SQL.

### 6. Lancer l'application

```bash
gradle run
```

La fenêtre de connexion s'ouvre après quelques secondes.

---

## Comptes de test

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Administrateur | admin@egjs-autorent.cg | Admin@2026 |
| Gestionnaire | gestionnaire@egjs-autorent.cg | Gest@2026 |
| Client | jp.moukala@gmail.com | Client@2026 |

---

## Commandes utiles

```bash
gradle run            # Lancer l'application
gradle initPasswords  # Initialiser les mots de passe (1 seule fois)
gradle jar            # Compiler un JAR autonome avec toutes les dépendances
gradle generateDoc    # Générer la documentation PDF
```

---

## Architecture

```
src/main/java/cg/egjs/autorent/
├── app/          → MainApp.java (point d'entrée JavaFX)
├── config/       → DatabaseConnection (singleton JDBC)
├── model/        → Entités : Vehicule, Contrat, Utilisateur…
├── dao/          → Accès BDD via JDBC + PreparedStatement
├── controller/   → Logique métier (AuthController, ContratController…)
├── service/      → PDFService (iText 7), PenaliteService
├── util/         → SessionManager, ThemeManager, FormatUtil (XAF), PasswordUtil
└── view/         → Interface JavaFX construite en Java pur (pas de FXML)
```

---

## Technologies

| Technologie | Rôle |
|-------------|------|
| Java 17 | Langage principal |
| JavaFX 21 | Interface graphique |
| Gradle 8 | Build et gestion des dépendances |
| MySQL 8.0 | Base de données |
| BCrypt (jbcrypt 0.4) | Hashage des mots de passe |
| iText 7 | Génération de PDF |
| SLF4J | Logging |

---

## Sécurité

- Mots de passe hashés BCrypt (coût 12)
- Blocage du compte après 3 tentatives échouées
- Toutes les actions sont journalisées (`journal_action`)
- Connexion BDD via singleton thread-safe

---

## Monnaie

Tous les montants sont en **XAF** (Franc CFA d'Afrique Centrale).
