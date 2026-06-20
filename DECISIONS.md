# Decisions

**Name:** Merugu Vaishnavi

**Date started:** 20-06-2026

**Date submitted:** 20-06-2026


I started by understanding the project structure, reading the README, service layer, repository layer, models, and existing tests. I prioritised fixing the failing monthly spend functionality because it was the customer-facing issue mentioned in the task, then improved the supporting calculations and ensured the behaviour was covered by tests.


---

# 1. Code & Design Decisions


## The codebase includes an `Auditable` abstract class that is not currently used by any entity. What did you do with it, if anything?

I reviewed the `Auditable` abstract class but did not introduce it into existing entities.

The purpose of the Auditable pattern is to automatically track common metadata such as created date, updated date, and potentially the user responsible for changes. It helps maintain consistency across entities and avoids duplicating audit fields.

However, applying it without a clear requirement would introduce unnecessary changes to the existing data model. Since the current application scope is transaction management and reporting, I avoided modifying entity inheritance relationships because it could affect database mappings and existing behaviour.

If auditing becomes a product requirement later, the pattern can be introduced carefully with migration support and entity testing.


---

## `TransactionResponse` is used as the outbound DTO for the API. What changes did you make to it, if any?

I reviewed `TransactionResponse` but did not make unnecessary changes because its existing structure was sufficient for the current API requirements.

Response DTOs are important because they separate the internal database model from the external API contract. Returning entities directly from controllers can expose internal implementation details, create tight coupling between API consumers and database structure, and accidentally expose fields that should remain private.

Using DTOs provides better control over API versioning, validation, and future changes.


---

## The `BudgetCalculator` requires grouping and sorting data. What data structure or approach did you choose?

I implemented category aggregation using Java Streams with:

- `Collectors.groupingBy()` for grouping transactions by category
- `BigDecimal` reduction for calculating totals
- Sorting using category spending totals in descending order
- `LinkedHashMap` to preserve the sorted order

Alternative approaches considered:

1. Manual iteration using loops and HashMaps:
   - Simple but requires more code and manual sorting.

2. Database aggregation queries:
   - Efficient for very large datasets but adds complexity and depends on database-specific queries.

3. Java Stream processing:
   - Cleaner, readable, and appropriate for the current application size.

I selected Java Streams because the current dataset size does not require database-level aggregation and the implementation remains maintainable.


---

## Were there any decisions you made that are not covered above?

The biggest decision was fixing monthly spend calculation using inclusive date boundaries.

The previous implementation did not correctly handle transactions occurring on the first day of the month. I changed the logic to include:

- transaction date >= first day of month
- transaction date <= last day of month

This matches expected calendar month behaviour.


---

# 2. Bug Fixes & Issues Found


## Describe each problem you found in the codebase.

### 1. Monthly Spend Calculation Bug

Location:
`TransactionServiceImpl`

Problem:
The monthly spend calculation did not correctly include all transactions within a month.

Impact:
Transactions on boundary dates could be excluded, causing incorrect spending reports.

Fix:
Implemented inclusive date filtering and grouped results by transaction category.


### 2. Incorrect Service Interface Implementation

Location:
`TransactionServiceImpl`

Problem:
The implementation method signatures did not match the `TransactionService` interface.

Examples:
- Wrong monthly spend method parameters
- Incorrect transaction creation method signature
- Incorrect account transaction method name

Impact:
The application failed to compile.

Fix:
Updated the implementation to exactly match the service interface.


### 3. Missing Repository Query Support

Location:
`TransactionRepository`

Problem:
Required repository methods were missing.

Impact:
The service layer could not correctly retrieve transactions needed for calculations.

Fix:
Added repository methods required by the service implementation.


### 4. Spending Category Calculation Issue

Location:
`BudgetCalculator`

Problem:
Top spending categories were not being calculated correctly.

Impact:
The API returned empty or incorrect category results.

Fix:
Implemented grouping, summing, sorting, and limiting logic.


---

## Were there any problems you noticed but chose not to fix?

Some improvements were identified but were outside the immediate scope:

- Adding authentication and authorization
- Adding pagination for transaction listing
- Introducing soft deletion
- Adding database migration management

These were not changed because the task focused on transaction management correctness and reporting behaviour.


---

# 3. Testing Decisions


## What tests did you write in `TransactionCandidateTest.java`?

I focused on validating important transaction behaviours:

1. Monthly spend calculation:
   - Ensures transactions on the first day of the month are included.
   - Prevents regression of the identified bug.

2. Category aggregation:
   - Ensures spending totals are correctly grouped by category.

3. Top spending categories:
   - Ensures categories are sorted correctly and only the required number of categories are returned.


---

## What did you deliberately not test, and why?

I did not add tests for:

- Database connection behaviour
- Spring Boot startup
- Infrastructure configuration

These are generally covered through integration testing and are outside the main business logic of this task.

With more time, I would add controller-level integration tests using MockMvc.


---

## Difference between `TransactionServiceTest` and `TransactionCandidateTest`

`TransactionServiceTest` validates the existing service implementation behaviour using unit tests.

`TransactionCandidateTest` focuses on additional regression coverage for the specific requirements and bugs identified during development.

They are not testing exactly the same thing:

- Service tests verify implementation behaviour.
- Candidate tests verify important expected business scenarios.


---

# 4. AI Tool Usage


## Which AI tools did you use?

I used:

- ChatGPT


---

## Examples of how you used AI

### Example 1:
Prompt:
"Help me analyse the Spring Boot project and fix failing tests."

AI helped identify the mismatch between the service interface and implementation.

I verified the suggestions by checking the existing code and test failures before applying changes.


### Example 2:
Prompt:
"Explain the failing monthly spend test and suggest a fix."

AI suggested checking date boundary conditions.

I accepted the approach after validating that the first day of month transactions were incorrectly excluded.


### Example 3:
Prompt:
"Suggest improvements for repository and service implementation."

AI provided possible improvements, but I reviewed them against the existing architecture before applying changes.


---

## Describe a moment where AI gave something wrong.

Initially, AI suggested a method signature for `calculateMonthlySpend()` that did not match the existing `TransactionService` interface.

I identified the issue because Maven compilation failed with an override error.

I corrected the implementation by checking the interface and making the service implementation match the existing contract.


---

## General philosophy on using AI for backend code

AI is useful for:

- Understanding unfamiliar codebases
- Generating implementation ideas
- Finding possible bugs
- Explaining errors

However, AI output should always be verified against:

- Existing architecture
- Interfaces
- Tests
- Business requirements

AI should assist engineering decisions, not replace understanding of the system.


---

# 5. What You'd Do Next


## Priority 1: Add API validation

Add stronger validation for transaction inputs such as:

- Positive amount
- Required category
- Valid transaction dates

Reason:
Prevents invalid financial records.


## Priority 2: Add controller integration tests

Add API-level tests for:

- POST transaction
- GET transactions
- DELETE transaction
- Monthly reports

Reason:
Ensures the complete request flow works correctly.


## Priority 3: Add pagination

Implement pagination for transaction listing.

Reason:
The system will eventually handle larger transaction volumes.


## Priority 4: Improve exception handling

Introduce structured error responses.

Reason:
Provides better API experience for frontend clients.


---

## Biggest remaining risk or weakness

The biggest remaining weakness is that the application currently uses an in-memory H2 database.

For production usage, persistent storage, migrations, monitoring, and stronger security controls would need to be introduced.