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
- **Backend**: Spring Boot 3.5.7 with Java 17-
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
  deckId, userId, scores, sessions, proficiency;
}

ReviewSessions: {
  date, deckName, score, cardsReviewed, correctAnswers;
}
```

## 📁 Project Structure

```
study-companion/
├── docker-compose.yml          # Multi-service orchestration
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/lukesauls/studycompanion/
│   │   └── studycompanion_backend/
│   │       ├── model/
│   │       │   ├── postgres/   # JPA entities (User, Deck, Card, Upload)
│   │       │   ├── mongo/      # MongoDB documents (Analytics, Sessions)
│   │       │   └── enums/      # Shared enums (FileType, ParsingStatus, etc.)
│   │       └── repository/     # Data access layers
│   └── pom.xml                # Maven dependencies
└── frontend/                  # Next.js application
    ├── app/                   # App router pages
    ├── lib/                   # API utilities
    └── package.json          # Node dependencies
```

## 🚦 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17+ (for local development)
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

#### Required Configuration

Copy `.env.example` to `.env` and configure with your values:

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/studydb
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password
MONGO_URI=mongodb://localhost:27017/studylogs
REDIS_HOST=localhost

# Frontend Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080
```

#### Docker Development

The `docker-compose.yml` uses default development credentials that are safe for local development only.

#### Production Deployment

- Use strong, unique passwords for all services
- Generate secure JWT secrets (min 256-bit)
- Configure cloud storage (AWS S3, Google Cloud Storage)
- Enable Redis authentication
- Set up proper CORS policies

> ⚠️ **Security**: Never commit `.env` files or production secrets to version control. The `.env` file is already in `.gitignore`.

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

**Phase**: Active Development

### Completed ✅

- Multi-database architecture setup
- Core PostgreSQL entities (User, Deck, Card, Upload)
- MongoDB analytics models (DeckAnalytics, ReviewSession)
- Docker containerization
- Enhanced Upload entity with parsing status tracking

### In Progress 🚧

- File parsing service implementation
- REST API controllers
- Frontend components and pages
- Authentication system

### Planned 📋

- AI integration for content parsing
- Spaced repetition algorithms
- Advanced analytics dashboard
- Mobile responsiveness
- Performance optimizations

## 🤝 Contributing

This is currently a personal project in development. Future contribution guidelines will be added as the project matures.

## 📄 License

This project is currently in development and all rights are reserved. Future licensing decisions will be made as the project matures.

**Developer**: Luke Sauls  
**Contact**: [GitHub](https://github.com/lukesauls66)  
**Project Repository**: [study-companion](https://github.com/lukesauls66/study-companion)
