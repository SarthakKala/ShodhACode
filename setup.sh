#!/bin/bash
# Setup script for Shodh-a-Code platform

echo "🚀 Setting up Shodh-a-Code Contest Platform..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"

# Build judge container image
echo "📦 Building judge container image..."
docker build -t shodh-judge:latest ./docker/judge

if [ $? -ne 0 ]; then
    echo "❌ Failed to build judge container"
    exit 1
fi

echo "✅ Judge container built successfully"

# Start all services
echo "🔧 Starting all services with Docker Compose..."
docker-compose up --build -d

if [ $? -ne 0 ]; then
    echo "❌ Failed to start services"
    exit 1
fi

echo "✅ All services started successfully!"
echo ""
echo "🌐 Application URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend:  http://localhost:8080"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""
echo "📝 To view logs: docker-compose logs -f"
echo "🛑 To stop: docker-compose down"

