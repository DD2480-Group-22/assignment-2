FROM adoptopenjdk/openjdk8:alpine as builder

RUN apk add --update ca-certificates && rm -rf /var/cache/apk/* && \
  find /usr/share/ca-certificates/mozilla/ -name "*.crt" -exec keytool -import -trustcacerts \
  -keystore /opt/java/openjdk/jre/lib/security/cacerts -storepass changeit -noprompt \
  -file {} -alias {} \; && \
  keytool -list -keystore /opt/java/openjdk/jre/lib/security/cacerts --storepass changeit

ENV MAVEN_VERSION 3.6.3
ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH

RUN wget http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
  mv apache-maven-$MAVEN_VERSION /usr/lib/mvn

WORKDIR /app/src

RUN mkdir reports

RUN mkdir git

COPY src build/src

COPY pom.xml build/

RUN mvn -f build/ clean package

RUN mv build/target/assignment-2-1.0-SNAPSHOT-launcher.jar ./ci-server.jar

RUN rm -rf build

CMD java -jar ci-server.jar dd2480-assignment-2 eu-north-1