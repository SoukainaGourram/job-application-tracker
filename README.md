# JobTrack — Plateforme de Suivi de Candidatures

> Projet portfolio professionnel Full-Stack — **Angular** / **Java 21 Spring Boot 3** / **PostgreSQL**

---

## 📌 Présentation

**JobTrack** est une application web moderne conçue pour permettre aux chercheurs d'emploi et aux développeurs de centraliser, organiser et piloter efficacement l'ensemble de leurs candidatures et entretiens.

Ce projet démontre les meilleures pratiques de conception logicielle :
- **Architecture découplée** : Backend API REST stateless sécurisée par JWT, Frontend SPA standalone réactif.
- **Sécurité rigoureuse** : Mots de passe hashés avec BCrypt, filtres d'authentification Spring Security, routes protégées par Guards Angular, isolation des données utilisateurs.
- **Typage strict & Validation** : Bean Validation (JSR-380) côté serveur, Reactive Forms typés côté client.
- **Documentation vivante** : OpenAPI 3 / Swagger UI avec support d'authentification Bearer token intégrée.

---

## 🛠️ Stack Technique

### Backend
- **Java** : 21 LTS
- **Framework** : Spring Boot 3.3.4
- **Sécurité** : Spring Security 6 (Stateless, filtre JWT personnalisé)
- **Tokens** : JJWT (Java JWT) 0.12.6
- **Persistance** : Spring Data JPA / Hibernate 6
- **Base de données** : PostgreSQL 18
- **Documentation API** : Springdoc OpenAPI 2.6.0 (Swagger UI)
- **Productivité & Mapping** : Lombok, MapStruct
- **Tests** : JUnit 5, Mockito, AssertJ, Spring MockMvc

### Frontend
- **Framework** : Angular (Architecture moderne **Standalone Components**, sans `AppModule`)
- **Langage** : TypeScript
- **Routage** : Angular Router avec lazy loading dynamique (`loadComponent`)
- **Formulaires** : Reactive Forms typés avec validateurs synchrones et gestion fine des erreurs
- **Communication HTTP** : `HttpClient` avec intercepteurs fonctionnels (`authInterceptor`, `errorInterceptor`)
- **État réactif** : Angular Signals (`signal`, `computed`) pour l'état d'authentification utilisateur
- **Design & Styles** : SCSS modulaire (Design tokens, mixins partagés, thème sombre moderne)
- **Tests** : Vitest, Angular Testing Library, JSDOM

---

## 🔒 Choix d'Architecture : Sécurité et Stockage JWT

> **Note sur le stockage du token JWT :**
> Dans le cadre de ce portfolio technique, le token JWT est conservé dans le **`localStorage`** du navigateur via le service dédié `TokenService`. 
>
> **Considération pour la production :**
> Dans une application d'entreprise à haute sécurité exposée au grand public, il est recommandé d'émettre le JWT dans un cookie sécurisé avec les attributs **`httpOnly`**, **`Secure`** et **`SameSite=Strict`** afin de prémunir totalement le token des attaques XSS (Cross-Site Scripting). La solution actuelle en `localStorage` a été choisie ici pour simplifier l'inspection du token lors des démonstrations et garantir une intégration fluide sans dépendance complexe de domaine en environnement de développement local.

---

## ⚙️ Configuration & Variables d'Environnement

Un fichier exemple `.env.example` est fourni à la racine. Pour configurer l'environnement local, copiez ce fichier vers `.env` :

```bash
cp .env.example .env
```

| Variable | Description | Valeur par défaut (dév) |
|---|---|---|
| `DB_HOST` | Hôte PostgreSQL | `localhost` |
| `DB_PORT` | Port PostgreSQL | `5432` |
| `DB_NAME` | Nom de la base de données | `jobtrack` |
| `DB_USERNAME` | Utilisateur de base de données | `jobtrack_user` |
| `DB_PASSWORD` | Mot de passe de base de données | `jobtrack_dev_password` |
| `JWT_SECRET` | Clé secrète HMAC-SHA (min. 256 bits, encodée base64) | *(clé fournie pour dev)* |
| `JWT_EXPIRATION` | Durée de validité du token en ms (24h) | `86400000` |
| `FRONTEND_URL` | Origine autorisée par le filtre CORS | `http://localhost:4200` |

---

## 🚀 Démarrage Rapide

