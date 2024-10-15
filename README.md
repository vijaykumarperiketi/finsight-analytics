
# Financial Analytics Platform

## Overview

The **Financial Analytics Platform** is a microservices-based application designed to handle transactions, provide personalized recommendations, manage financial and debt goals, send notifications, and offer in-depth analytics. The platform includes Role-Based Access Control (RBAC) for Customers, Bankers, and Administrators.

The microservices architecture enables scalability, modular development, and ease of deployment. Each service is containerized using Docker and orchestrated via Kubernetes, with the potential to be deployed in an AWS environment in the future.

---

## Features

- **User Management**: Manages users, roles, and authentication (RBAC with roles: Customer, Banker, Admin).
- **Transaction Management**: Allows users to create, view, and manage transactions.
- **Financial Goals and Debt Management**: Tracks financial and debt goals for users.
- **Recommendation Engine**: Provides personalized and goal-based recommendations using Neo4j.
- **Notification Service**: Sends notifications (email, SMS, and in-app) for transaction updates.
- **Analytics**: Offers detailed analytics and dashboards for administrators and users.
- **Admin Service**: Enables admins to monitor system health and user activity.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Technologies](#technologies)
3. [Pre-requisites](#pre-requisites)
4. [Running Locally (Docker)](#running-locally-docker)
5. [Deploying to Kubernetes](#deploying-to-kubernetes)
6. [AWS Migration (In Progress)](#aws-migration-in-progress)
7. [API Endpoints](#api-endpoints)
8. [Testing and Smoke Tests](#testing-and-smoke-tests)
9. [Roadmap](#roadmap)

---

## Architecture

The platform is composed of the following microservices:

1. **User Management Service**: Handles user registration, authentication, financial goals, and debts.
2. **Transaction Service**: Processes transactions (credit, debit, financial goal, debt goal).
3. **Recommendation Engine**: Generates recommendations using Neo4j graph database.
4. **Notification Service**: Sends notifications to users via email, SMS, and in-app.
5. **Analytics Service**: Aggregates and provides analytics data.
6. **Admin Service**: Enables admin monitoring and management of system-wide activities.

Each microservice is containerized and deployed via Docker/Kubernetes, with internal communication facilitated by REST and asynchronous Kafka messaging.

---

## Technologies

- **Spring Boot** (Microservices)
- **React** (Frontend)
- **PostgreSQL** (Database)
- **Neo4j** (Graph Database)
- **Kafka** (Message Queue)
- **Redis** (Caching)
- **Docker** (Containerization)
- **Kubernetes** (Orchestration)
- **AWS** (Potential future cloud platform)

---

## Pre-requisites

- **Docker** (>= 20.10)
- **Kubernetes** (>= 1.21)
- **Node.js** (>= 14.x)
- **Java** (>= 11)
- **Maven** (>= 3.8)
- **Vault** for secrets management (in Kubernetes)

---

## Running Locally (Docker)

### Step 1: Clone the Repository

```bash
git clone https://github.com/vijaykumarperiketi/finsight-analytics.git
cd finsight-analytics
```

### Step 2: Build the Application

Build the backend services using Maven:

```bash
mvn clean install -DskipTests
```

### Step 3: Spin up Services with Docker Compose

The following services will be started using Docker Compose:

- PostgreSQL for the database
- Kafka for messaging
- Redis for caching
- Neo4j for graph-based recommendations
- Microservices (User Management, Transaction, Notification, etc.)

```bash
docker-compose up --build
```

### Step 4: Access the Application

- Frontend: http://localhost:3000
- User Management API: http://localhost:8081
- Transaction API: http://localhost:8082
- Notification API: http://localhost:8083
- Recommendation API: http://localhost:8084
- Analytics API: http://localhost:8085
- Admin API: http://localhost:8086

---

## Deploying to Kubernetes

### Step 1: Install Prerequisites

- kubectl (>= 1.21)
- Minikube or a local Kubernetes cluster
- Helm (>= 3.x)

### Step 2: Build and Push Docker Images

Ensure Docker images for each service are pushed to a registry:

```bash
docker build -t yourusername/user-management:latest ./backend/usermanagement
docker push yourusername/user-management:latest
```

Repeat for each microservice.

### Step 3: Deploy Using Helm

Navigate to the Helm chart directory and deploy:

```bash
helm upgrade --install financial-analytics-platform ./financial-analytics-platform --namespace financial-analytics-platform
```

### Step 4: Access the Application

Use kubectl to retrieve the external IP and access the application via the Kubernetes Ingress:

```bash
kubectl get svc -n financial-analytics-platform
```

---

## AWS Migration (In Progress)

The application is being migrated to AWS for production. This involves the following steps:

- Deploying Kubernetes Cluster on AWS EKS.
- Using AWS RDS (PostgreSQL) for database management.
- Using Amazon MSK (Managed Kafka) for Kafka.
- ElasticCache for Redis caching.
- Amazon Neptune for graph database functionality if needed, otherwise using Neo4j on EC2.
- AWS S3 for static asset storage.

We will document the migration process in future commits.

---

## API Endpoints

| Service                 | Method | Endpoint                                   | Description                                  |
|-------------------------|--------|--------------------------------------------|----------------------------------------------|
| **User Management**     | POST   | /auth/login                                | User login and JWT token generation          |
|                         | GET    | /users/{id}/financial-goals                | Get all financial goals for a user           |
| **Transaction Service** | POST   | /transactions                              | Create a new transaction                     |
|                         | GET    | /transactions/user/{userId}                | Get transactions for a specific user         |
| **Recommendation**      | GET    | /recommendations/personalized/{userId}     | Get personalized recommendations             |
| **Notification**        | GET    | /notifications/user/{userId}               | Get notifications for a specific user        |
| **Analytics**           | GET    | /analytics/user/{userId}                   | Get analytics data for a user                |
| **Admin Service**       | GET    | /admin/users                               | List all users                               |

---

## Testing and Smoke Tests

### Smoke Test

- Verify that all services are running using health checks: `/actuator/health`.
- Use Postman or cURL to test basic endpoints (login, transaction creation, recommendations).
- Run basic UI tests using Cypress or Selenium.

For details on the smoke test script, see the `test/smoke-test.sh` file.

---

## Roadmap

- **AWS Migration**: Continue the migration of services to AWS using RDS, MSK, and EC2.
- **Advanced Analytics**: Add more advanced data visualizations and insights.
- **Scaling**: Implement autoscaling in AWS for better load management.
- **CI/CD Pipeline**: Automate deployments using GitHub Actions integrated with AWS.

---

## Contributors

- **Vijay Kumar Periketi** - Project Lead & Developer

---
