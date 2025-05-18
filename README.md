# Workout Progress Tracker

A comprehensive web application for tracking and visualizing workout progress, built with Spring Boot and PostgreSQL. Users can log their workouts, track performance metrics, and visualize their fitness journey through interactive charts and graphs.

## 🚀 Features

- **User Authentication** - Secure user registration and login
- **Exercise Library** - Pre-defined workouts with categories and images
- **Workout Logging** - Track daily workout sessions
- **Performance Tracking** - Record sets, reps, weights for strength training
- **Cardio Monitoring** - Track duration, distance, and calories for cardio exercises
- **Progress Visualization** - Generate graphs showing improvement over time
- **Flexible Exercise Support** - Handle both strength training and cardio workouts

## 🛠️ Tech Stack

### Backend
- **Java 17+** - Programming language
- **Spring Boot 3.4.5** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication & authorization
- **PostgreSQL 16** - Primary database
- **Docker** - Containerization
- **Maven** - Dependency management
- **Lombok** - Code generation

### Planned Frontend
- **React.js** - User interface
- **Chart.js** - Data visualization
- **Axios** - HTTP client

## 📋 Prerequisites

- **Java 17+** installed
- **Docker** and Docker Desktop running
- **Maven** (or use included wrapper)
- **Git** for version control

## ⚡ Quick Start

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd workout-tracker
```

### 2. Start PostgreSQL Database
```bash# Start PostgreSQL container
docker run -d --name workout-tracker-db \
  -e POSTGRES_DB=workout_tracker \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16
 ```

### 3. Run the Application
```bash# Using Maven wrapper (recommended)
./mvnw spring-boot:run


# Or using Maven directly
mvn spring-boot:run
```

### 4. Verify Installation

- API Status: http://localhost:8080/api/test/status
- Create sample data: POST http://localhost:8080/api/test/create-sample-data

### 📁 Project Structure

```
src/
├── main/
│   ├── java/com/chidituke/workout_tracker/
│   │   ├── model/              # Entity models
│   │   │   ├── User.java
│   │   │   ├── Workout.java
│   │   │   ├── WorkoutLog.java
│   │   │   └── PerformanceRecord.java
│   │   ├── repository/         # Data access layer
│   │   ├── service/            # Business logic (planned)
│   │   ├── controller/         # REST controllers
│   │   │   └── TestController.java
│   │   └── WorkoutTrackerApplication.java
│   └── resources/
│       └── application.properties
├── docker-compose.yml          # Database container config
└── pom.xml                     # Maven dependencies
```

### 🗄️ Database Schema
**Users Table**
- id - Primary key
- username - Unique username
- password - Encrypted password
- email - Unique email
- created_at - Registration timestamp

**Workouts Table** (Exercise Library)
- id - Primary key
- name - Exercise name (e.g., "Bench Press")
- description - Exercise details
- category - Exercise category (e.g., "Chest", "Cardio")
- image_url - Exercise image path
- is_cardio - Boolean flag for cardio exercises

**Workout Logs Table** (Daily Sessions)
- id - Primary key
- user_id - Foreign key to users
- workout_id - Foreign key to workouts
- date - Session date
- notes - Optional session notes

**Performance Records Table** (Individual Sets/Sessions)
- id - Primary key
- workout_log_id - Foreign key to workout_logs
- set_number - Set number (for strength) or session number (for cardio)
- reps - Repetitions (strength training)
- weight - Weight used (strength training)
- duration_minutes - Exercise duration (cardio)
- duration_seconds - Precise timing (sprints, intervals)
- distance_km - Distance covered (cardio)
- calories_burned - Estimated calories
- notes - Performance notes

### 🔌 API Endpoints (Current)
**Test Endpoints**
- GET /api/test/status - API health check
- GET /api/test/users - List all users
- POST /api/test/user - Create new user
- GET /api/test/workouts - List all workouts
- POST /api/test/workout - Create new workout
- POST /api/test/create-sample-data - Generate test data

### 🚧 Upcoming Features

 User authentication with JWT tokens
 Workout search and filtering
 Progress analytics and insights
 React.js frontend application
 Exercise image gallery
 Social sharing capabilities
 Export functionality (PDF, CSV)
 Mobile responsive design
 Workout recommendations
 Achievements and gamification

### 🧪 Testing
```bash# Run all tests
./mvnw test

# Check database connection
docker exec -it workout-tracker-db psql -U postgres -d workout_tracker
```

### 🐳 Docker Commands
```bash# Start database
docker run -d --name workout-tracker-db \
  -e POSTGRES_DB=workout_tracker \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16


# View running containers
docker ps

# Stop database
docker stop workout-tracker-db

# Remove container
docker rm workout-tracker-db

# View database logs
docker logs workout-tracker-db
```

### 📊 Data Model Relationships
```
User (1) ←→ (Many) WorkoutLog ←→ (Many) Workout
                     ↓
              (Many) PerformanceRecord
```

**Example Flow:**

1. User "John" logs in
2. Selects "Bench Press" from workout library
3. Creates a WorkoutLog for today's session
4. Records multiple PerformanceRecords (Set 1: 10 reps @ 135lbs, Set 2: 8 reps @ 135lbs)
5. Data is stored and available for progress tracking

### 🤝 Contributing
This is a portfolio project by Chidi Tuke. Suggestions and feedback are welcome!

### 📝 License
This project is for educational and portfolio purposes.

### 📧 Contact
**Chidi Tuke**

**GitHub:** https://github.com/Ctuke13
**Email:** chidituke@gmail.com
**LinkedIn:** https://www.linkedin.com/in/ctuke/


Last Updated: May 2025