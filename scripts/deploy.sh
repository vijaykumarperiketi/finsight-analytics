#!/bin/bash

# Fail script on any error
set -e

# Create namespace
echo "Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# Define microservices
SERVICES=("user-management" "transaction-service" "notification-service" "recommendation-engine" "analytics-service" "admin-service")

# Build Docker images for all services
for i in "${!SERVICES[@]}"; do
  SERVICE=${SERVICES[$i]}
  
  echo "Building Docker image for ${SERVICE} from ${SERVICE}..."
  docker build -t vijaykumarperiketi/${SERVICE}:latest ./backend/${SERVICE}
  
  echo "Pushing Docker image for ${SERVICE} to Docker Hub..."
  docker push vijaykumarperiketi/${SERVICE}:latest
done

# Build Docker image for frontend
echo "Building Docker image for frontend..."
docker build -t vijaykumarperiketi/frontend:latest ./frontend

echo "Pushing Docker image for frontend to Docker Hub..."
docker push vijaykumarperiketi/frontend:latest

# Deploy ELK stack
echo "Deploying Elasticsearch..."
kubectl apply -f k8s/logging/elasticsearch/deployment.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/logging/elasticsearch/service.yaml --namespace financial-analytics-platform

echo "Deploying Kibana..."
kubectl apply -f k8s/logging/kibana/deployment.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/logging/kibana/service.yaml --namespace financial-analytics-platform

echo "Deploying Logstash..."
kubectl apply -f k8s/logging/logstash/configmap.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/logging/logstash/deployment.yaml --namespace financial-analytics-platform

# Deploy Prometheus and Grafana
echo "Deploying Prometheus..."
kubectl apply -f k8s/monitoring/prometheus/configmap.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/monitoring/prometheus/deployment.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/monitoring/prometheus/service.yaml --namespace financial-analytics-platform

echo "Deploying Grafana..."
kubectl apply -f k8s/monitoring/grafana/configmap.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/monitoring/grafana/deployment.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/monitoring/grafana/secret.yaml --namespace financial-analytics-platform
kubectl apply -f k8s/monitoring/grafana/service.yaml --namespace financial-analytics-platform

# Create Kubernetes secret for Vault token
echo "Creating Kubernetes secret for Vault token..."
kubectl apply -f k8s/templates/secret.yaml --namespace financial-analytics-platform

# Deploy using Helm
echo "Deploying application using Helm..."
helm upgrade --install financial-analytics-platform ./financial-analytics-platform --namespace financial-analytics-platform

# Apply ingress configuration
echo "Applying ingress configuration..."
kubectl apply -f k8s/ingress/ingress.yaml --namespace financial-analytics-platform

echo "Deployment complete!"
