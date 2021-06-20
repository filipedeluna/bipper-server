#!/bin/bash

# Update
echo "Checking for git updates..."
git pull origin master

# Rebuild the JAR for the client
echo "Creating new client JAR from source..."
mvn clean package assembly:single || exit

# Run the image daemonized (killing the old one first)
echo "Restarting Docker image..."
cd docker || exit
sudo docker-compose down || exit
sudo docker system prune -f --volumes || exit
sudo docker-compose up --remove-orphans --build -d || exit

echo "Bipper Production server is up."
