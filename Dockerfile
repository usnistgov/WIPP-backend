FROM maven:3.5.4-jdk-8-alpine

EXPOSE 8080

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN mvn clean install

RUN tar -cvf WIPP-backend.tar /usr/src/app/

RUN curl -ulabshare:AP2DxpCyvU2wQpzu4KUh5nGaUTuz2NbQ1c5nbe -T /usr/src/app/WIPP-backend.tar "https://builds.aws.labshare.org/artifactory/labshare/WIPP-backend/"

ENTRYPOINT ["mvn", "spring-boot:run"]
