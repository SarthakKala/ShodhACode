# Docker Deployment Guide

## Quick Start

### 1. Build Judge Container

The judge container is used for secure code execution. Build it first:

```bash
docker build -t shodh-judge:latest ./docker/judge
```

### 2. Start All Services

```bash
docker-compose up --build
```

Or use the provided setup scripts:

**Linux/Mac:**

```bash
chmod +x setup.sh
./setup.sh
```

**Windows:**

```cmd
setup.bat
```

### 3. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                     Docker Host                          │
│                                                          │
│  ┌────────────┐    ┌──────────────┐    ┌────────────┐  │
│  │  Frontend  │───▶│   Backend    │───▶│ H2 Database│  │
│  │ (Nginx:80) │    │(Spring:8080) │    │ (In-Memory)│  │
│  └────────────┘    └──────────────┘    └────────────┘  │
│                           │                              │
│                           ▼                              │
│                    ┌──────────────┐                      │
│                    │Docker Socket │                      │
│                    └──────────────┘                      │
│                           │                              │
│                           ▼                              │
│           ┌──────────────────────────────┐              │
│           │ Ephemeral Judge Containers   │              │
│           │ (shodh-judge:latest)         │              │
│           │ - Java, Python, C++, JS      │              │
│           │ - Network isolated           │              │
│           │ - Resource limited           │              │
│           │ - Auto-removed               │              │
│           └──────────────────────────────┘              │
└─────────────────────────────────────────────────────────┘
```

---

## Container Details

### Frontend Container

- **Base**: nginx:alpine
- **Port**: 80 (mapped to 3000 on host)
- **Purpose**: Serves React SPA
- **Build**: Multi-stage (Node 18 for build, Nginx for serving)

### Backend Container

- **Base**: eclipse-temurin:21-jre
- **Port**: 8080
- **Purpose**: REST API and code judging orchestration
- **Special**: Mounts Docker socket to spawn judge containers

### Judge Container

- **Base**: ubuntu:22.04
- **Languages**: Java 17, Python 3, GCC/G++, Node.js 18
- **User**: coderunner (non-root, UID 1000)
- **Usage**: Ephemeral containers spawned per submission

---

## Security Considerations

### Judge Container Security

1. **Network Isolation**: `--network none` prevents internet access
2. **Non-root Execution**: Code runs as `coderunner` user
3. **Resource Limits**:
   - Memory: Configurable per problem (default 256MB)
   - CPU: Limited to 1 core
   - Time: Process killed after timeout
4. **Auto-cleanup**: Containers removed after execution (`--rm`)
5. **Filesystem Isolation**: Only /workspace mounted (read-only for user code)

### Backend Security

- Docker socket access required for spawning containers
- In production, consider:
  - Running Docker socket proxy with limited permissions
  - Using Kubernetes for better isolation
  - Implementing rate limiting on submissions

---

## Docker Commands Reference

### Building Images

```bash
# Build judge image
docker build -t shodh-judge:latest ./docker/judge

# Build backend image
docker build -t shodh-backend:latest ./backend

# Build frontend image
docker build -t shodh-frontend:latest ./frontend
```

### Managing Services

```bash
# Start all services (detached)
docker-compose up -d

# Start with build
docker-compose up --build

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Restart a service
docker-compose restart backend
```

### Debugging

```bash
# Check running containers
docker ps

# Check all containers (including stopped)
docker ps -a

# Execute command in running container
docker exec -it shodh-backend bash

# View backend logs
docker logs shodh-backend

# Inspect container
docker inspect shodh-backend
```

### Cleanup

```bash
# Stop and remove all containers
docker-compose down

# Remove with volumes
docker-compose down -v

# Remove images
docker rmi shodh-backend shodh-frontend shodh-judge

# Clean up judge containers (if any stuck)
docker ps -a | grep judge_ | awk '{print $1}' | xargs docker rm -f

# Clean up dangling images
docker image prune -f
```

---

## Troubleshooting

### Issue: Judge container build fails

**Solution**: Ensure you have internet connection and Docker can pull base images:

```bash
docker pull ubuntu:22.04
```

### Issue: Backend can't connect to Docker

**Solution**: Verify Docker socket is mounted and accessible:

```bash
# Check socket exists
ls -l /var/run/docker.sock

# On Windows, ensure Docker Desktop is running
```

### Issue: Frontend can't connect to backend

**Solution**: Update API URL if using different ports. Edit `frontend/src/services/api.js`:

```javascript
const API_BASE_URL = "http://localhost:8080/api";
```

### Issue: Port conflicts

**Solution**: Change ports in `docker-compose.yml`:

```yaml
ports:
  - "8081:8080" # Change 8080 to 8081
```

---

## Performance Optimization

### For Production

1. **Use PostgreSQL** instead of H2:

   ```yaml
   postgres:
     image: postgres:15
     environment:
       POSTGRES_DB: shodh
       POSTGRES_USER: shodh
       POSTGRES_PASSWORD: password
   ```

2. **Enable Redis** for caching leaderboards

3. **Use Docker secrets** for sensitive data

4. **Enable log aggregation** (ELK stack)

5. **Add monitoring** (Prometheus + Grafana)

---

## Development vs Production

### Development Mode

- H2 in-memory database
- Hot reload enabled
- Debug logging
- CORS enabled for localhost

### Production Mode

- PostgreSQL database
- Optimized builds
- Error-only logging
- CORS restricted to specific domains
- TLS/SSL enabled
- Rate limiting on APIs
