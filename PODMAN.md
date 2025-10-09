# Podman Deployment Guide

This guide covers deploying the Boat Fuel Tracker application using Podman and Podman Compose.

## Prerequisites

### Install Podman

**macOS:**
```bash
brew install podman podman-compose
podman machine init
podman machine start
```

**RHEL/Fedora:**
```bash
sudo dnf install podman podman-compose
```

**Ubuntu:**
```bash
sudo apt-get install podman podman-compose
```

### Verify Installation

```bash
podman --version
podman-compose --version
```

---

## Quick Start with Podman Compose

### Option 1: Full Stack (Keycloak + MySQL + App)

```bash
# Start all services
podman-compose up -d

# View logs
podman-compose logs -f

# Stop all services
podman-compose down

# Stop and remove volumes
podman-compose down -v
```

### Option 2: Individual Services

```bash
# Start just Keycloak
podman-compose up -d keycloak

# Start MySQL
podman-compose up -d mysql

# Start application
podman-compose up -d app
```

---

## Running with Podman (Without Compose)

### 1. Create Pod

```bash
# Create a pod (similar to docker-compose network)
podman pod create --name boat-fuel-pod \
  -p 8080:8080 \
  -p 8180:8180 \
  -p 3306:3306
```

### 2. Run Keycloak

```bash
podman run -d \
  --name boat-fuel-keycloak \
  --pod boat-fuel-pod \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -e KC_HTTP_PORT=8180 \
  quay.io/keycloak/keycloak:23.0 start-dev
```

### 3. Run MySQL

```bash
podman run -d \
  --name boat-fuel-mysql \
  --pod boat-fuel-pod \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=boatfuel \
  -e MYSQL_USER=boatfuel \
  -e MYSQL_PASSWORD=changeme \
  -v mysql-data:/var/lib/mysql \
  docker.io/library/mysql:8.0
```

### 4. Build Application Image

```bash
# Build the application
mvn clean package

# Build Podman image
podman build -f Dockerfile.jvm -t boat-fuel-tracker:latest .
```

### 5. Run Application

```bash
podman run -d \
  --name boat-fuel-app \
  --pod boat-fuel-pod \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://localhost:3306/boatfuel \
  -e QUARKUS_DATASOURCE_USERNAME=boatfuel \
  -e QUARKUS_DATASOURCE_PASSWORD=changeme \
  -e QUARKUS_OIDC_AUTH_SERVER_URL=http://localhost:8180/realms/boat-fuel-tracker \
  boat-fuel-tracker:latest
```

---

## Systemd Integration (Linux)

Podman can generate systemd unit files for automatic startup.

### Generate Systemd Files

```bash
# For a pod
podman generate systemd --new --files --name boat-fuel-pod

# For individual containers
podman generate systemd --new --files --name boat-fuel-keycloak
podman generate systemd --new --files --name boat-fuel-mysql
podman generate systemd --new --files --name boat-fuel-app
```

### Enable Services

```bash
# Copy to systemd directory
sudo cp *.service /etc/systemd/system/

# Reload systemd
sudo systemctl daemon-reload

# Enable and start
sudo systemctl enable --now pod-boat-fuel-pod.service
```

### Manage Services

```bash
# Status
sudo systemctl status pod-boat-fuel-pod

# Start
sudo systemctl start pod-boat-fuel-pod

# Stop
sudo systemctl stop pod-boat-fuel-pod

# Restart
sudo systemctl restart pod-fuel-pod

# View logs
journalctl -u pod-boat-fuel-pod -f
```

---

## Rootless Podman (Recommended)

Podman runs rootless by default - no sudo required!

```bash
# All commands work without sudo
podman-compose up -d
podman ps
podman logs boat-fuel-app
```

### User Systemd Services (Rootless)

```bash
# Generate rootless systemd files
podman generate systemd --new --files --name boat-fuel-pod

# Copy to user systemd directory
mkdir -p ~/.config/systemd/user
mv *.service ~/.config/systemd/user/

# Reload user systemd
systemctl --user daemon-reload

# Enable and start
systemctl --user enable --now pod-boat-fuel-pod.service

# Enable lingering (services survive logout)
loginctl enable-linger $USER
```

---

## Building Native Image with Podman

```bash
# Build native executable in container
podman run --rm \
  -v $(pwd):/project:z \
  -w /project \
  quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-17 \
  ./mvnw package -Pnative -DskipTests

# Build native container image
podman build -f Dockerfile.native -t boat-fuel-tracker:native .

# Run native container
podman run -d \
  --name boat-fuel-app \
  -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://localhost:3306/boatfuel \
  boat-fuel-tracker:native
```

---

## Podman vs Docker Commands

Most Docker commands work with Podman by just replacing `docker` with `podman`:

