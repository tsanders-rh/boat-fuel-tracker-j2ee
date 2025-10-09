# Deployment Guide for Boat Fuel Tracker (Quarkus)

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+ (or configure different database)
- Docker (optional, for containerized deployment)
- Kubernetes/OpenShift (optional, for cloud deployment)

## Deployment Options

### 1. Development Mode (Hot Reload)

Perfect for local development with automatic code reloading:

```bash
# Start in dev mode
mvn quarkus:dev

# Access the application
curl http://localhost:8080/fuelups/user/test-user
```

**Features:**
- Live reload on code changes
- Dev UI at http://localhost:8080/q/dev
- Continuous testing mode
- Database schema auto-update

---

### 2. JVM Mode Deployment

Traditional JAR deployment with fast startup (~1-2 seconds):

```bash
# Package the application
mvn clean package

# Run the application
java -jar target/quarkus-app/quarkus-run.jar

# Or use the provided script
./target/quarkus-app/quarkus-run
```

**Configuration:**
Set environment variables or create `application.properties`:
```bash
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://localhost:3306/boatfuel
export QUARKUS_DATASOURCE_USERNAME=boatfuel
export QUARKUS_DATASOURCE_PASSWORD=changeme

java -jar target/quarkus-app/quarkus-run.jar
```

---

### 3. Docker Container Deployment

#### Option A: Docker Compose (Recommended for Development)

Complete stack with MySQL included:

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

Access: http://localhost:8080

#### Option B: Build and Run Container Manually

```bash
# 1. Package the application
mvn clean package

# 2. Build Docker image
docker build -f Dockerfile.jvm -t boat-fuel-tracker:latest .

# 3. Run MySQL
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=boatfuel \
  -e MYSQL_USER=boatfuel \
  -e MYSQL_PASSWORD=changeme \
  -p 3306:3306 \
  mysql:8.0

# 4. Run application
docker run -d \
  --name boat-fuel-app \
  --link mysql:mysql \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://mysql:3306/boatfuel \
  -e QUARKUS_DATASOURCE_USERNAME=boatfuel \
  -e QUARKUS_DATASOURCE_PASSWORD=changeme \
  -p 8080:8080 \
  boat-fuel-tracker:latest
```

---

### 4. Native Binary Deployment (Ultra-fast startup)

Native image with ~0.016s startup time and minimal memory footprint:

**Prerequisites:**
- GraalVM 22.3+ (Java 17)
- Native image tools

```bash
# Build native executable
mvn clean package -Pnative

# Run the native binary
./target/*-runner

# Or build native Docker container
mvn clean package -Pnative -Dquarkus.native.container-build=true
docker build -f Dockerfile.native -t boat-fuel-tracker:native .
docker run -p 8080:8080 boat-fuel-tracker:native
```

**Benefits:**
- ~15ms startup time
- ~30MB memory footprint
- No JVM required in container
- Instant scale-up in Kubernetes

---

### 5. Kubernetes/OpenShift Deployment

#### Create Kubernetes Resources

```bash
# Add Quarkus Kubernetes extension
mvn quarkus:add-extension -Dextensions="kubernetes"
```

#### kubernetes.yml Example

Create `src/main/kubernetes/kubernetes.yml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: boat-fuel-tracker
spec:
  selector:
    app: boat-fuel-tracker
  ports:
    - port: 8080
      targetPort: 8080
  type: LoadBalancer
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: boat-fuel-tracker
spec:
  replicas: 3
  selector:
    matchLabels:
      app: boat-fuel-tracker
  template:
    metadata:
      labels:
        app: boat-fuel-tracker
    spec:
      containers:
      - name: boat-fuel-tracker
        image: boat-fuel-tracker:latest
        ports:
        - containerPort: 8080
        env:
        - name: QUARKUS_DATASOURCE_JDBC_URL
          value: jdbc:mysql://mysql-service:3306/boatfuel
        - name: QUARKUS_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: QUARKUS_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        livenessProbe:
          httpGet:
            path: /q/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

#### Deploy to Kubernetes

```bash
# Build and push image
docker build -f Dockerfile.jvm -t your-registry/boat-fuel-tracker:1.0 .
docker push your-registry/boat-fuel-tracker:1.0

# Create secret for database credentials
kubectl create secret generic db-credentials \
  --from-literal=username=boatfuel \
  --from-literal=password=changeme

# Deploy application
kubectl apply -f src/main/kubernetes/kubernetes.yml

# Check status
kubectl get pods
kubectl get services

# View logs
kubectl logs -f deployment/boat-fuel-tracker
```

#### Using Quarkus Kubernetes Extension

Alternatively, let Quarkus generate Kubernetes manifests:

Add to `application.properties`:
```properties
quarkus.kubernetes.deployment-target=kubernetes
quarkus.container-image.build=true
quarkus.container-image.group=your-registry
quarkus.container-image.name=boat-fuel-tracker
quarkus.container-image.tag=1.0
```

Generate and deploy:
```bash
mvn clean package -Dquarkus.kubernetes.deploy=true
```

---

### 6. OpenShift Deployment

```bash
# Login to OpenShift
oc login

# Create new project
oc new-project boat-fuel-tracker

