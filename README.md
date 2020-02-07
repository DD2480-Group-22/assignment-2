[![Build Status](https://travis-ci.com/DD2480-Group-22/assignment-2.svg?branch=master)](https://travis-ci.com/DD2480-Group-22/assignment-2)
[![codecov](https://codecov.io/gh/DD2480-Group-22/assignment-2/branch/master/graph/badge.svg)](https://codecov.io/gh/DD2480-Group-22/assignment-2)
[![Docker Automated build](https://img.shields.io/docker/automated/nilsx/dd480-assignment-2)](https://hub.docker.com/repository/docker/nilsx/dd480-assignment-2)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6e26b85db281421ebc7665e67cd6c55e)](https://www.codacy.com/gh/DD2480-Group-22/assignment-2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=DD2480-Group-22/assignment-2&amp;utm_campaign=Badge_Grade)

# Assignment 2
This is a basic Continuous integration (CI-server) written in Java that can test Maven 3.x projects. The server can be 
configured to listen for webhooks from Github, to automatically test a repository each time a new push event occurs.

The CI-server is the second assignment in the course DD2480 Software Engineering Fundamentals at KTH, the code is 
written by group 22.

## Statement of Contributions
  * Nils Streijffert: Setup tests of projects, setup server, pull repository from Github, upload to S3 bucket, Docker and deployment.

## Configuration
### Requirements for development
  * Java 8
  * Maven 3.6.3
  * Amazon Web Service (AWS) account and S3 bucket
  * Github account

### Alternative
The project includes a Docker file which is capable of both compiling and running the server.

### Setup
#### Environment variables
Environment variables to have to be available to the server.
   * **GITHUB_TOKEN:** Github access token
   * **AWS_SECRET_KEY:** AWS secret key
   * **AWS_ACCESS_KEY_ID:** AWS access key id
   * **MAVEN_HOME:** Path to Maven install folder
   * **PORT:** The port the server should listen on (defaults to 8080 if not set)

#### Command line arguments
   1. AWS bucket name
   2. AWS bucket region

#### Running the project
##### Locally
   1. `mvn clean package`
   2. `java -jar assignment-2-1.0-SNAPSHOT-launcher.jar` (in the same folder as the jar-file)

##### With Docker
Build and run the container manually from the command line.
   1. `docker build -t ci-server .`
   2. `docker run -p 8080:8080 -e GITHUB_TOKEN -e AWS_SECRET_KEY -e AWS_ACCESS_KEY_ID -e MAVEN_HOME ci-server`
   
##### Docker-compose
Automatically build and run the container with docker-compose. 
   1. `docker-compose up` (add the flag -d to run the container headless)
   
##### Get logs from container
`docker -f ci-server`

## Deployment
The code on the `production` branch on Github is automatically downloaded by DockerHub and built. The new Docker image 
is then automatically deployed on a Digital Ocean droplet running Rancher os. To view the logs of the production
CI-server click [here](http://104.248.32.226:9999/container/cf63f735d839).

## Limitations of the server
The server can only test individual commits that are pushed to a repository, it can't test pull request. 
This is important since Github's API views pull requests as push events and forwards them to the server. The POST 
requests sent for pull requests don't contain a head commit id, they are therefor disregarded by the server (the server
will return error code 400).

## Commit Status
  * We update the commit status' using the Git Status API with the 4 possible values (`success`, `failure`, `error` and `waiting`) accordingly to the situation.
  * A test can be made by modifying any file in the `assessment` branch and making a commit, then, the corresponding status should appear under the "checks" section first as `waiting` and then updated with the proper outcome result.

## Retrieving build logs
The result of the builds are stored in an AWS S3 bucket, they can be retrieved by clicking the status indicator on a commit on Github
or by sending a GET request to the server to the endpoint: `<server address>/reports/<buildId>`. There is also a page with all available build reports
listed `<server address>/reports`.
