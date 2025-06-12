# api-banking-assignment

Objective: Build a small application using Quarkus Reactive to create REST APIs for
performing CRUD (Create, Read, Update, Delete) operations on a Product Management
system.

## Database Setup Using Docker

We will be using postgres container for our database. You can run the following command to start a postgres container:
```shell script
docker run --name pg-quarkus -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=productsdb -p 5432:5432 -d postgres

```
## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw clean install
./mvnw quarkus:dev

```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

## Testing the Application using Postman

You can test api using Postman by importing the postman collection


