    
FROM maven:3.5.4-jdk-8-alpine

EXPOSE 8080

COPY . /usr/src/app

WORKDIR /usr/src/app

RUN mvn clean install

CMD ["mvn", "spring-boot:run"]
