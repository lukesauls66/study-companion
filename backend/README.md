# Study Companion Backend 🚀

A comprehensive Spring Boot REST API for the Study Companion application, featuring multi-database architecture with PostgreSQL, MongoDB, and Redis.

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Database Schemas](#-database-schemas)
- [API Documentation](#-api-documentation)
- [Security Configuration](#-security-configuration)
- [Development Setup](#-development-setup)
- [Testing](#-testing)
- [Docker Configuration](#-docker-configuration)

## 🏗️ Architecture Overview

### Multi-Database Design

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │    MongoDB      │    │      Redis      │
│                 │    │                 │    │                 │
│ • Users         │    │ • DeckAnalytics │    │ • Sessions      │
│ • Decks         │    │ • ReviewSessions│    │ • CachedDecks   │
│ • Cards         │    │                 │    │ • DeckCache     │
│ • Uploads       │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                      │                      │
        └─────────┬────────────┴──────────────────────┘
                  │
            ┌─────────────────┐
            │  Spring Boot    │
            │   Application   │
            └─────────────────┘
```

### Package Structure

```
com.study_companion.backend/
├── config/                    # Configuration classes
│   ├── SecurityConfig.java   # Spring Security setup
│   └── MongoConfiguration.java
├── controller/                # REST endpoints
│   ├── auth/                 # Authentication endpoints
│   ├── postgres/             # PostgreSQL entity controllers
│   ├── mongo/                # MongoDB analytics controllers
│   └── redis/                # Redis cache controllers
├── dto/                      # Data Transfer Objects
├── exception/                # Custom exceptions & global handler
├── model/                    # Entity definitions
│   ├── postgres/            # JPA entities
│   ├── mongo/               # MongoDB documents
│   └── redis/               # Redis cache models
├── repository/              # Data access layer
├── service/                 # Business logic layer
└── BackendApplication.java  # Main Spring Boot class
```

## 🛠️ Tech Stack

- **Framework**: Spring Boot 4.0.2
- **Java Version**: Java 21
- **Security**: Spring Security with BCrypt password encoding
- **Databases**:
  - PostgreSQL 15 (Primary data)
  - MongoDB 6 (Analytics)
  - Redis 7 (Caching)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test, H2 (test database)

## 📊 Database Schemas

### PostgreSQL Entities

#### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank @Column(nullable = false, unique = true)
    private String email;

    @NotBlank @Size(max = 100)
    private String name;

    @NotBlank @Size(max = 32) @Column(unique = true)
    private String username;

    @NotBlank
    private String password; // BCrypt encoded

    @Enumerated(EnumType.STRING) @NotNull
    private Role role; // USER, ADMIN

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private boolean isVerified;

    @OneToMany(mappedBy = "user")
    private List<Deck> decks;

    @OneToMany(mappedBy = "user")
    private List<Upload> uploads;
}
```

#### Deck Entity

```java
@Entity
@Table(name = "decks")
public class Deck {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL)
    private List<Card> cards;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Card Entity

```java
@Entity
@Table(name = "cards")
public class Card {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank @Size(max = 1000)
    private String question;

    @NotBlank @Size(max = 2000)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id")
    private Deck deck;

    @Enumerated(EnumType.STRING)
    private CardCreationType creationType; // MANUAL, AI_GENERATED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Upload Entity

```java
@Entity
@Table(name = "uploads")
public class Upload {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private UUID userId;

    private UUID deckId;

    @NotBlank @Size(max = 255)
    private String fileName;

    private String fileUrl;

    @Enumerated(EnumType.STRING) @NotNull
    private FileType fileType; // PDF, DOCX, TXT

    private Long fileSize;

    @Enumerated(EnumType.STRING) @NotNull
    private ParsingStatus parsingStatus; // PENDING, PROCESSING, COMPLETED, FAILED

    private LocalDateTime parsingStartedAt;
    private LocalDateTime parsingCompletedAt;
    private String errorMessage;
}
```

### MongoDB Documents

#### DeckAnalytics

```java
@Document(collection = "deck_analytics")
public class DeckAnalytics {
    @Id
    private String id;

    private UUID deckId;
    private UUID userId;
    private int highestScore;
    private int previousScore;
    private boolean proficiency;
    private List<ReviewSession> reviewSessions;
}
```

#### ReviewSession

```java
@Document(collection = "review_sessions")
public class ReviewSession {
    @Id
    private String id;

    private UUID deckId;
    private UUID userId;
    private String deckName;
    private Date date;
    private int score;
    private int cardsReviewed;
    private int correctAnswers;
}
```

### Redis Cache Models

#### Session Cache

```java
@RedisHash("sessions")
public class Session {
    @Id
    private String id;

    private UUID userId;
    private UUID deckId;
    private String deckName;
    private int currentCardIndex;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
```

## 🔗 API Documentation

### Interactive Swagger Documentation

The API is fully documented using **OpenAPI 3.0** with interactive Swagger UI for testing and exploration.

#### Access Documentation

- **🌐 Swagger UI**: http://localhost:8080/swagger-ui.html
- **📄 OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **📝 OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### API Overview

#### 🔐 **Authentication Endpoints** (`/api/auth`)

- `POST /api/auth/register` - Register new user account
- `POST /api/auth/login` - Authenticate user and create session
- `POST /api/auth/logout` - Invalidate user session

#### 👤 **User Management** (`/api/user`) - _Authenticated_

- `POST /api/user/createUser` - Create user (admin only)
- `GET /api/user/getUserById/{id}` - Get user by ID
- `GET /api/user/getAllUsers` - Get all users (admin only)
- `PUT /api/user/updateUser/{id}` - Update user information
- `DELETE /api/user/deleteUser/{id}` - Delete user (admin only)

#### 📚 **Deck Management** (`/api/deck`)

- `GET /api/deck/getAllDecks` - Get all decks (public)
- `GET /api/deck/{deckId}` - Get deck by ID (public)
- `POST /api/deck/createDeck` - Create new deck (_authenticated_)
- `PUT /api/deck/updateDeck/{id}` - Update deck (_authenticated_)
- `DELETE /api/deck/deleteDeck/{id}` - Delete deck (_authenticated_)

#### 🃏 **Card Management** (`/api/card`)

- `GET /api/card/getCards` - Get all cards (public)
- `GET /api/card/{cardId}` - Get card by ID (public)
- `GET /api/card/deck/{deckId}` - Get cards by deck (public)
- `POST /api/card/createCard` - Create new card (_authenticated_)
- `PUT /api/card/updateCard/{id}` - Update card (_authenticated_)
- `DELETE /api/card/deleteCard/{id}` - Delete card (_authenticated_)

#### ⬆️ **Upload Management** (`/api/upload`) - _Authenticated_

- `POST /api/upload/createUpload` - Upload file for processing
- `GET /api/upload/getUpload/{id}` - Get upload status
- `GET /api/upload/getUserUploads` - Get user's uploads
- `DELETE /api/upload/deleteUpload/{id}` - Delete upload

#### 📊 **Analytics** (`/api/analytics`) - _Authenticated_

- `GET /api/analytics/getDeckAnalytics/{deckId}` - Get deck performance analytics
- `POST /api/analytics/createNewReviewSession` - Record review session
- `GET /api/analytics/getUserAnalytics` - Get user analytics summary

#### 💾 **Session Cache** (`/api/cache`) - _Authenticated_

- `POST /api/cache/createNewSession` - Create study session
- `GET /api/cache/getSession/{id}` - Get session details
- `DELETE /api/cache/deleteSession/{id}` - End session

### 🧪 Testing with Swagger UI

1. **Start the application**: Ensure Docker containers are running or `mvn spring-boot:run`
2. **Open Swagger UI**: Navigate to http://localhost:8080/swagger-ui.html
3. **Authentication**: Use `/api/auth/login` endpoint first for authenticated routes
4. **Interactive Testing**: Click "Try it out" on any endpoint to test with real data
5. **Schema Validation**: View request/response schemas automatically generated from DTOs

### 📋 Example Usage

#### Authentication Flow

```bash
# 1. Register new user
POST /api/auth/register
{
  "email": "test@example.com",
  "name": "Test User",
  "username": "testuser",
  "rawPassword": "password123"
}

# 2. Login to get session
POST /api/auth/login
{
  "username": "testuser",
  "password": "password123"
}

# 3. Now authenticated for protected endpoints
```

#### Public Data Access

```bash
# Get all public decks
GET /api/deck/getAllDecks

# Get specific deck cards
GET /api/card/deck/{deckId}
```

> 💡 **Pro Tip**: The Swagger UI provides **live validation** and **example data** for all endpoints.

---

## 🔒 Security Configuration

### Authentication

- **Password Encoding**: BCrypt with default strength (10 rounds)
- **Session Management**: `SessionCreationPolicy.IF_REQUIRED`
- **User Details**: Custom `CustomUserDetailsService` loads users by username
- **Authorities**: Role-based (`ROLE_USER`, `ROLE_ADMIN`)

### Authorization Rules

- **Public Endpoints**:
  - `/api/auth/**` - Authentication endpoints
  - `/api/deck/getAllDecks`, `/api/deck/{deckId}` - Public deck access
  - `/api/card/getCards`, `/api/card/{cardId}`, `/api/card/deck/**` - Public card access
- **Authenticated Endpoints**: All other endpoints require authentication

### CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    return source;
}
```

## ⚙️ Development Setup

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- MongoDB 6+
- Redis 7+

### Local Development

1. **Clone and navigate to backend**:

   ```bash
   git clone https://github.com/lukesauls66/study-companion.git
   cd study-companion/backend
   ```

2. **Start databases with Docker**:

   ```bash
   cd ..
   docker-compose up -d postgres mongo redis
   ```

3. **Run Spring Boot application**:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. **API will be available at**: `http://localhost:8080`

### Environment Variables

#### Required for Local Development

```properties
# Default application.properties
spring.application.name=backend
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### Docker Environment (docker-compose.yml)

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/studydb
  SPRING_DATASOURCE_USERNAME: user
  SPRING_DATASOURCE_PASSWORD: password
  MONGO_URI: mongodb://mongo:27017/studylogs
  REDIS_HOST: redis
```

## 🧪 Testing

### Test Structure

```
src/test/java/com/study_companion/backend/
├── model/                    # Entity tests
│   ├── postgres/            # JPA entity tests
│   ├── mongo/               # MongoDB document tests
│   └── redis/               # Redis model tests
└── service/                 # Service integration tests
    ├── UserServiceIntegrationTest.java
    ├── DeckServiceIntegrationTest.java
    ├── CardServiceIntegrationTest.java
    ├── UploadServiceIntegrationTest.java
    ├── DeckAnalyticsServiceIntegrationTest.java
    └── SessionCacheServiceIntegrationTest.java
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceIntegrationTest

# Generate test report
./mvnw surefire-report:report
```

### Test Database

- **In-Memory Database**: H2 for integration tests
- **Test Profiles**: Automatic Spring Boot test configuration
- **Test Data**: Comprehensive test scenarios covering edge cases

## 🐳 Docker Configuration

### Multi-Stage Dockerfile

```dockerfile
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Service Dependencies

```yaml
backend:
  depends_on:
    postgres:
      condition: service_healthy
    mongo:
      condition: service_healthy
    redis:
      condition: service_healthy
```

### Health Checks

- **PostgreSQL**: `pg_isready -U user -d studydb`
- **MongoDB**: `mongosh --eval "db.adminCommand('ping')"`
- **Redis**: `redis-cli ping`

## 📝 Development Notes

### Code Quality Standards

- **Exception Handling**: Comprehensive custom exceptions with GlobalExceptionHandler
- **Validation**: JSR-303 Bean Validation on all DTOs and entities
- **Logging**: SLF4J with structured logging throughout services
- **Documentation**: Extensive JavaDoc on all public methods
- **Security**: BCrypt password encoding, input validation, SQL injection prevention

### Performance Considerations

- **Lazy Loading**: JPA relationships configured for optimal performance
- **Connection Pooling**: HikraCP for database connections
- **Caching**: Redis for session management and deck caching
- **Indexing**: Appropriate database indexes on frequently queried fields

### Future Enhancements

- **API Versioning**: Implement versioning strategy for API evolution
- **Rate Limiting**: Add rate limiting for API endpoints
- **Monitoring**: Integrate Spring Boot Actuator for health checks and metrics
- **Documentation**: Consider Swagger/OpenAPI for automated API docs

---

**Backend Status**: ✅ Production Ready for MVP  
**Last Updated**: February 22, 2026  
**Version**: 1.0.0
