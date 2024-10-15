#!/bin/sh

# Fetch secrets from Vault
DB_CREDENTIALS=$(curl --silent --header "X-Vault-Token: ${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/data/postgres | jq -r '.data.data')
JWT_SECRET=$(curl --silent --header "X-Vault-Token: ${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/data/jwt | jq -r '.data.data.secret')
KAFKA_CREDENTIALS=$(curl --silent --header "X-Vault-Token: ${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/data/kafka | jq -r '.data.data')
NEO4J_CREDENTIALS=$(curl --silent --header "X-Vault-Token: ${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/data/neo4j | jq -r '.data.data')
REDIS_CREDENTIALS=$(curl --silent --header "X-Vault-Token: ${VAULT_TOKEN}" ${VAULT_ADDR}/v1/secret/data/redis | jq -r '.data.data')

# Export database credentials as environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://${SPRING_DATASOURCE_HOST}:${SPRING_DATASOURCE_PORT}/${SPRING_DATASOURCE_DB}
export SPRING_DATASOURCE_USERNAME=$(echo $DB_CREDENTIALS | jq -r '.postgres-username')
export SPRING_DATASOURCE_PASSWORD=$(echo $DB_CREDENTIALS | jq -r '.postgres-password')

# Service-specific configurations
case "$SERVICE_NAME" in
  user-management)
    export JWT_SECRET=$JWT_SECRET
    ;;

  transaction-service)
    export KAFKA_BOOTSTRAP_SERVERS=$(echo $KAFKA_CREDENTIALS | jq -r '.bootstrapServers')
    export KAFKA_CONSUMER_GROUP=$(echo $KAFKA_CREDENTIALS | jq -r '.transaction-group')
    ;;

  notification-service)
    export KAFKA_BOOTSTRAP_SERVERS=$(echo $KAFKA_CREDENTIALS | jq -r '.bootstrapServers')
    export KAFKA_CONSUMER_GROUP=$(echo $KAFKA_CREDENTIALS | jq -r '.notification-group')
    ;;

  recommendation-engine)
    export SPRING_NEO4J_URI=$(echo $NEO4J_CREDENTIALS | jq -r '.uri')
    export SPRING_NEO4J_AUTHENTICATION_USERNAME=$(echo $NEO4J_CREDENTIALS | jq -r '.neo4j-username')
    export SPRING_NEO4J_AUTHENTICATION_PASSWORD=$(echo $NEO4J_CREDENTIALS | jq -r '.neo4j-password')
    export KAFKA_BOOTSTRAP_SERVERS=$(echo $KAFKA_CREDENTIALS | jq -r '.bootstrapServers')
    export KAFKA_CONSUMER_GROUP=$(echo $KAFKA_CREDENTIALS | jq -r '.recommendation-group')
    ;;

  analytics-service)
    export KAFKA_BOOTSTRAP_SERVERS=$(echo $KAFKA_CREDENTIALS | jq -r '.bootstrapServers')
    export KAFKA_CONSUMER_GROUP=$(echo $KAFKA_CREDENTIALS | jq -r '.analytics-group')
    ;;
  
  admin-service)
    export SPRING_REDIS_HOST=redis
    export SPRING_REDIS_PORT=6379
    export SPRING_REDIS_PASSWORD=$(echo $REDIS_CREDENTIALS | jq -r '.redis-password')
esac

# Start the application
exec java -jar ${SERVICE_NAME}.jar
