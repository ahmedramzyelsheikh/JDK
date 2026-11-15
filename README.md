# JDK
Learn Backend Java Path

A complete path to becoming a Java backend developer: **29 topic folders** with theory, diagrams,
the packages/libraries to use (and why), corner cases, interview questions, exercises and runnable,
tested practicals — plus a small starter Maven project to write your own code in.

👉 **Start here: [topics/README.md](topics/README.md)**

## Requirements

- JDK 21 (Eclipse Temurin 21 recommended)
- Maven is optional: the Maven Wrapper (`mvnw` / `mvnw.cmd`) downloads the right version for you.
- Optional: .NET SDK (C# comparisons in topics 03 and 09), GCC (C/C++ comparison), Docker (topics 23–29).

## Commands

```bash
# your own exercises (starter project in src/)
./mvnw test                                  # Windows: .\mvnw.cmd test

# all topic practicals that use Maven (Spring Boot, Hibernate, Jackson, jOOQ, ...)
./mvnw -f topics/pom.xml verify

# all single-file practicals (java File.java)
bash scripts/run-practicals.sh

# one single-file practical
java topics/06-advanced-java/06-concurrency/practical/VirtualThreadsDemo.java
```

In IntelliJ IDEA or VS Code (with the Java Extension Pack) open the folder; both `pom.xml` files are picked up as
Maven projects.

## Repository layout

```
topics/                      the learning material: 01-history ... 29-capstone (see topics/README.md)
  pom.xml                    parent of every Maven-based practical (Spring Boot 4.1 BOM, Java 21)
scripts/run-practicals.sh    runs every single-file practical (used by CI)
src/main/java/com/jdk/       starter project: one package per core topic for your own exercises
src/test/java/com/jdk/       tests for the starter project
.github/workflows/build.yml  CI: builds and tests everything on every push
```

## Roadmap

### Phase 1: Core Java
- [ ] [01 History](topics/01-history/README.md) · [02 Compilation & JVM](topics/02-compilation-and-jvm/README.md) · [03 C/C++/C#/Java](topics/03-language-comparison/README.md)
- [ ] [04 Foundation syntax](topics/04-foundation-syntax/README.md): types, operators, control flow, methods, arrays, strings, `java.time`, `enum`, `var`, text blocks
- [ ] [05 OOP](topics/05-oop/README.md): classes, encapsulation, inheritance, polymorphism, interfaces, abstract classes, records, sealed types
- [ ] [06.01 Exceptions](topics/06-advanced-java/01-exceptions/README.md) · [06.02 Collections](topics/06-advanced-java/02-collections/README.md) · [06.03 Generics](topics/06-advanced-java/03-generics/README.md)
- [ ] [06.04 Lambdas & Streams](topics/06-advanced-java/04-functional-streams/README.md) · [06.05 I/O & JSON](topics/06-advanced-java/05-io-nio-json/README.md) · [06.06 Concurrency](topics/06-advanced-java/06-concurrency/README.md)
- [ ] [06.07 Modern Java](topics/06-advanced-java/07-modern-java/README.md) · [06.08 JVM memory & GC](topics/06-advanced-java/08-jvm-memory-gc/README.md) · [06.09 Reflection & modules](topics/06-advanced-java/09-reflection-annotations-modules/README.md)
- [ ] [07 MVC](topics/07-mvc/README.md) · [08 REST APIs](topics/08-rest-api/README.md) · [09 LINQ](topics/09-linq/README.md) · [10 ORM](topics/10-orm/README.md)

### Phase 2: Tooling and quality
- [ ] [11 Git workflow](topics/11-git/README.md): branches, pull requests, meaningful commits
- [ ] [12 Maven & Gradle](topics/12-build-tools/README.md): lifecycle, dependencies, scopes, plugins
- [ ] [13 Testing](topics/13-testing/README.md): JUnit, AssertJ, Mockito, test-driven development
- [ ] [14 Clean code, SOLID, design patterns](topics/14-clean-code-solid-design-patterns/README.md)
- [ ] [15 Logging & debugging](topics/15-logging-debugging/README.md): SLF4J + Logback, debugging in the IDE

### Phase 3: Databases
- [ ] [16 SQL](topics/16-sql/README.md): joins, indexes, transactions, normalization
- [ ] [17 JDBC](topics/17-jdbc/README.md): connections, `PreparedStatement`, transactions, HikariCP
- [ ] JPA / Hibernate: covered in [10 ORM](topics/10-orm/README.md)
- [ ] [18 Migrations](topics/18-database-migrations/README.md): Flyway, Liquibase

### Phase 4: Web and Spring
- [ ] HTTP and REST fundamentals: covered in [08 REST APIs](topics/08-rest-api/README.md)
- [ ] [19 Spring Core](topics/19-spring-core/README.md): IoC container, dependency injection, beans, profiles, configuration
- [ ] [20 Spring Boot](topics/20-spring-boot/README.md): REST controllers, validation, global exception handling, Spring Data JPA
- [ ] [21 Spring Security](topics/21-spring-security/README.md): authentication, authorization, JWT, OAuth2
- [ ] [22 OpenAPI / Swagger](topics/22-openapi-swagger/README.md)
- [ ] [23 Integration testing](topics/23-integration-testing/README.md): `@SpringBootTest`, Testcontainers

### Phase 5: Production backend
- [ ] [24 Docker & Compose](topics/24-docker/README.md)
- [ ] [25 Caching & messaging](topics/25-caching-messaging/README.md): Redis, RabbitMQ, Kafka
- [ ] [26 Microservices](topics/26-microservices/README.md): communication, API gateway, resilience
- [ ] [27 Observability](topics/27-observability/README.md): Actuator, metrics, tracing, structured logs
- [ ] [28 CI/CD & cloud](topics/28-cicd-cloud/README.md): GitHub Actions, deployment
- [ ] [29 Capstone](topics/29-capstone/README.md): a complete REST API with auth, DB, tests and Docker

## Starter project (`src/`)

`src/main/java/com/jdk/` has one package per core topic (basics, oop, exceptions, collections, generics,
functional, io, concurrency), each with a small example and a JUnit 5 test. Add your exercise solutions there.
