FROM catthehacker/ubuntu:full-latest

USER root

# Install required dependencies and Docker Engine
RUN apt-get update && \
    apt-get install -y \
        ca-certificates \
        curl \
        gnupg \
        lsb-release \
        iptables \
        uidmap \
        fuse-overlayfs && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
        https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | \
        tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce docker-ce-cli containerd.io && \
    apt-get clean

# Expose Docker API port (optional)
EXPOSE 2375

# Start Docker daemon and keep the container alive
CMD ["sh", "-c", "dockerd & while ! docker info > /dev/null 2>&1; do sleep 1; done && tail -f /dev/null"]
