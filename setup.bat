@echo off
REM Setup script for Shodh-a-Code platform (Windows)

echo.
echo Setting up Shodh-a-Code Contest Platform...
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not installed. Please install Docker Desktop first.
    exit /b 1
)

docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Compose is not installed. Please install Docker Compose first.
    exit /b 1
)

echo Docker and Docker Compose are installed
echo.

REM Build judge container image
echo Building judge container image...
docker build -t shodh-judge:latest ./docker/judge

if %errorlevel% neq 0 (
    echo Failed to build judge container
    exit /b 1
)

echo Judge container built successfully
echo.

REM Start all services
echo Starting all services with Docker Compose...
docker-compose up --build -d

if %errorlevel% neq 0 (
    echo Failed to start services
    exit /b 1
)

echo.
echo All services started successfully!
echo.
echo Application URLs:
echo    Frontend: http://localhost:3000
echo    Backend:  http://localhost:8080
echo    H2 Console: http://localhost:8080/h2-console
echo.
echo To view logs: docker-compose logs -f
echo To stop: docker-compose down
echo.

