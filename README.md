# sampleServer

A small Spring Boot 4 REST API for pets, built contract-first from `src/main/resources/PetStoreApi.yml`.
The OpenAPI generator creates the `PetsApi` interface and `Pet` model at build time; `PetStoreController` implements them.

## Run

```bash
mvn spring-boot:run                                         # http://localhost:8085
mvn spring-boot:run -Dspring-boot.run.profiles=dev          # also enables the H2 console at /h2-console
```

Requires Java 21.

## Endpoints

| Method | Path            | Result                         |
|--------|-----------------|--------------------------------|
| GET    | `/pets`         | all pets                       |
| GET    | `/pets/{id}`    | one pet, or 404                |
| POST   | `/pets`         | 201 with the created pet       |
| PUT    | `/pets/{id}`    | the updated pet, or 404        |
| DELETE | `/pets/{id}`    | 204, or 404                    |

Errors are returned as RFC 9457 problem details.

## Test

```bash
mvn verify
```

`sampleClient` keeps a copy of the same spec. Change both together.