| Docker | Podman |
|--------|--------|
| `docker run` | `podman run` |
| `docker ps` | `podman ps` |
| `docker images` | `podman images` |
| `docker build` | `podman build` |
| `docker logs` | `podman logs` |
| `docker-compose up` | `podman-compose up` |

### Alias Docker to Podman (Optional)

```bash
# Add to ~/.bashrc or ~/.zshrc
alias docker=podman
alias docker-compose=podman-compose
```

---

## OpenShift/Kubernetes with Podman

### Generate Kubernetes YAML

```bash
# Generate Kubernetes manifests from pod
podman generate kube boat-fuel-pod > boat-fuel-k8s.yaml

# Or from compose
podman-compose -f podman-compose.yml up --build
podman generate kube boat-fuel-pod > boat-fuel-k8s.yaml
```

### Deploy to OpenShift

```bash
# Login to OpenShift
oc login

# Create project
oc new-project boat-fuel-tracker

# Deploy
oc apply -f boat-fuel-k8s.yaml

# Expose route
oc expose svc/boat-fuel-app
```

---

## Managing Containers

### List Running Containers

```bash
podman ps
```

### View Logs

```bash
# Specific container
podman logs -f boat-fuel-app

# All containers in pod
podman pod logs -f boat-fuel-pod
```

### Execute Commands

```bash
# Access container shell
podman exec -it boat-fuel-app /bin/bash

# Run single command
podman exec boat-fuel-app curl http://localhost:8080/q/health
```

### Stop/Remove

```bash
# Stop pod and all containers
podman pod stop boat-fuel-pod

# Remove pod and all containers
podman pod rm -f boat-fuel-pod

# Remove all containers
podman rm -f boat-fuel-keycloak boat-fuel-mysql boat-fuel-app

# Remove images
podman rmi boat-fuel-tracker:latest
```

---

## Volume Management

```bash
# List volumes
podman volume ls

# Inspect volume
podman volume inspect mysql-data

# Remove volume
podman volume rm mysql-data

# Backup volume
podman run --rm \
  -v mysql-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/mysql-backup.tar.gz -C /data .

# Restore volume
podman run --rm \
  -v mysql-data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/mysql-backup.tar.gz -C /data
```

---

## Troubleshooting

### Port Already in Use

```bash
# Find process using port
lsof -i :8080

# Or change port mapping
podman run -p 8081:8080 ...
```

### Container Won't Start

```bash
# Check logs
podman logs boat-fuel-app

# Inspect container
podman inspect boat-fuel-app

# Check pod status
podman pod ps
```

### Permission Issues (SELinux)

```bash
# Add :z or :Z to volume mounts
podman run -v $(pwd):/app:z ...

# Disable SELinux temporarily (not recommended for production)
sudo setenforce 0
```

### Podman Machine Issues (macOS/Windows)

```bash
# Restart machine
podman machine stop
podman machine start

# Recreate machine
podman machine rm
podman machine init
podman machine start

# Check machine status
podman machine ls
podman machine inspect
```

---

## Performance Optimization

### Use Podman 4+ with Netavark

```bash
# Check network backend
podman info | grep -i network

# If using CNI, migrate to Netavark
podman system reset --force
# Podman 4+ will use Netavark by default
```

### Rootless Performance

```bash
# Increase ulimits for rootless
echo "$(whoami):100000:65536" | sudo tee -a /etc/subuid
echo "$(whoami):100000:65536" | sudo tee -a /etc/subgid

# Reset Podman to apply
podman system reset
```

---

## Security Features

### Run as Non-Root (Default in Podman)

```bash
# No sudo needed!
podman run --user 1000:1000 ...
```

### Security Scanning

```bash
# Scan image for vulnerabilities
podman run --rm quay.io/skopeo/stable:latest \
  inspect docker://boat-fuel-tracker:latest

# Or use Trivy
podman run --rm \
  -v /var/run/podman/podman.sock:/var/run/docker.sock:ro \
  aquasec/trivy image boat-fuel-tracker:latest
```

---

## Additional Resources

- [Podman Documentation](https://docs.podman.io/)
- [Podman Compose](https://github.com/containers/podman-compose)
- [Podman vs Docker](https://docs.podman.io/en/latest/Tutorials/podman-for-docker-users.html)
- [Rootless Containers](https://github.com/containers/podman/blob/main/docs/tutorials/rootless_tutorial.md)

---

## Quick Reference

```bash
# Start everything
podman-compose up -d

# View all containers
podman ps -a

# View logs
podman-compose logs -f app

# Stop everything
podman-compose down

# Rebuild and restart
podman-compose up -d --build

# Access Keycloak admin
open http://localhost:8180

# Access application
open http://localhost:8080

# Clean everything
podman-compose down -v
podman system prune -a --volumes
```
