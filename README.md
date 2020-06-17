# AI4Industry: JaCaMo starter project

This project is a template to start your own [JaCaMo](https://github.com/jacamo-lang/jacamo) project using Gradle. 

## Prerequisites

- JDK 8+

## Getting started

Create your project with:

```
git clone --recursive git@github.com:Interactions-HSG/ai4industry-jacamo.git PROJECT_NAME
```

Replace `PROJECT_NAME` with the name of your project.

## Start to hack

1. Delete the `.git` directory

2. Run `./gradlew`

## Mocking your HTTP requests

When developing your application, it might be useful to mock your HTTP requests. One simple solution is to use [MockServer](https://www.mock-server.com/):

1. Add your expected HTTP responses in `mockserver/mockserver.json`. You can find the format of an expectation in the [MockServer OpenAPI specification](https://app.swaggerhub.com/apis/jamesdbloom/mock-server-openapi/5.10.x#/Expectation).

2. Run MockServer with [Docker](https://www.docker.com/). To use the expectation initialization file created in the previous step, you will have to use a bind mount and to set an environment variable like so:

```
docker run -v "$(pwd)"/mockserver/mockserver.json:/tmp/mockserver/mockserver.json \
-e MOCKSERVER_INITIALIZATION_JSON_PATH=/tmp/mockserver/mockserver.json -d --rm --name mockserver \
-p 1080:1080 mockserver/mockserver
```

The above command will run the Docker container in the background and will print the container ID. To stop the container: `docker stop CONTAINER_ID` 