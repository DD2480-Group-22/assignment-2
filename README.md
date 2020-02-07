[![Build Status](https://travis-ci.com/DD2480-Group-22/assignment-2.svg?branch=master)](https://travis-ci.com/DD2480-Group-22/assignment-2)
[![codecov](https://codecov.io/gh/DD2480-Group-22/assignment-2/branch/master/graph/badge.svg)](https://codecov.io/gh/DD2480-Group-22/assignment-2)
[![Docker Automated build](https://img.shields.io/docker/automated/nilsx/dd480-assignment-2)](https://hub.docker.com/repository/docker/nilsx/dd480-assignment-2)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6e26b85db281421ebc7665e67cd6c55e)](https://www.codacy.com/gh/DD2480-Group-22/assignment-2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=DD2480-Group-22/assignment-2&amp;utm_campaign=Badge_Grade)

# Assignment 2: Continuous Integration


## Group members:
* Erik Svensson
* Felipe Ignacio Vicencio Neumann
* Nils Streijffert
* Pablo Javier Aravena Núñez


### Statement of contributions
##### Erik Svensson
* 

##### Felipe Ignacio Vicencio Neumann
* 

##### Nils Streijffert
* 

#### Pablo Javier Aravena Núñez
* 


## Running the program
### Requirements
* Java JDK 8
* Apache Maven 3.6.3


## How to generate documentation for the source code
1. Compile the project: `mvn compile`
2. Generate documentation: `mvn javadoc:javadoc`
3. To view the documentation open the file `target/site/apidocs/index.html` in a web browser.


## Statement of Contributions
  * `...`

## Configuration
### Requirements
  * This CI Server requires a valid `pom.xml` file on the repo to be used, as we use Maven to build and run tests on projects.

### Environment variables
    * GITHUB_TOKEN: Github access token

## Commit Status
  * We update the commit status' using the Git Status API with the 4 possible values (`success`, `failure`, `error` and `waiting`) accordingly to the situation.
  * A test can be made by modifying any file in the `assessment` branch and making a commit, then, the corresponding status should appear under the "checks" section first as `waiting` and then updated with the proper outcome result.

## Instructions
  * To build the project and also run our tests execute the command `mvn clean install`.

## URL to our CI
  * `...`
