# JDK
Learn Backend Java Path

A single Maven project for working through the Java backend path. Each topic lives in its
own package under `src/main/java/com/jdk/`, with a runnable example (`main` method) and a
matching JUnit 5 test under `src/test/java/com/jdk/`.

## Requirements

- JDK 21 (Eclipse Temurin 21 recommended)
- Maven is optional: the Maven Wrapper (`mvnw` / `mvnw.cmd`) downloads the right version for you.

## Commands

```bash
# compile and run all tests
./mvnw test          # Windows: .\mvnw.cmd test

# run the tests for one topic
./mvnw test -Dtest="com.jdk.collections.*Test"

# run one example
./mvnw -q compile
java -cp target/classes com.jdk.functional.Orders
```

In IntelliJ IDEA or VS Code (with the Java Extension Pack) open the folder and it is picked
up as a Maven project; run any `main` method or test directly from the editor.

## Project layout

```
src/main/java/com/jdk/
  basics/        01 types, control flow, arrays, strings, switch expressions
  oop/           02 classes, interfaces, records, sealed types, pattern matching
  exceptions/    03 checked vs unchecked, custom exceptions, try/catch/finally
  collections/   04 List, Set, Map, Comparator
  generics/      05 generic types and methods, bounds, wildcards
  functional/    06 lambdas, method references, Streams, Optional
  io/            07 java.nio.file, try-with-resources
  concurrency/   08 ExecutorService, CompletableFuture, atomics, virtual threads
src/test/java/com/jdk/   one test class per example
```

Add a new topic by creating a package (for example `com.jdk.datetime`) with an example
class and a test next to it.

## Roadmap

### Phase 1: Core Java (this project)
- [ ] 01 Basics: syntax, primitive types, operators, control flow, methods, arrays, `String` / `StringBuilder`
- [ ] 02 OOP: classes, encapsulation, inheritance, polymorphism, interfaces, abstract classes, records, sealed types
- [ ] 03 Exceptions: hierarchy, checked vs unchecked, custom exceptions, try-with-resources
- [ ] 04 Collections: `List`, `Set`, `Map`, `Queue`/`Deque`, `equals`/`hashCode`, `Comparable`/`Comparator`, Big-O of each
- [ ] 05 Generics: type parameters, bounded types, wildcards (PECS), type erasure
- [ ] 06 Functional: lambdas, functional interfaces, method references, Streams, Collectors, `Optional`
- [ ] 07 I/O: `java.nio.file`, reading/writing files, serialization basics, JSON (Jackson)
- [ ] 08 Concurrency: threads, `ExecutorService`, `CompletableFuture`, locks, atomics, concurrent collections, virtual threads
- [ ] Date/time API (`java.time`), `enum`, `var`, text blocks, modern Java 17-21 features

### Phase 2: Tooling and quality
- [ ] Git workflow: branches, pull requests, meaningful commits
- [ ] Maven: lifecycle, dependencies, scopes, plugins (and a look at Gradle)
- [ ] Testing: JUnit 5, AssertJ, Mockito, test-driven development
- [ ] Clean code, SOLID, common design patterns (Builder, Factory, Strategy, Singleton, Observer)
- [ ] Logging with SLF4J + Logback, debugging in the IDE

### Phase 3: Databases
- [ ] SQL: joins, indexes, transactions, normalization (PostgreSQL or MySQL)
- [ ] JDBC: connections, `PreparedStatement`, transactions, connection pools (HikariCP)
- [ ] JPA / Hibernate: entities, relationships, lazy vs eager loading, the N+1 problem
- [ ] Database migrations with Flyway or Liquibase

### Phase 4: Web and Spring
- [ ] HTTP and REST fundamentals: methods, status codes, headers, JSON
- [ ] Spring Core: IoC container, dependency injection, beans, profiles, configuration
- [ ] Spring Boot: REST controllers, validation, global exception handling, Spring Data JPA
- [ ] Spring Security: authentication, authorization, JWT, OAuth2 basics
- [ ] API documentation with OpenAPI / Swagger
- [ ] Integration testing with `@SpringBootTest` and Testcontainers

### Phase 5: Production backend
- [ ] Docker and Docker Compose
- [ ] Caching (Redis), messaging (RabbitMQ or Kafka)
- [ ] Microservices basics: service communication, API gateway, resilience
- [ ] Observability: Actuator, metrics, structured logs
- [ ] CI/CD with GitHub Actions, deploying to a cloud provider
- [ ] Capstone: a complete REST API (for example a bank, store or task manager) with auth, DB, tests and Docker

Phases 3 and later are best done as separate Spring Boot projects (or as Maven modules
added to this repository when you get there).
