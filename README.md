# ProcureAI

Plateforme SaaS de gestion des achats en architecture microservices. Elle couvre tout le cycle, de la demande d’achat jusqu’au paiement et à la comptabilité, avec lecture automatique des factures (OCR + LLM) et rapprochement à trois voies.

## Fonctionnalités

- Cycle d’achat complet : demande → commande → réception → facture → rapprochement → paiement → écritures comptables
- Authentification JWT et contrôle d’accès par rôle (Admin, Manager, Employé, Comptable, Magasinier)
- Communication asynchrone via RabbitMQ pour les notifications métier
- OCR des factures (PaddleOCR) puis structuration JSON par Gemini (repli regex si pas de clé)
- Rapprochement à 3 voies : commande / réception / facture
- TVA marocaine (0, 7, 10, 14, 20 %) et écritures CGNC (6111, 3455, 4411, 5141)

## Architecture

```
Frontend (React)  →  API Gateway :8080  →  microservices (Eureka)
                              │
                    ┌─────────┴─────────┐
                    │                   │
              PostgreSQL           RabbitMQ
              (1 DB / service)     (notifications)
```

| Service | Rôle |
|---|---|
| `discovery-service` | Eureka (découverte de services) |
| `api-gateway` | Entrée unique, JWT, routage |
| `auth-service` | Utilisateurs, login, rôles |
| `supplier-service` | Fournisseurs et produits |
| `procurement-service` | Demandes d’achat |
| `order-service` | Commandes fournisseur |
| `reception-service` | Réceptions magasin |
| `invoice-service` | Factures, TVA, TTC |
| `matching-service` | Rapprochement 3 voies |
| `payment-service` | Paiements |
| `accounting-service` | Écritures comptables |
| `notification-service` | Notifications (consommateur RabbitMQ) |
| `ai-service` | OCR + Gemini (Python / FastAPI) |
| `frontend` | Interface React (Vite + Nginx) |

Les services métier communiquent aussi en synchrone via **OpenFeign**.

## Stack

- **Backend :** Java 21, Spring Boot 3, Spring Cloud (Gateway, Eureka, OpenFeign), Spring Data JPA
- **Messaging :** RabbitMQ
- **IA :** Python, FastAPI, PaddleOCR, Gemini
- **Frontend :** React 19, Vite, React Router
- **Infra :** Docker Compose, PostgreSQL 16

## Prérequis

- Docker Desktop
- (Optionnel) clé Gemini pour l’OCR structuré : [Google AI Studio](https://aistudio.google.com/apikey)

## Démarrage

Depuis le dossier `infrastructure` :

```powershell
# Optionnel : activer Gemini pour l'OCR
# Créer infrastructure/.env avec :
# GEMINI_API_KEY=votre_cle

docker compose up -d --build
```

Attendre que les services soient prêts (Eureka + gateway surtout), puis charger les données de démo :

```powershell
.\seed\seed.ps1
```

### URLs

| Application | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |

### Comptes de test

| Email | Mot de passe | Rôle |
|---|---|---|
| `tarik@company.com` | `tarik123` | ADMIN |
| `karim@company.com` | `karim123` | MANAGER |
| `sara@company.com` | `sara123` | EMPLOYEE |
| `hamid@company.com` | `hamid123` | MAGASINIER |
| `amina@company.com` | `amina123` | COMPTABLE |

## Flux métier (résumé)

1. L’employé crée une **demande d’achat** ; le manager l’approuve.
2. L’admin crée une **commande** fournisseur et l’envoie (notification RabbitMQ au magasinier).
3. Le magasinier enregistre la **réception**.
4. Le comptable saisit la **facture** (OCR optionnel) liée à la commande.
5. Le **rapprochement** compare commande, réception et facture → valide ou rejette.
6. Le comptable **paie** ; les **écritures comptables** HT / TVA / TTC sont enregistrées.

## OCR factures (`ai-service`)

1. PaddleOCR lit le texte (PDF / image).
2. Si `GEMINI_API_KEY` est définie, Gemini renvoie un JSON structuré (numéro, date, ICE, HT, TVA, TTC, lignes).
3. Sinon, ou en cas d’échec Gemini, un parseur regex prend le relais.

Endpoint (via le gateway) : `POST /api/ai/ocr/invoice` (rôle `COMPTABLE`).

## Rebuild d’un service

```powershell
cd infrastructure
docker compose up -d --build auth-service frontend
```

## Tests

```powershell
# Exemple pour un service Java
cd auth-service
mvn test

# Service IA
cd ai-service
.\.venv\Scripts\python.exe -m pytest -q
```

## Structure du dépôt

```
ProcureAI/
├── api-gateway/
├── discovery-service/
├── auth-service/
├── supplier-service/
├── procurement-service/
├── order-service/
├── reception-service/
├── invoice-service/
├── matching-service/
├── payment-service/
├── accounting-service/
├── notification-service/
├── ai-service/          # FastAPI + PaddleOCR + Gemini
├── frontend/            # React
└── infrastructure/
    ├── docker-compose.yml
    ├── .env             # secrets locaux (non versionné)
    ├── postgres/
    └── seed/
```

## Licence

Projet  Personnel
