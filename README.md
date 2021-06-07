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

1. Delete the `.git` directory to remove the git history

2. Run `./gradlew`

## Using ThingArtifacts

You are provided with an implementation of a CArtAgO artifact that can retrieve, interpret, and use a W3C WoT TD to interact with the described Thing. A Jason agent can create a `ThingArtifact` as follows:

```
makeArtifact("forkliftRobot", "tools.ThingArtifact", [Url, true], ArtId);
```

The `ThingArtifact` takes two initialization parameters:
- a URL that dereferences to a W3C WoT TD
- an optional `dryRun` flag: when set to `true`, all HTTP requests composed by the artifact are printed to the JaCaMo console instead of being executed (default value is `false`).

The `ThingArtifact` can use an [APIKeySecurityScheme](https://www.w3.org/TR/wot-thing-description/#apikeysecurityscheme) for authenticating HTTP requests. The API token can be set via the `setAPIKey` operation:

```
setAPIKey(Token)[artifact_name("forkliftRobot")];
```

The `ThingArtifact` provides agents with 3 additional CArtAgO operations: `readProperty`, `writeProperty`, and `invokeAction`, which correspond to operation types defined by the W3C WoT TD recommendation.

The general invocation style of `writeProperty` and `invokeAction` is as follows (see also the Javadoc comments):

```
writeProperty|invokeAction ( <semantic type of affordance>, [ <optional: list of semantic types for object properties> ], [ <list of Jason terms, can be arbitrarily nested> ] )
```

Example for writing a TD property of a `BooleanSchema` type:

```
writeProperty("http://example.org/Status", [true])[artifact_name("forkliftRobot")];
```

Example for invoking a TD action with an `ArraySchema` payload:

```
invokeAction("http://example.org/MoveTo", [30, 60, 70])[artifact_name("forkliftRobot")];
```

Example for invoking a TD action with an `ObjectSchema` payload:

```
invokeAction("http://example.org/CarryFromTo",
    ["http://example.org/SourcePosition", "http://example.org/TargetPosition"],
    [[30, 50, 70], [30, 60, 70]]
  )[artifact_name("forkliftRobot")];
```

A TD property can be read in a similar manner, where `PositionValue` is a CArtAgO operation feedback parameter:

```
readProperty("http://example.org/Position", PositionValue)[artifact_name("forkliftRobot")];
```

You can find more details about CArtAgO and the Jason to/from CArtAgO data binding [here](http://cartago.sourceforge.net/?page_id=47). You can find additional examples for using the `ThingArtifact` in a Jason program in `src/agt/wot_agent.asl`.

## Mocking your HTTP requests

When developing your application, it might be useful to mock your HTTP requests. One simple solution is to use [MockServer](https://www.mock-server.com/):

1. Add your expected HTTP responses in `mockserver/mockserver.json`. You can find the format of an expectation in the [MockServer OpenAPI specification](https://app.swaggerhub.com/apis/jamesdbloom/mock-server-openapi/5.10.x#/Expectation).

2. Run MockServer with [Docker](https://www.docker.com/). To use the expectation initialization file created in the previous step, you will have to use a bind mount and to set an environment variable like so:

```
docker run -v "$(pwd)"/mockserver/mockserver.json:/tmp/mockserver/mockserver.json \
-e MOCKSERVER_INITIALIZATION_JSON_PATH=/tmp/mockserver/mockserver.json \
-d --rm --name mockserver -p 1080:1080 mockserver/mockserver
```

The above command will run the Docker container in the background and will print the container ID. To stop the container: `docker stop CONTAINER_ID`
