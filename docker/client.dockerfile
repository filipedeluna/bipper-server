FROM python:alpine

LABEL vendor="Bipper" maintainer="filipe@deluna.pt"

ARG FLASK_PORT
ENV FLASK_PORT ${FLASK_PORT}
ENV SERVER_ADDRESS ${SERVER_ADDRESS}
ENV SERVER_PORT ${SERVER_PORT}

# Install required dependencies
RUN apk update && apk upgrade
RUN apk add --update nodejs npm

# Requirements
RUN pip install Flask
RUN pip install requests
RUN pip install python-dotenv
RUN pip install datetime
RUN pip install bleach

# Copy the client folder and install dependencies
ADD docker/bipper-client /bipper-client
RUN chmod 755 -R /bipper-client
RUN (cd bipper-client/static && npm install)

# Execute the flask app
CMD cd /bipper-client && flask run -p ${FLASK_PORT} >> /log/client-log.txt 2>&1


