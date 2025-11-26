# Repository Methods Reference Guide

## Overview

This document provides a comprehensive reference for Spring Data repository interfaces used in the Study Companion backend.

## Repository Interface Hierarchy

```
CrudRepository<T, ID>
    ↑
PagingAndSortingRepository<T, ID>
    ↑
JpaRepository<T, ID>

CrudRepository<T, ID>
    ↑
PagingAndSortingRepository<T, ID>
    ↑
MongoRepository<T, ID>
```

## Repository Types by Data Store

| Data Store     | Repository Interface     | Entity Annotation | Example                   |
| -------------- | ------------------------ | ----------------- | ------------------------- |
| **PostgreSQL** | `JpaRepository<T, ID>`   | `@Entity`         | `CardRepository`          |
| **MongoDB**    | `MongoRepository<T, ID>` | `@Document`       | `ReviewSessionRepository` |
| **Redis**      | `CrudRepository<T, ID>`  | `@RedisHash`      | `SessionRepository`       |

---

## CrudRepository<T, ID> Methods

### Create/Update Operations

```java
<S extends T> S save(S entity);                           // Save single entity
<S extends T> Iterable<S> saveAll(Iterable<S> entities);  // Save multiple entities
```

### Read Operations

```java
Optional<T> findById(ID id);                              // Find by primary key
boolean existsById(ID id);                                // Check if entity exists
Iterable<T> findAll();                                    // Find all entities
Iterable<T> findAllById(Iterable<ID> ids);               // Find multiple by IDs
long count();                                             // Count all entities
```

### Delete Operations

```java
void deleteById(ID id);                                   // Delete by ID
void delete(T entity);                                    // Delete entity instance
void deleteAllById(Iterable<? extends ID> ids);          // Delete multiple by IDs
void deleteAll(Iterable<? extends T> entities);          // Delete multiple entities
void deleteAll();                                         // Delete all entities
```

---

## JpaRepository<T, ID> Methods

**Inherits ALL CrudRepository methods PLUS:**

### Enhanced Create/Update (Better Return Types)

```java
<S extends T> List<S> saveAll(Iterable<S> entities);      // Returns List instead of Iterable
<S extends T> S saveAndFlush(S entity);                   // Save + immediate flush
<S extends T> List<S> saveAllAndFlush(Iterable<S> entities); // Batch save + flush
```

### Enhanced Read Operations

```java
List<T> findAll();                                        // Returns List instead of Iterable
List<T> findAll(Sort sort);                              // Find all with sorting
List<T> findAllById(Iterable<ID> ids);                   // Returns List instead of Iterable
```

### Bulk Delete Operations (Better Performance)

```java
void deleteAllInBatch(Iterable<T> entities);             // Batch delete (single SQL)
void deleteAllByIdInBatch(Iterable<ID> ids);             // Batch delete by IDs
void deleteAllInBatch();                                  // Delete all (truncate-like)
```

### JPA-Specific Operations

```java
void flush();                                             // Force immediate DB write
T getById(ID id);                                        // Get reference (may be lazy)
T getReferenceById(ID id);                               // Get JPA reference
```

### Pagination & Sorting (from PagingAndSortingRepository)

```java
Iterable<T> findAll(Sort sort);                          // Find with sorting
Page<T> findAll(Pageable pageable);                      // Find with pagination
```

---

## MongoRepository<T, ID> Methods

**Inherits ALL CrudRepository + PagingAndSortingRepository methods PLUS:**

### Enhanced Create/Update

```java
<S extends T> List<S> saveAll(Iterable<S> entities);      // Returns List
<S extends T> S insert(S entity);                         // MongoDB insert (fails if exists)
<S extends T> List<S> insert(Iterable<S> entities);      // Batch insert
```

### Enhanced Read Operations

```java
List<T> findAll();                                        // Returns List
List<T> findAll(Sort sort);                              // Find all with sorting
List<T> findAllById(Iterable<ID> ids);                   // Returns List
```

### Pagination & Sorting

```java
Page<T> findAll(Pageable pageable);                      // Pagination support
Iterable<T> findAll(Sort sort);                          // Sorting support
```

---

## Method Comparison Table

| Operation            | CrudRepository        | JpaRepository                  | MongoRepository           |
| -------------------- | --------------------- | ------------------------------ | ------------------------- |
| **save()**           | ✅ Iterable return    | ✅ List return + flush options | ✅ List return + insert() |
| **findAll()**        | ✅ Iterable return    | ✅ List return + sorting       | ✅ List return + sorting  |
| **delete()**         | ✅ Individual deletes | ✅ Batch deletes               | ✅ Individual deletes     |
| **Pagination**       | ❌                    | ✅ Page<T>                     | ✅ Page<T>                |
| **Sorting**          | ❌                    | ✅ Sort parameter              | ✅ Sort parameter         |
| **Flushing**         | ❌                    | ✅ flush(), saveAndFlush()     | ❌                        |
| **Batch Operations** | ❌                    | ✅ deleteAllInBatch()          | ❌                        |
| **Insert vs Save**   | ❌                    | ❌                             | ✅ insert() method        |

---

## Usage Examples

### CrudRepository (Redis)

```java
// Basic CRUD only - used for Redis entities
@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    Optional<Session> findByUserId(UUID userId);
}

// Usage
Session session = sessionRepository.save(new Session(userId));
Optional<Session> found = sessionRepository.findById(sessionId);
sessionRepository.deleteById(sessionId);
```

### JpaRepository (PostgreSQL)

