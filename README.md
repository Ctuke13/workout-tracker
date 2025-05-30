# Workout Tracker Application

A comprehensive fitness tracking web application built with Java Spring Boot and PostgreSQL, designed to help users monitor their workout progress and fitness improvements over time.

## 🎯 Project Vision

This application enables users to:
- **Secure Login** - User authentication with persistent sessions
- **Browse Workouts** - Search and explore exercises with visual images  
- **Track Sessions** - Click workout images to start logging sessions
- **Record Performance** - Input sets, reps, weights for strength training
- **Log Cardio** - Track time, distance, and calories for cardio exercises
- **Monitor Progress** - View graphs showing improvement day-to-day
- **Data Persistence** - All workout data saved and accessible between sessions

## 🛠️ Technology Stack

- **Backend:** Java Spring Boot 3.x
- **Database:** PostgreSQL
- **Security:** Spring Security with JWT Authentication
- **API:** RESTful architecture with comprehensive endpoints
- **Build Tool:** Maven
- **Frontend:** HTML/CSS/JavaScript *(planned)*

## 📊 Development Status

### ✅ **Backend Complete (95%)**

**🔐 Authentication & Security**
- [x] User registration and login system
- [x] JWT token-based authentication
- [x] Secure password handling with BCrypt
- [x] Role-based access control

**💪 Workout Management**
- [x] Full CRUD operations for workout exercises
- [x] Category-based organization (Chest, Legs, Cardio, etc.)
- [x] Search functionality by name and category
- [x] Support for both strength and cardio workouts
- [x] Image URL support for workout visualization

**📝 Workout Session Tracking**
- [x] Start and manage workout sessions
- [x] Historical workout data retrieval
- [x] Recent workouts with pagination
- [x] User-specific workout statistics

**📈 Performance Analytics**
- [x] Detailed set/rep/weight recording for strength training
- [x] Time/distance/calories tracking for cardio
- [x] Progress analytics (max weight, total volume)
- [x] Date-range performance analysis
- [x] Personal record tracking

**🔒 Security & Data Integrity**
- [x] All endpoints secured with JWT authentication
- [x] User ownership verification on all operations
- [x] Input validation and comprehensive error handling
- [x] Comprehensive API testing suite

### 🚧 **In Progress**
- [ ] PostgreSQL database configuration
- [ ] Frontend user interface development
- [ ] Progress visualization charts
- [ ] End-to-end integration testing

### 📋 **Next Steps**
1. **Database Setup** - Configure PostgreSQL connection and schema
2. **Frontend Development** - Create responsive user interface
3. **Progress Charts** - Implement data visualization
4. **Deployment** - Production-ready configuration

## 🏗️ Architecture Overview

```
📦 Backend Architecture
├── 🎮 Controllers
│   ├── AuthController - User authentication
│   ├── WorkoutController - Exercise management
│   ├── WorkoutLogController - Session tracking
│   └── PerformanceController - Performance analytics
├── 🔧 Services
│   ├── UserService - User management logic
│   ├── WorkoutService - Workout business logic
│   ├── WorkoutLogService - Session management
│   └── PerformanceService - Analytics and tracking
├── 📊 Models/Entities
│   ├── User - User account information
│   ├── Workout - Exercise definitions
│   ├── WorkoutLog - Individual workout sessions
│   └── PerformanceRecord - Detailed performance data
├── 🗄️ Repositories
│   └── JPA repositories with custom queries
└── 🔐 Security
    ├── JWT token provider and validation
    ├── Spring Security configuration
    └── Custom authentication filters
```

## 🚀 API Endpoints

### 🔐 Authentication
```
POST /api/auth/register    # Register new user
POST /api/auth/login       # User login (returns JWT)
```

### 💪 Workouts
```
GET    /api/workouts                  # Get all workouts
POST   /api/workouts                  # Create new workout
GET    /api/workouts/{id}            # Get workout by ID
PUT    /api/workouts/{id}            # Update workout
DELETE /api/workouts/{id}            # Delete workout
GET    /api/workouts/search          # Search workouts by name
GET    /api/workouts/category/{cat}  # Get workouts by category
GET    /api/workouts/type            # Filter by cardio/strength
```

### 📝 Workout Sessions
```
POST   /api/workout-logs           # Start new workout session
GET    /api/workout-logs           # Get workout history
GET    /api/workout-logs/recent    # Get recent workouts
GET    /api/workout-logs/{id}      # Get specific workout session
PUT    /api/workout-logs/{id}      # Update workout session
DELETE /api/workout-logs/{id}      # Delete workout session
GET    /api/workout-logs/stats     # Get workout statistics
```

### 📈 Performance Tracking
```
POST   /api/performance                           # Create performance record
GET    /api/performance/workout-log/{id}          # Get performance by session
GET    /api/performance/workout/{id}              # Get performance history
PUT    /api/performance/{id}                      # Update performance record
DELETE /api/performance/{id}                      # Delete performance record
GET    /api/performance/analytics/max-weight/{id} # Get max weight for exercise
GET    /api/performance/analytics/volume          # Get total volume by date range
```

## 💻 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd workout-tracker
```

2. **Configure database** (application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/workout_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

4. **Test the API**
- Use the provided `api-test.http` file for comprehensive endpoint testing
- API will be available at `http://localhost:8080`

## 🧪 Testing

The project includes comprehensive API tests in `src/main/resources/api-test.http` covering:
- Authentication flows (registration, login, token validation)
- All CRUD operations for workouts, sessions, and performance
- Error scenarios and validation testing
- Security testing with invalid tokens

## 🎯 Portfolio Highlights

This project demonstrates:
- **Enterprise Java Development** - Spring Boot, Spring Security, JPA
- **Database Design** - Proper entity relationships and constraints
- **RESTful API Design** - Following industry best practices
- **Security Implementation** - JWT authentication and authorization
- **Scalable Architecture** - Service layer pattern and dependency injection
- **Comprehensive Testing** - Full API coverage with realistic scenarios

Perfect showcase for full-stack developer positions requiring Java expertise!

## 📄 License

This project is for educational and portfolio purposes.