### 1. Prérequis
- Java 21 LTS
- Maven 3.9+
- Node.js 22+ & npm
- PostgreSQL 18 (ou Docker)

### 2. Base de données PostgreSQL
Créez la base et l'utilisateur avec `psql` :
```sql
CREATE USER jobtrack_user WITH PASSWORD 'jobtrack_dev_password';
CREATE DATABASE jobtrack OWNER jobtrack_user;
GRANT ALL PRIVILEGES ON DATABASE jobtrack TO jobtrack_user;
```

### 3. Démarrer le Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```
Le backend sera disponible sur : **`http://localhost:8080`**  
Documentation Swagger UI : **`http://localhost:8080/swagger-ui.html`**  
Spécification OpenAPI JSON : **`http://localhost:8080/api-docs`**

### 4. Démarrer le Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
L'application Angular sera accessible sur : **`http://localhost:4200`**

---

## 📡 Endpoints de l'API (Phase 2 — Authentification)

| Méthode | Endpoint | Accès | Description | Code Succès |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Inscription d'un nouvel utilisateur | `201 Created` |
| `POST` | `/api/auth/login` | Public | Authentification et émission du JWT | `200 OK` |
| `GET` | `/api/auth/me` | Authentifié (`Bearer <JWT>`) | Récupération du profil connecté | `200 OK` |

### Format des requêtes / réponses

#### Inscription (`POST /api/auth/register`)
```json
{
  "firstName": "Marie",
  "lastName": "Curie",
  "email": "marie.curie@jobtrack.dev",
  "password": "Password123!"
}
```

#### Réponse Auth (`AuthResponse`)
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "firstName": "Marie",
    "lastName": "Curie",
    "email": "marie.curie@jobtrack.dev",
    "role": "USER",
    "createdAt": "2026-09-27T13:38:30.782289Z"
  }
}
```

---

## 🧪 Exécution des Tests

### Tests Backend (JUnit 5 + MockMvc)
```bash
cd backend
mvn test
```
*Couvre : tests unitaires du service d'authentification (`AuthServiceTest`), tests unitaires JWT (`JwtServiceTest`), tests d'intégration des contrôleurs avec MockMvc et contexte de sécurité Spring (`AuthControllerTest`).*

### Tests Frontend (Vitest)
```bash
cd frontend
npm test -- --watch=false
```
*Couvre : tests du `TokenService`, de l'`AuthService` réactif, de l'`AuthGuard`, de l'`AuthInterceptor` HTTP, du `LoginComponent` et du `RegisterComponent`.*

---

## 📁 Structure du Répertoire

```text
jobtrack/
├── .github/
│   └── workflows/
│       └── ci.yml                 # Pipeline CI GitHub Actions (Java + Angular + Docker)
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/jobtrack/
│       │   │   ├── config/        # SecurityConfig, ApplicationConfig, OpenApiConfig, JpaAuditingConfig
│       │   │   ├── controller/    # AuthController
│       │   │   ├── dto/           # Request & Response DTOs
│       │   │   ├── entity/        # User entity, Role enum
│       │   │   ├── exception/     # GlobalExceptionHandler, Custom exceptions
│       │   │   ├── repository/    # UserRepository
│       │   │   ├── security/      # JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl
│       │   │   └── service/       # AuthService
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-dev.yml
│       └── test/
│           ├── java/com/jobtrack/
│           │   ├── controller/    # AuthControllerTest
│           │   ├── security/      # JwtServiceTest
│           │   └── service/       # AuthServiceTest
│           └── resources/
│               └── application.yml
├── frontend/
│   ├── angular.json
│   ├── package.json
│   └── src/
│       ├── index.html
│       ├── main.ts
│       ├── styles.scss            # Variables CSS & Design tokens
│       └── app/
│           ├── app.config.ts      # Fournisseurs standalone (Router, Interceptors, HttpClient)
│           ├── app.routes.ts      # Définition des routes et guards
│           ├── core/
│           │   ├── guards/        # authGuard
│           │   ├── interceptors/  # authInterceptor, errorInterceptor
│           │   ├── models/        # Interfaces TypeScript
│           │   └── services/      # AuthService, TokenService
│           ├── features/
│           │   ├── auth/          # login/ & register/ (Standalone + SCSS)
│           │   └── dashboard/     # Page Dashboard temporaire protégée
│           └── layout/
│               └── main-layout/   # Shell avec Sidebar de navigation & Header
├── .env.example
├── .gitignore
└── README.md
```
