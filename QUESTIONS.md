# Questions

---

# Java & Object-Oriented Design

## 1. The `Auditable` abstract class in this codebase uses `@MappedSuperclass`. Explain what this annotation tells JPA, and describe what would happen to the database schema if you removed it. Why is it better to put `createdAt` and `updatedAt` in a shared abstract class rather than adding those fields directly to `Transaction` and `Account` separately?

`@MappedSuperclass` tells JPA that the fields declared in the parent class should be inherited by child entity classes and mapped into their database tables. The parent class itself will not have a separate database table.

If this annotation is removed, JPA will not automatically include fields such as `createdAt` and `updatedAt` in the child entity tables. Each entity would need to define these fields separately or use another inheritance strategy.

Using a shared abstract class avoids duplicate code and keeps auditing behaviour consistent across multiple entities. If the auditing requirements change in the future, the changes can be made in one place instead of updating every entity class.


---

## 2. `TransactionService` is defined as a Java interface, with `TransactionServiceImpl` as its only implementation. Why use the interface?

An interface defines the contract of the service layer separately from its implementation. Even though there is currently only one implementation, it provides flexibility for future changes.

For example, a mock implementation can be created for unit testing, or another implementation can be added later without modifying the controller layer.

It also improves maintainability because developers can understand the available service operations without looking at the internal implementation details.


---

## 3. `Category` is modelled as an enum rather than a String field on `Transaction`. Explain.

Using an enum restricts Category values to a predefined set and prevents invalid category values from being stored.

With:

```java
@Enumerated(EnumType.STRING)
```

JPA stores the actual enum name in the database.

Example:

```
FOOD
TRANSPORT
UTILITIES
```

This approach is safer than storing ordinal values because adding a new enum value does not change existing stored values.

If a future developer adds a new category but forgets to update related business logic, reports, filtering, or calculations may not handle the new category correctly.


---

## 4. `BudgetCalculator` is a final class with a private constructor and a single static method. What pattern is this?

`BudgetCalculator` follows the Utility Class pattern.

A utility class contains helper methods that do not require object state. The class is marked as final to prevent inheritance and uses a private constructor to prevent creating objects.

For grouping and sorting data, I used a `Map<Category, BigDecimal>` as an intermediate aggregation structure. Transactions are grouped by category, the total spending amount is calculated, and the entries are sorted. The final result is stored in a `LinkedHashMap` because it maintains insertion order after sorting.


---

# Spring Boot & REST API Design

## 5. Why was returning `ResponseEntity<Transaction>` instead of `ResponseEntity<TransactionResponse>` a problem?

Returning entities directly from controllers exposes the database model as the API response structure.

A DTO separates the internal database representation from the external API contract. It allows control over which fields are exposed and prevents database changes from directly affecting API consumers.

Returning entities directly can expose unnecessary fields, create tight coupling between frontend and backend, and make future API changes more difficult.


---

## 6. Describe the complete journey from HTTP request to database insert.

The request flow is:

1. Client sends a POST request containing transaction details.
2. Controller receives the request using `@PostMapping`.
3. Spring converts JSON into a `TransactionRequest` object.
4. Validation is performed using `@Valid`.
5. Controller calls the service layer.
6. Service performs business logic.
7. Repository saves the entity.
8. JPA inserts the record into the database.
9. Response DTO is returned to the client.

The controller handles HTTP communication, the service handles business logic, and the repository handles persistence.

If `@Valid` is removed, invalid input can reach the service and database layers, which may result in incorrect data being stored.


---

## 7. Why does Spring provide `@RestController`, `@Service`, and `@Repository`?

Spring provides different stereotype annotations to communicate the responsibility of each component.

`@RestController` represents the API layer and handles HTTP requests.

`@Service` represents the business logic layer.

`@Repository` represents the data access layer.

Although all three register Spring beans, the separation improves readability and maintainability. It also allows Spring to provide additional behaviour such as exception translation for repository classes.


---

## 8. What HTTP status code should be returned if `month=13` is passed?

The endpoint should return:

```
400 Bad Request
```

because the request contains invalid input.

The controller should validate that the month value is between 1 and 12 before calling the service layer. This can be implemented using manual validation or custom validation annotations.

Spring does not automatically understand that an integer represents a valid calendar month, so application-level validation is required.


---

# Data Access & SQL

## 9. How does Spring Data JPA generate queries from method names?

Spring Data JPA analyses repository method names during application startup and automatically generates queries.

For example:

```java
findByAccountId(Long accountId)
```

is converted into a query that searches transactions where the accountId matches the given value.

Derived query methods reduce the amount of SQL code developers need to write.

A `@Query` annotation should be used when the query requires complex joins, custom filtering, aggregation, or when the derived method name becomes difficult to understand.


---

## 10. Explain the monthly spend date boundary bug.

The monthly spend bug was caused by incorrect date boundary handling.

The previous implementation did not correctly include transactions that occurred on the first day of the month. Because of this, valid transactions were excluded from the monthly calculation.

The issue was fixed by using inclusive date comparisons:

```
transaction date >= first day of month
transaction date <= last day of month
```

A test case containing a transaction exactly on the first day of a month exposes this type of off-by-one error.

Date bugs are common because developers often make mistakes with inclusive and exclusive ranges.


---

## 11. How would you change H2 to PostgreSQL in production?

To move from H2 to PostgreSQL, I would make the following changes.

First, add the PostgreSQL dependency in `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

Then update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/verodb
spring.datasource.username=username
spring.datasource.password=password

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

The H2 dependency can be removed if it is not required anymore.

Using:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

in production is dangerous because it deletes database tables when the application stops. Production systems should use migration tools like Flyway or Liquibase.


---

# Testing

## 12. Explain Mockito usage in `TransactionServiceTest`.

Mockito creates fake objects for unit testing.

`@Mock` creates a fake `TransactionRepository` instead of using the real database.

`@InjectMocks` creates `TransactionServiceImpl` and injects the mocked repository into it.

The test verifies service behaviour independently from database operations.

It can catch service-layer logic problems but cannot detect database configuration issues or controller-level problems.


---

## 13. Should controller tests be skipped because service tests exist?

No.

Service tests and controller tests validate different parts of the application.

Controller tests using MockMvc can detect problems such as incorrect endpoint mappings, incorrect HTTP status codes, validation failures, or incorrect response formats.

For example, the service may successfully create a transaction, but the controller may return an incorrect HTTP status code.


---

## 14. What was the first test you focused on in `TransactionCandidateTest.java`?

The first test I focused on was the monthly spend calculation boundary scenario because it represented the main failing behaviour in the existing system.

I selected this test because the monthly spend report was the visible customer-facing functionality mentioned in the task.

The order of testing shows that I prioritised fixing the most important business requirement first and then improved coverage for related functionality.


---

# AI & Modern Engineering

## 15. Describe how you used AI tools during this project.

I used ChatGPT as an assistant during this project for understanding the codebase, debugging errors, and reviewing implementation approaches.

One example was debugging Maven compilation failures. AI helped identify that the methods in `TransactionServiceImpl` did not match the methods defined in the `TransactionService` interface. I verified this manually by comparing the interface and implementation before making changes.

Another example was analysing the monthly spend test failure. AI suggested checking date boundary conditions, which helped identify that transactions on the first day of the month were excluded.

One situation where AI required careful verification was when it suggested a method signature that did not match the existing service interface. I caught this because Maven compilation failed with override errors and corrected the implementation according to the project structure.

My approach is to use AI as a development assistant for faster debugging, explanations, and generating ideas. However, every AI suggestion must be verified against the existing architecture, requirements, and test results.