FROM nilsx/alpine-openjdk8-maven:latest

WORKDIR /app/src

RUN mkdir reports

RUN mkdir reports_html

RUN mkdir git

COPY src build/src

COPY pom.xml build/

RUN mvn -f build/ clean package

RUN mv build/target/assignment-2-1.0-SNAPSHOT-launcher.jar ./ci-server.jar

RUN rm -rf build

CMD java -jar ci-server.jar dd2480-assignment-2 eu-north-1
