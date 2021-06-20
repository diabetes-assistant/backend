# Diabetes Assistant Backend
[![GitHub license](https://img.shields.io/github/license/diabetes-assistant/backend)](https://github.com/diabetes-assistant/backend/blob/main/LICENSE)
[![CircleCI](https://img.shields.io/circleci/build/github/diabetes-assistant/backend)](https://app.circleci.com/pipelines/github/diabetes-assistant/backend)

A _Spring Webflux_ backend for the [Diabetes Assistant App](https://github.com/diabetes-assistant/diabetes-assistant-app)
built with gradle.

## Prerequisites
* JDK 16

## Run locally
This will run the server locally by default on port 8080
```bash
./gradlew bootRun
```

## Tests
To run all tests and checks
```bash
./gradlew check
```

### Unit tests
```bash
./gradlew test
```

### Integration tests
```bash
./gradlew integrationTest
```

## Linter/Autoformat
```bash
./gradlew spotlessApply
```

## Static code analysis
```bash
./gradlew spotbugsMain spotbugsTest spotbugsIntTest
```