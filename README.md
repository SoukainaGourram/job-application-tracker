# JobTrack

**JobTrack** est une application web **Full-Stack** conçue pour aider les utilisateurs à organiser, centraliser et suivre leurs candidatures tout au long du processus de recrutement.

## Fonctionnalités

* 🔐 **Authentification des utilisateurs** avec JWT
* 🏢 **Gestion des entreprises**
* 💼 **Gestion des offres d'emploi**
* 📋 **Suivi des candidatures**
* 📊 **Pipeline Kanban** pour visualiser l'avancement des candidatures
* 🕒 **Historique des candidatures**
* 📅 **Gestion des entretiens**
* 👤 **Gestion des contacts au sein des entreprises**
* 🔒 **Isolation des données entre les utilisateurs**

## Technologies utilisées

### Backend

* Java 21
* Spring Boot
* Spring Security
* JWT
* PostgreSQL
* API REST

### Frontend

* Angular
* TypeScript
* Angular Material

### DevOps

* Docker
* Docker Compose
* GitHub Actions

## Architecture

JobTrack repose sur une architecture **client-serveur** composée de trois parties principales :

* **Frontend :** application Angular
* **Backend :** API REST développée avec Spring Boot
* **Base de données :** PostgreSQL

## Installation et exécution

### 1. Cloner le dépôt

```bash
git clone https://github.com/SoukainaGourram/jobtrack.git
cd jobtrack
```

### 2. Lancer l'application

L'application peut être exécutée à l'aide de **Docker Compose** afin de démarrer les différents services nécessaires.

```bash
docker-compose up --build
```

### 3. Accéder à l'application

Une fois les services démarrés, vous pouvez accéder à l'application depuis votre navigateur à l'adresse configurée pour le frontend Angular.

## Objectif du projet

L'objectif de JobTrack est de fournir une solution centralisée permettant aux candidats de gérer efficacement leur recherche d'emploi, depuis l'ajout d'une offre jusqu'au suivi des candidatures, des entretiens et des échanges avec les recruteurs.

Le projet met également en pratique des concepts modernes du développement logiciel tels que **Spring Boot, Spring Security, JWT, API REST, Angular, PostgreSQL, Docker et CI/CD avec GitHub Actions**.
