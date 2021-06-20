#!/bin/bash

# Update
echo "Checking for git updates..."
git pull origin master

# Run the image daemonized (killing the old one first)
echo "Restarting Docker image..."
cd docker-test || exit
sudo rm -rf postgres-data
sudo docker-compose down || exit
sudo docker system prune -f --volumes || exit
sudo docker-compose up --remove-orphans --build -d || exit

echo "Bipper Test server is up."
