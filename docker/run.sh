#!/bin/bash

# Update
echo "Checking for git updates..."
git pull origin master

# Rebuild the server JAR
echo "Creating new client JAR from source..."
#mvn clean package assembly:single || exit

cd docker || exit

# Pull and update the client
git clone https://github.com/filipedeluna/bipper-client || (cd bipper-client && git config pull.rebase false && git pull)

# Run the image daemonized (killing the old one first)
echo "Restarting Docker image..."
sudo docker-compose down || exit
sudo docker system prune -f --volumes || exit
sudo docker-compose up --remove-orphans --build -d || exit

echo "Bipper Production server is up."
