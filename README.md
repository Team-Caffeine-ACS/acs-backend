## Container Image

Backend image is stored in GitHub Container Registry (GHCR):
ghcr.io/team-caffeine-acs/acs-backend

### Pull image
docker pull ghcr.io/team-caffeine-acs/acs-backend:latest

### Run container
docker run -p 8080:8080 ghcr.io/team-caffeine-acs/acs-backend:latest

### Build container
docker build -t ghcr.io/team-caffeine-acs/acs-backend:latest .

### Push image
docker login ghcr.io
docker push ghcr.io/team-caffeine-acs/acs-backend:<tag>