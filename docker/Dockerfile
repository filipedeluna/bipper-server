FROM adoptopenjdk/openjdk11:alpine-jre

LABEL vendor="Bipper" maintainer="filipe@deluna.pt"

# Install required dependencies
RUN apk update && apk upgrade

# Copy the client jar and resources
ADD target/bipper_server.jar /bipper_server.jar
RUN chmod 755 /bipper_server.jar
ADD res /res
RUN chmod 744 -R /res

# Execute the jar after a delay to allow db to start
CMD sleep 30 && java -jar bipper_server.jar res/server.properties >> log/server-log.txt 2>&1


