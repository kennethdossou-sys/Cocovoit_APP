# Campus Covoit' — API REST de covoiturage étudiant

API REST sécurisée permettant à des étudiants de proposer des trajets et de réserver des places. Un conducteur publie un trajet, des passagers réservent. Le cœur du projet réside dans les règles métier et le contrôle d'accès (authentification JWT, rôles, et propriété des ressources).

Projet de fin de module **Web JEE** — EPF Africa.

---

## Sommaire

1. [Stack technique](#1-stack-technique)
2. [Prérequis](#2-prérequis)
3. [Lancer le projet](#3-lancer-le-projet)
4. [Liens utiles](#4-liens-utiles)
5. [Comptes de test](#5-comptes-de-test)
6. [Architecture](#6-architecture)
7. [Modèle de données](#7-modèle-de-données)
8. [Contrat d'API (endpoints)](#8-contrat-dapi-endpoints)
9. [Règles métier et codes HTTP](#9-règles-métier-et-codes-http)
10. [Comment tester](#10-comment-tester)
11. [Note de sécurité](#11-note-de-sécurité-production)

---

## 1. Stack technique

| Domaine | Choix |
|---------|-------|
| Langage | Java 17 |
| Framework | Spring Boot 3.5.x |
| Web | Spring Web (REST) |
| Persistance | Spring Data JPA + base H2 en mémoire |
| Sécurité | Spring Security + JWT (jjwt 0.12.6), mots de passe BCrypt |
| Validation | Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Min`, `@Future`) |
| Documentation | springdoc-openapi 2.8.x (Swagger UI) |
| Build | Maven, packaging **jar** (Tomcat embarqué) |

---

## 2. Prérequis

- **JDK 17** ou supérieur (`java -version` pour vérifier)
- **Maven 3.8+** (`mvn -version`) — ou utiliser le wrapper `./mvnw` fourni
- Un client REST pour tester : **Postman**, l'extension **REST Client** de VS Code, ou directement **Swagger**

---

## 3. Lancer le projet

Depuis la racine du projet (le dossier contenant `pom.xml`) :

```bash
mvn spring-boot:run
```

ou avec le wrapper Maven (aucune installation de Maven requise) :

```bash
./mvnw spring-boot:run      # Linux / macOS
mvnw.cmd spring-boot:run    # Windows
```

L'application démarre sur **http://localhost:8080**. Le message `Started CocovoitApplication ...` dans la console indique que tout est prêt. Les comptes de test sont alors affichés dans la console.

Pour seulement compiler sans lancer :

```bash
mvn clean compile
```

---

## 4. Liens utiles

| Ressource | URL | Accès |
|-----------|-----|-------|
| **Swagger UI** (doc + test) | http://localhost:8080/swagger-ui.html | public |
| Spécification OpenAPI (JSON) | http://localhost:8080/v3/api-docs | public |
| **Console H2** (voir la base) | http://localhost:8080/h2-console | public |

**Connexion à la console H2** : champ *JDBC URL* = `jdbc:h2:mem:cocovoitdb`, *User Name* = `sa`, *Password* = (vide).

> ⚠️ Le nom de la base (`cocovoitdb`) doit correspondre exactement à la valeur `spring.datasource.url` de votre `src/main/resources/application.properties`. Adaptez si vous l'avez nommée différemment.

---

## 5. Comptes de test

Injectés automatiquement au démarrage via un `CommandLineRunner`.

| Username | Mot de passe | Rôle | Usage |
|----------|--------------|------|-------|
| `admin`  | `admin123`   | **ADMIN** | Modération : voir tous les comptes, supprimer trajets/comptes |
| `awa`    | `awa123`     | ÉTUDIANT | Conductrice du trajet de démo Dakar → Thiès |
| `cheikh` | `cheikh123`  | ÉTUDIANT | Conducteur du trajet Dakar → Saint-Louis |

Deux trajets de démonstration sont également créés au démarrage.

---

## 6. Architecture

Architecture en **couches strictes** :

```
controller  →  service  →  repository  →  (base H2)
   REST         règles       accès JPA
              métier
```

- **Aucune logique métier dans les contrôleurs**, aucune requête base directe : les contrôleurs délèguent tout aux services.
- **Injection par constructeur** partout (pas de `@Autowired` sur les champs).
- **DTO en entrée et en sortie** : les entités JPA ne sont jamais exposées directement. Le mot de passe (haché BCrypt) ne sort **jamais** dans une réponse JSON.
- **Gestion globale des erreurs** via `@RestControllerAdvice` : format JSON homogène, codes HTTP cohérents.

Arborescence des packages (`com.epfafrica.cocovoit`) :

```
config/      SecurityConfig, OpenApiConfig, DataInitializer (seed)
controller/  Auth, Trajet, Reservation, Admin
service/     logique métier
repository/  Spring Data JPA (query methods + @Query JPQL)
dto/         records d'entrée/sortie avec Bean Validation
entity/      Utilisateur, Trajet, Reservation + enums
exception/   exceptions métier + GlobalExceptionHandler
security/    JwtService, JwtAuthenticationFilter, UserDetails...
```

---

## 7. Modèle de données

Trois entités principales et leurs relations :

```
Utilisateur (1) ──< (N) Trajet        un utilisateur propose plusieurs trajets (conducteur)
Utilisateur (1) ──< (N) Reservation   un utilisateur fait plusieurs réservations (passager)
Trajet      (1) ──< (N) Reservation   un trajet reçoit plusieurs réservations
```

**Utilisateur** : id, username (unique), password (BCrypt), nom, prénom, email, role [ETUDIANT | ADMIN]

**Trajet** : id, villeDepart, villeArrivee, dateHeureDepart (futur), placesTotal, placesDisponibles (calculé), prixParPlace, statut [OUVERT | COMPLET | ANNULE], conducteur (FK Utilisateur)

**Reservation** : id, trajet (FK), passager (FK Utilisateur), nbPlaces, statut [CONFIRMEE | ANNULEE], dateReservation

Les associations sont des `@ManyToOne` côté propriétaire avec les `@OneToMany` inverses. Les DTO de sortie aplatissent ces relations pour éviter les boucles de sérialisation (Trajet → Reservation → Trajet).

---

## 8. Contrat d'API (endpoints)

Base : `http://localhost:8080`

### Authentification
| Méthode | Route | Accès | Description |
|---------|-------|-------|-------------|
| POST | `/auth/register` | public | Crée un compte ÉTUDIANT → 201 |
| POST | `/auth/login` | public | Renvoie un jeton JWT → 200 / 401 |
| GET  | `/auth/moi` | connecté | Profil de l'utilisateur courant → 200 |

### Trajets
| Méthode | Route | Accès | Description |
|---------|-------|-------|-------------|
| GET | `/api/trajets` | public | Liste + filtres `?depart=&arrivee=&date=` → 200 |
| GET | `/api/trajets/{id}` | public | Détail → 200 / 404 |
| POST | `/api/trajets` | connecté | Créer (créateur = conducteur) → 201 |
| PUT | `/api/trajets/{id}` | conducteur | Modifier → 200 / 403 / 404 |
| DELETE | `/api/trajets/{id}` | conducteur / ADMIN | Annuler → 204 / 403 |
| GET | `/api/trajets/{id}/reservations` | conducteur | Voir ses passagers → 200 / 403 |

### Réservations
| Méthode | Route | Accès | Description |
|---------|-------|-------|-------------|
| POST | `/api/trajets/{id}/reservations` | connecté | Réserver n places → 201 / 409 |
| GET | `/api/mes-reservations` | connecté | Mes réservations → 200 |
| DELETE | `/api/reservations/{id}` | passager / ADMIN | Annuler, libère les places → 204 / 403 |

### Administration
| Méthode | Route | Accès | Description |
|---------|-------|-------|-------------|
| GET | `/api/admin/utilisateurs` | ADMIN | Liste des comptes → 200 / 403 |
| DELETE | `/api/admin/utilisateurs/{id}` | ADMIN | Supprimer un compte → 204 / 403 |

---

## 9. Règles métier et codes HTTP

**Sémantique des codes** (notée dans le barème) :

| Code | Signification |
|------|---------------|
| 200 / 201 / 204 | Succès / Créé / Supprimé sans contenu |
| 400 | Requête mal formée (validation) |
| 401 | **Pas connecté** (token absent/invalide) |
| 403 | **Connecté mais sans le droit** (propriété ou rôle) |
| 404 | Ressource inexistante |
| 409 | Conflit métier |

> La distinction **401 vs 403** est centrale : *401 = le back ne sait pas qui je suis ; 403 = il le sait, mais je n'ai pas les droits sur cette ressource.*

**Règles implémentées** :

- On ne peut pas réserver son propre trajet → **409**
- On ne peut pas réserver un trajet COMPLET, ANNULE ou déjà passé → **409**
- Pas de double réservation CONFIRMEE sur le même trajet → **409**
- Places demandées > disponibles → **409**
- Réserver décrémente les places ; à 0, le trajet passe COMPLET
- Annuler une réservation restitue les places ; si COMPLET, repasse OUVERT
- Seul le passager (ou un ADMIN) peut annuler sa réservation → sinon **403**
- Seul le conducteur (ou un ADMIN) peut modifier/supprimer un trajet → sinon **403**
- Annuler un trajet annule toutes ses réservations en cascade
- Validation des entrées : date future, places ≥ 1, prix ≥ 0, email valide, champs non vides → **400**

---

## 10. Comment tester

### Option A — Swagger (rapide, mono-utilisateur)

1. Ouvrir http://localhost:8080/swagger-ui.html
2. Déplier `POST /auth/login` → **Try it out** → `{ "username":"awa","password":"awa123" }` → **Execute**
3. Copier le `token` de la réponse
4. Cliquer **Authorize** (en haut à droite), coller le token, valider
5. Tous les endpoints protégés utilisent désormais ce token

Pour changer d'utilisateur : refaire un login, re-cliquer **Authorize**, remplacer le token.

### Option B — Postman (scénario multi-acteurs)

1. Login (`POST /auth/login`) : onglet **Body → raw → JSON**, corps `{ "username":"awa","password":"awa123" }`
2. Récupérer le `token` dans la réponse
3. Sur chaque requête protégée : onglet **Authorization → Type: Bearer Token →** coller le token

**Astuce** : créer des variables d'environnement `tokenAwa`, `tokenCheikh`, `tokenAdmin` et les réutiliser via `{{tokenAwa}}` pour jouer plusieurs acteurs en parallèle.

### Option C — Fichier `.http` (REST Client VS Code)

Le fichier **`covoit.http`** à la racine couvre les 31 scénarios principaux **et les cas d'erreur** (400, 401, 403, 404, 409). Ouvrir le fichier dans VS Code et cliquer *Send Request* au-dessus de chaque requête.

### Scénario de démonstration recommandé

1. `register` un nouveau compte → **201** ; refaire avec un email invalide → **400**
2. `login` awa → **200 + token** ; mauvais mot de passe → **401**
3. `GET /api/trajets` sans token → **200** (public)
4. `POST /api/trajets` sans token → **401** ; avec token awa → **201**
5. cheikh tente `PUT` sur le trajet d'awa → **403** (propriété)
6. cheikh réserve 2 places → **201**
7. awa réserve son propre trajet → **409** ; cheikh double-réserve → **409**
8. dernière place réservée → trajet **COMPLET** ; réservation de plus → **409**
9. cheikh annule sa réservation → **204** ; places restituées, trajet **OUVERT**
10. `GET /api/admin/utilisateurs` avec awa → **403** ; avec admin → **200**

---

## 11. Note de sécurité (production)

Pour ce projet pédagogique, la clé JWT, sa durée de validité et les comptes par défaut sont **en clair** dans la configuration. En production, il faudrait :

- externaliser le secret JWT (variable d'environnement / coffre-fort de secrets)
- imposer des mots de passe forts et une politique de rotation
- servir l'API en **HTTPS** uniquement
- désactiver la console H2 et utiliser une base persistante (PostgreSQL)
- restreindre l'accès public à la documentation Swagger

---

*Projet réalisé dans le cadre du module Web JEE — EPF Africa, juin 2026.*
