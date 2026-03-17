# Study Companion 📚

A modern, AI-powered study companion application that helps students create, manage, and review flashcards with intelligent analytics and progress tracking.

## 🚀 Features

- **Smart Flashcard Creation**: Upload study materials and let AI automatically generate flashcards
- **Progress Analytics**: Track study performance with detailed analytics and insights
- **File Upload Support**: Parse various file formats (PDF, DOCX, TXT) into flashcards
- **User Management**: Secure user authentication and personal deck organization
- **Real-time Processing**: Asynchronous file parsing with status tracking

## 🏗️ Architecture

### Tech Stack

- **Frontend**: Next.js 16 with TypeScript, React 19, Tailwind CSS 4
- **Backend**: Spring Boot 4.0.2 with Java 21
- **Databases**:
  - PostgreSQL (Primary data: Users, Decks, Cards, Uploads)
  - MongoDB (Analytics: Study sessions, Deck analytics)
  - Redis (Caching & Session management)
- **Containerization**: Docker & Docker Compose

### Database Design

#### PostgreSQL (Primary Data)

```sql
-- Core entities for structured data with ACID compliance
Users → Decks → Cards
       ↓
     Uploads (with parsing status tracking)
```

#### MongoDB (Analytics & Logs)

```javascript
// Flexible schema for analytics and session data
DeckAnalytics: {
  (deckId, userId, scores, sessions, proficiency);
}

ReviewSessions: {
  (date, deckName, score, cardsReviewed, correctAnswers);
}
```

## 📁 Project Structure

```
study-companion/
├── docker-compose.yml          # Multi-service orchestration
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/study_companion/backend/
│   │   ├── controller/         # REST API controllers
│   │   │   ├── auth/          # Authentication endpoints
│   │   │   ├── postgres/      # PostgreSQL entity controllers
│   │   │   ├── mongo/         # MongoDB analytics controllers
│   │   │   └── redis/         # Redis cache controllers
│   │   ├── model/
│   │   │   ├── postgres/      # JPA entities (User, Deck, Card, Upload)
│   │   │   ├── mongo/         # MongoDB documents (Analytics, Sessions)
│   │   │   └── redis/         # Redis cache models
│   │   ├── service/           # Business logic layer
│   │   ├── repository/        # Data access layers
│   │   ├── config/            # Security and database configuration
│   │   ├── dto/               # Data transfer objects
│   │   └── exception/         # Custom exceptions and global handler
│   └── pom.xml               # Maven dependencies
└── frontend/                  # Next.js application
    ├── app/                   # App router pages
    ├── lib/                   # API utilities
    └── package.json          # Node dependencies
```

## 🚦 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21+ (for local development)
- Node.js 18+ (for local development)

### Quick Start with Docker

1. **Clone the repository**

   ```bash
   git clone https://github.com/lukesauls66/study-companion.git
   cd study-companion
   ```

2. **Start all services**

   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - PostgreSQL: localhost:5432
   - MongoDB: localhost:27017
   - Redis: localhost:6379

### Local Development

#### Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend (Next.js)

```bash
cd frontend
npm install
npm run dev
```

## 📊 Data Models

### Key Entities

#### Upload Entity (PostgreSQL)

```java
@Entity
public class Upload {
    UUID id, userId, deckId;
    String fileName, fileUrl;
    FileType fileType;
    Long fileSize;
    ParsingStatus parsingStatus; // PENDING, PROCESSING, COMPLETED, FAILED
    LocalDateTime parsingStartedAt, parsingCompletedAt;
    String errorMessage;

    // Helper methods for parsing workflow
    void startParsing();
    void completeParsing();
    void failParsing(String error);
}
```

#### DeckAnalytics (MongoDB)

```java
@Document
public class DeckAnalytics {
    UUID deckId, userId;
    int highestScore, previousScore;
    List<ReviewSession> reviewSessions;
    boolean proficiency;

    // Analytics methods
    double getAverageScore();
    void addReviewSession(ReviewSession session);
}
```

## 🔧 Configuration

### Environment Variables

#### Docker Development Configuration

All configuration is handled through Docker environment variables in `docker-compose.yml`:

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/studydb
    SPRING_DATASOURCE_USERNAME: user
    SPRING_DATASOURCE_PASSWORD: password
    MONGO_URI: mongodb://mongo:27017/studylogs
    REDIS_HOST: redis

frontend:
  environment:
    NEXT_PUBLIC_API_URL: http://localhost:8080
```

#### Application Properties

Minimal configuration in `application.properties`:

```properties
spring.application.name=backend
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

#### Production Deployment

- Use strong, unique passwords for all services
- Configure cloud storage (AWS S3, Google Cloud Storage)
- Enable Redis authentication
- Set up proper CORS policies
- Use environment-specific configuration files

> ⚠️ **Security**: The current `docker-compose.yml` uses default credentials safe only for local development.

## 🏃‍♂️ Development Workflow

### File Upload & Parsing Flow

1. User uploads file → `Upload` entity created with `PENDING` status
2. Background service picks up upload → status changes to `PROCESSING`
3. AI parser extracts content and creates `Card` entities
4. Upload status updates to `COMPLETED` or `FAILED`
5. Analytics updated in MongoDB for tracking

### Study Session Flow

1. User selects deck for review
2. Session results stored in `ReviewSession` (MongoDB)
3. `DeckAnalytics` updated with performance metrics

## 🛠️ Built With

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Next.js](https://nextjs.org/) - React framework for frontend
- [PostgreSQL](https://www.postgresql.org/) - Primary database
- [MongoDB](https://www.mongodb.com/) - Analytics database
- [Redis](https://redis.io/) - Caching layer
- [Docker](https://www.docker.com/) - Containerization
- [Tailwind CSS](https://tailwindcss.com/) - Styling

## 📈 Current Status

**Phase**: Backend Complete - Frontend Development

### Completed ✅

- ✅ **Backend Architecture**: Complete Spring Boot application with multi-database setup
- ✅ **Authentication & Security**: BCrypt password hashing, Spring Security configuration
- ✅ **Database Models**: PostgreSQL entities, MongoDB analytics, Redis caching
- ✅ **REST API**: Full CRUD operations across all controllers
- ✅ **Service Layer**: Complete business logic with comprehensive error handling
- ✅ **Exception Handling**: Global exception handler with custom exceptions
- ✅ **Testing**: Comprehensive integration tests with 100% pass rate
- ✅ **Docker Setup**: Multi-service containerization with health checks
- ✅ **Data Transfer Objects**: Complete DTO layer with validation

### In Progress 🚧

- 🚧 **Frontend Development**: Next.js application with React 19
- 🚧 **API Integration**: Frontend-backend connectivity
- 🚧 **UI Components**: Modern React components with Tailwind CSS

### Planned 📋

- 📋 **AI Integration**: Intelligent flashcard generation from uploads
- 📋 **Advanced Analytics**: Enhanced study progress tracking
- 📋 **Spaced Repetition**: Smart review scheduling algorithms
- 📋 **Mobile Optimization**: Responsive design and PWA features

## 🤝 Contributing

This is currently a personal project in development. Future contribution guidelines will be added as the project matures.

## 📄 License

This project is currently in development and all rights are reserved. Future licensing decisions will be made as the project matures.

**Developer**: Luke Sauls  
**Contact**: [GitHub](https://github.com/lukesauls66)  
**Project Repository**: [study-companion](https://github.com/lukesauls66/study-companion)
