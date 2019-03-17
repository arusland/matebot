FROM openjdk:8-jre-alpine
WORKDIR app
COPY matebot.jar matebot.jar
COPY matebotctl.jar matebotctl.jar
COPY application.properties application.properties
RUN mkdir -p /app/logs
RUN mkdir -p /app/data
CMD java -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -jar matebot.jar
