FROM maven:3-jdk-8 AS builder

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY pom.xml /usr/src/app

COPY src /usr/src/app

RUN mvn clean package

FROM openjdk:8-jdk-alpine AS runner

RUN apk add --update ca-certificates && rm -rf /var/cache/apk/* && \
  find /usr/share/ca-certificates/mozilla/ -name "*.crt" -exec keytool -import -trustcacerts \
  -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -storepass changeit -noprompt \
  -file {} -alias {} \; && \
  keytool -list -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts --storepass changeit

ENV MAVEN_VERSION 3.5.4
ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH

RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  mv apache-maven-$MAVEN_VERSION /usr/lib/mvn

COPY --from=builder /usr/src/app/target/ci-server.jar /usr/app/

WORKDIR /usr/app

RUN mkdir -p /usr/app/reports

CMD java -cp ci-server.jar App dd2480-assignment-2 eu-north-1 8080