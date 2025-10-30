# Multi-language code execution environment
FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install multiple language runtimes
RUN apt-get update && apt-get install -y \
    # Java
    openjdk-17-jdk \
    # Python
    python3 \
    python3-pip \
    # C++ compiler
    g++ \
    build-essential \
    # JavaScript (Node.js)
    curl \
    && curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user for code execution (security)
RUN useradd -m -u 1000 coderunner

# Set working directory
WORKDIR /workspace

# Copy will be done at runtime via volume mount
# Command will be overridden at runtime

# Default user for execution
USER coderunner

CMD ["/bin/bash"]

