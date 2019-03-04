    
FROM openjdk:8-jdk-alpine

EXPOSE 8080

ENV MAVEN_VERSION 3.5.4
ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH

RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  mv apache-maven-$MAVEN_VERSION /usr/lib/mvn

RUN mkdir -p /usr/src/app

COPY data /usr/src/app
COPY wipp-backend-application /usr/src/app
COPY wipp-backend-argo-workflows /usr/src/app
COPY wipp-backend-core /usr/src/app
COPY wipp-backend-images /usr/src/app
COPY pom.xml /usr/src/app 

WORKDIR /usr/src/app/wipp-backend-application

ENTRYPOINT ["mvn","spring-boot:run"]
