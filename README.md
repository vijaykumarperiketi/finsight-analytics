# Financial Analytics Platform

This project is a comprehensive financial analytics platform composed of multiple microservices. The platform is built using Spring Boot, React.js, Kafka, Neo4j, Redis, and other technologies. The microservices communicate with each other using Feign clients and the platform includes logging and monitoring using the ELK stack, Prometheus, and Grafana.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
- [Kubernetes Deployment](#kubernetes-deployment)
- [CI/CD with GitHub Actions](#cicd-with-github-actions)
- [Environment Variables](#environment-variables)
- [Services](#services)

## Prerequisites

- Docker and Docker Compose
- kubectl
- Helm
- GitHub account
- Docker Hub account

## Local Setup

### Step 1: Install Docker and Docker Compose

1. Download and install Docker Desktop from [Docker's official site](https://www.docker.com/products/docker-desktop).
2. Verify the installation:
   ```sh
   docker --version
   docker-compose --version