# Deploy MySQL
oc new-app mysql:8.0 \
  -e MYSQL_USER=boatfuel \
  -e MYSQL_PASSWORD=changeme \
  -e MYSQL_DATABASE=boatfuel

# Build and deploy application from source
oc new-app quay.io/quarkus/ubi-quarkus-native-s2i:22.3-java17~https://github.com/your-repo/boat-fuel-tracker.git \
  --name=boat-fuel-tracker \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://mysql:3306/boatfuel \
  -e QUARKUS_DATASOURCE_USERNAME=boatfuel \
  -e QUARKUS_DATASOURCE_PASSWORD=changeme

# Expose the service
oc expose svc/boat-fuel-tracker

# Get the route
oc get route
```

---

## Environment Configuration

### application.properties Profiles

The application supports different profiles:

**Development** (`%dev`):
```properties
%dev.quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/boatfuel
%dev.quarkus.hibernate-orm.log.sql=true
```

**Production** (`%prod`):
```properties
%prod.quarkus.datasource.jdbc.url=${DATABASE_URL}
%prod.quarkus.hibernate-orm.log.sql=false
```

Activate profile:
```bash
java -jar target/quarkus-app/quarkus-run.jar -Dquarkus.profile=prod
```

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `QUARKUS_DATASOURCE_JDBC_URL` | Database JDBC URL | `jdbc:mysql://localhost:3306/boatfuel` |
| `QUARKUS_DATASOURCE_USERNAME` | Database username | `boatfuel` |
| `QUARKUS_DATASOURCE_PASSWORD` | Database password | `changeme` |
| `QUARKUS_HTTP_PORT` | HTTP port | `8080` |
| `QUARKUS_LOG_LEVEL` | Logging level | `INFO` |

---

## Health Checks

Quarkus provides built-in health endpoints:

```bash
# Liveness probe (is app running?)
curl http://localhost:8080/q/health/live

# Readiness probe (is app ready for traffic?)
curl http://localhost:8080/q/health/ready

# Overall health
curl http://localhost:8080/q/health
```

Add health extension:
```bash
mvn quarkus:add-extension -Dextensions="smallrye-health"
```

---

## Monitoring and Metrics

Add metrics support:

```bash
mvn quarkus:add-extension -Dextensions="smallrye-metrics"
```

Access metrics:
```bash
# Prometheus format
curl http://localhost:8080/q/metrics

# Application metrics
curl http://localhost:8080/q/metrics/application

# JVM metrics
curl http://localhost:8080/q/metrics/base
```

---

## Database Setup

### MySQL

```sql
CREATE DATABASE boatfuel;
CREATE USER 'boatfuel'@'%' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON boatfuel.* TO 'boatfuel'@'%';
FLUSH PRIVILEGES;
```

### PostgreSQL (Alternative)

To use PostgreSQL instead:

1. Update `pom.xml`:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-postgresql</artifactId>
</dependency>
```

2. Update `application.properties`:
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/boatfuel
```

---

## Testing the Deployment

```bash
# Health check
curl http://localhost:8080/q/health

# Get fuel-ups for a user
curl http://localhost:8080/fuelups/user/user123

# Create a new fuel-up
curl -X POST http://localhost:8080/fuelups \
  -H "Content-Type: application/json" \
  -d '{
    "user": {"userId": "user123"},
    "date": "2025-10-08",
    "gallons": 50.5,
    "pricePerGallon": 4.25
  }'

# Get statistics
curl http://localhost:8080/fuelups/user/user123/statistics
```

---

## Performance Tuning

### JVM Options

```bash
java -Xms512m -Xmx1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -jar target/quarkus-app/quarkus-run.jar
```

### Connection Pool Tuning

In `application.properties`:
```properties
quarkus.datasource.jdbc.min-size=5
quarkus.datasource.jdbc.max-size=20
quarkus.datasource.jdbc.acquisition-timeout=10
```

---

## Troubleshooting

### Check application logs
```bash
# JVM mode
tail -f target/quarkus.log

# Docker
docker logs -f boat-fuel-app

# Kubernetes
kubectl logs -f deployment/boat-fuel-tracker
```

### Common Issues

1. **Database connection failed**: Check JDBC URL, credentials, and network access
2. **Port already in use**: Change port with `-Dquarkus.http.port=8081`
3. **OutOfMemoryError**: Increase heap size with `-Xmx`

---

## Production Checklist

- [ ] Configure production database credentials
- [ ] Set `quarkus.hibernate-orm.database.generation=validate` (not `update`)
- [ ] Enable HTTPS/TLS
- [ ] Configure authentication/authorization
- [ ] Set up monitoring and alerting
- [ ] Configure backup strategy
- [ ] Review and tune connection pool settings
- [ ] Set appropriate log levels
- [ ] Configure resource limits in Kubernetes
- [ ] Set up CI/CD pipeline
- [ ] Perform load testing

---

## Additional Resources

- [Quarkus Deployment Guide](https://quarkus.io/guides/deploying-to-kubernetes)
- [Quarkus Container Images](https://quarkus.io/guides/container-image)
- [Quarkus on OpenShift](https://quarkus.io/guides/deploying-to-openshift)