```java
// Enhanced operations - used for PostgreSQL entities
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByDeckId(UUID deckId);
    long countByDeckId(UUID deckId);
    void deleteByDeckId(UUID deckId);
}

// Usage
List<Card> cards = cardRepository.saveAllAndFlush(cardList);  // Batch + flush
List<Card> allCards = cardRepository.findAll(Sort.by("createdAt").descending());
Page<Card> pagedCards = cardRepository.findAll(PageRequest.of(0, 10));
cardRepository.deleteAllInBatch(cardsToDelete);  // Efficient batch delete
```

### MongoRepository (MongoDB)

```java
// Document-specific operations - used for MongoDB entities
@Repository
public interface ReviewSessionRepository extends MongoRepository<ReviewSession, String> {
    List<ReviewSession> findByUserId(UUID userId);
    List<ReviewSession> findByDeckId(UUID deckId);
}

// Usage
ReviewSession session = reviewRepository.insert(new ReviewSession()); // Insert only
List<ReviewSession> sessions = reviewRepository.findAll(Sort.by("sessionDate"));
Page<ReviewSession> paged = reviewRepository.findAll(PageRequest.of(0, 20));
```

---

## Custom Query Methods

### Spring Data Query Method Naming Conventions

```java
// All repository types support custom query methods via method naming:

// Find operations
List<T> findBy<PropertyName>(PropertyType value);
List<T> findBy<PropertyName>And<Property2Name>(Type1 value1, Type2 value2);
List<T> findBy<PropertyName>Or<Property2Name>(Type1 value1, Type2 value2);

// Existence checks
boolean existsBy<PropertyName>(PropertyType value);

// Count operations
long countBy<PropertyName>(PropertyType value);

// Delete operations
void deleteBy<PropertyName>(PropertyType value);

// Sorting and limiting
List<T> findBy<PropertyName>OrderBy<SortProperty>Asc(PropertyType value);
List<T> findTop10By<PropertyName>(PropertyType value);
```

### Examples by Repository Type

```java
// PostgreSQL (JpaRepository)
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByDeckId(UUID deckId);                               // Navigate to deck.id
    List<Card> findByDeckIdAndCreationType(UUID deckId, CardCreationType type);
    Page<Card> findByDeckId(UUID deckId, Pageable pageable);           // With pagination
    long countByDeckId(UUID deckId);
    void deleteByDeckId(UUID deckId);
}

// MongoDB (MongoRepository)
public interface ReviewSessionRepository extends MongoRepository<ReviewSession, String> {
    List<ReviewSession> findByUserId(UUID userId);
    List<ReviewSession> findByUserIdAndSessionDateBetween(UUID userId, LocalDate start, LocalDate end);

    // MongoDB-specific @Query annotation
    @Query("{ 'userId': ?0, 'score': { $gte: ?1 } }")
    List<ReviewSession> findByUserIdAndMinScore(UUID userId, int minScore);
}

// Redis (CrudRepository)
public interface SessionRepository extends CrudRepository<Session, String> {
    Optional<Session> findByUserId(UUID userId);                        // Uses @Indexed field
    boolean existsByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
```

---

## Performance Considerations

### JpaRepository Performance Tips

```java
// Use batch operations for better performance
cardRepository.saveAllAndFlush(cards);          // Better than multiple save() calls
cardRepository.deleteAllInBatch(cards);         // Single DELETE statement

// Use pagination for large datasets
Page<Card> cards = cardRepository.findAll(PageRequest.of(0, 20));

// Force immediate persistence when needed
cardRepository.saveAndFlush(card);              // Immediate DB write
```

### MongoDB Performance Tips

```java
// Use insert() for new documents (faster than save())
reviewRepository.insert(newSession);            // Fails if document exists

// Use proper indexing with @Indexed annotation on entity fields
@Indexed
private UUID userId;                            // Creates MongoDB index

// Use @Query for complex operations
@Query("{ 'userId': ?0, 'sessionDate': { $gte: ?1 } }")
List<ReviewSession> findRecentSessions(UUID userId, LocalDate since);
```

### Redis Performance Tips

```java
// Redis is already optimized for key-value operations
sessionRepository.save(session);                // O(1) operation
sessionRepository.findById(sessionId);          // O(1) lookup

// Use @Indexed for secondary key lookups
@Indexed
private UUID userId;                            // Allows findByUserId() queries
```

---

## Testing Strategy

### Repository Testing Guidelines

- **CrudRepository (Redis)**: Integration tests with Testcontainers
- **JpaRepository (PostgreSQL)**: Integration tests with @DataJpaTest
- **MongoRepository (MongoDB)**: Integration tests with @DataMongoTest

### Example Test Structure

```java
// PostgreSQL Integration Test
@DataJpaTest
class CardRepositoryTest {
    @Autowired
    private CardRepository cardRepository;

    @Test
    void testFindByDeckId() {
        // Test with real H2/PostgreSQL
    }
}

// MongoDB Integration Test
@DataMongoTest
class ReviewSessionRepositoryTest {
    @Autowired
    private ReviewSessionRepository reviewRepository;

    @Test
    void testFindByUserId() {
        // Test with embedded MongoDB
    }
}

// Redis Integration Test with Testcontainers
@DataRedisTest
@Testcontainers
class SessionRepositoryTest {
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Test
    void testSessionPersistence() {
        // Test with real Redis container
    }
}
```

---

## Summary

- **PostgreSQL entities** → `JpaRepository<T, ID>` (full JPA features)
- **MongoDB documents** → `MongoRepository<T, ID>` (document operations)
- **Redis hashes** → `CrudRepository<T, ID>` (key-value operations)

Each repository type provides the appropriate level of functionality for its underlying data store while maintaining a consistent Spring Data interface.
