# 🏋️‍♂️ Workout Tracker v2

A comprehensive, enterprise-grade fitness tracking platform built with Spring Boot and React TypeScript. This full-stack application provides advanced workout management, performance analytics, gamification, and social features for fitness enthusiasts and professional trainers.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)

## 🎯 Project Vision

Workout Tracker v2 is a complete fitness ecosystem that enables users to:
- 📅 **Schedule & Track Workouts** - Plan workouts with advanced calendar integration
- 💪 **200+ Exercise Library** - Comprehensive exercise database with filtering and search
- 📊 **Performance Analytics** - Track progress with detailed metrics and visualizations
- 🎮 **Gamification** - Achievements, ranks, seasons, and leaderboards
- 🏆 **Progress Tracking** - XP, ranks, streaks, and personal records
- 👥 **Social Features** - Share workouts, follow users, and interact with the community
- 💬 **Messaging System** - Direct and group conversations for trainer-client communication
- 💳 **Subscription Tiers** - Feature-gated multi-tier subscription system
- 🎯 **Professional Features** - Tools for trainers to manage clients and assign workouts

## ✨ Key Features

### 🏋️ Workout Management
- **Smart Calendar** - Visual workout scheduling with conflict detection
- **Workout Plans** - Create custom workout plans with multiple exercises
- **Workout Programs** - Multi-week programs with structured progression
- **Exercise Configuration** - Support for strength, cardio, and isometric exercises
- **Superset Support** - Group exercises for advanced training techniques
- **Rest Timers** - Built-in rest timer between sets
- **Real-time Tracking** - Track sets, reps, weight, time, and distance during workouts

### 📊 Analytics & Performance
- **Performance Dashboard** - Comprehensive analytics with charts and graphs
- **Personal Records** - Automatic PR tracking for all exercises
- **Progress Tracking** - Historical data with trend analysis
- **Workout Statistics** - Volume, duration, calories, and frequency metrics
- **Exercise Analytics** - Most performed exercises and performance trends
- **Performance Metrics** - Detailed tracking of strength and endurance improvements

### 🎮 Gamification System
- **Achievement System** - 40+ achievements across multiple categories
- **Rank Progression** - 7 ranks from Novice to Legendary
- **XP System** - Earn XP for completing workouts and achievements
- **Seasonal Leaderboards** - Compete with others in time-limited seasons
- **Streaks** - Track workout consistency with daily streaks
- **Tier System** - Visual tier progression with celebrations
- **Season History** - Track your performance across multiple seasons

### 👥 Social & Community
- **User Profiles** - Customizable profiles with stats and achievements
- **Social Feed** - Share workouts, progress, and achievements
- **Comments & Likes** - Interact with community posts
- **Follow System** - Follow friends and fitness influencers
- **Workout Sharing** - Share workout plans with the community
- **User Search** - Find and connect with other users

### 💬 Messaging & Communication
- **Direct Messaging** - One-on-one conversations
- **Group Conversations** - Create group chats for teams
- **Workout Assignments** - Trainers can assign workouts to clients
- **Progress Check-ins** - Share workout progress with trainers
- **Media Messages** - Share images and files
- **Unread Tracking** - Never miss important messages

### 🎓 Professional Features
- **Professional Profiles** - Verified trainer profiles with credentials
- **Client Management** - Manage multiple clients and their programs
- **Workout Assignment** - Assign custom workouts to clients
- **Progress Monitoring** - Track client progress and performance
- **Subscription Management** - Multi-tier subscription system (Free, Premium, Professional)
- **Feature Gating** - Tier-based access control

### 💪 Exercise Library
- **200+ Exercises** - Pre-loaded comprehensive exercise database
- **Advanced Filtering** - Filter by category, equipment, muscle group, difficulty
- **Custom Exercises** - Users can create and save custom exercises
- **Exercise Ratings** - Rate and review exercises
- **Favorites System** - Save favorite exercises for quick access
- **Exercise History** - Track usage and performance for each exercise
- **Admin Controls** - Moderation and approval for user-created exercises

## 🛠️ Technology Stack

### Backend
- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** PostgreSQL 15
- **Security:** Spring Security + JWT Authentication
- **ORM:** Spring Data JPA + Hibernate
- **Migration:** Flyway
- **Caching:** Redis (optional)
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito

### Frontend
- **Framework:** React 18
- **Language:** TypeScript 5
- **UI Library:** shadcn/ui (Radix UI primitives)
- **Styling:** Tailwind CSS
- **State Management:** React Context + Hooks
- **HTTP Client:** Axios
- **Routing:** React Router v6
- **Charts:** Recharts
- **Build Tool:** Create React App / Vite

### DevOps & Infrastructure
- **Containerization:** Docker
- **Orchestration:** Docker Compose
- **Database:** PostgreSQL with pgAdmin
- **API Testing:** REST Client (VS Code)

## 📊 Project Statistics

- **Backend:** 341 Java files
- **Frontend:** 153 TypeScript/JavaScript files
- **Controllers:** 26 REST controllers
- **Services:** 38 business logic services
- **Models:** 46 domain entities
- **Repositories:** 30 data access repositories
- **DTOs:** 140+ request/response objects
- **Migrations:** 17 SQL migration files
- **Components:** 80+ React components
- **Pages:** 16 application pages
- **API Endpoints:** 100+ RESTful endpoints

## 🏗️ Architecture Overview

### Backend Architecture (Spring Boot)

```
📦 Backend
├── 🎮 Controllers (26 files)
│   ├── Analytics (2) - Analytics & Performance Tracking
│   ├── Auth (1) - Authentication & Authorization
│   ├── Exercise (5) - Exercise Management
│   ├── Messaging (2) - Conversations & Messages
│   ├── Progress (1) - Gamification & Achievements
│   ├── Social (1) - Social Feed & Interactions
│   ├── User (4) - User Management & Preferences
│   └── Workout (9) - Workout Plans, Programs & Sessions
│
├── 🔧 Services (38 files)
│   ├── Analytics (2) - Performance Analytics & Metrics
│   ├── Exercise (5) - Exercise CRUD & Search
│   ├── Messaging (2) - Messaging & Conversations
│   ├── Progress (5) - Achievements, Leaderboards, Seasons
│   ├── Scheduled Workouts (5) - Calendar & Scheduling
│   ├── Social (4) - Posts, Comments, Likes
│   ├── User (8) - User Management & Profiles
│   └── Workout (7) - Workout Plans, Programs, Sessions
│
├── 🏗️ Models (46 files)
│   ├── User Domain (7) - Users, Subscriptions, Profiles
│   ├── Workout Domain (14) - Exercises, Plans, Programs
│   ├── Progress Domain (10) - Achievements, Ranks, Seasons
│   ├── Social Domain (5) - Posts, Comments, Likes
│   ├── Messaging Domain (7) - Conversations, Messages
│   └── Analytics Domain (1) - Performance Metrics
│
├── 🗄️ Repositories (30 files)
│   └── Spring Data JPA repositories with custom queries
│
├── 📤 DTOs (140+ files)
│   ├── Request DTOs (67) - API request objects
│   └── Response DTOs (73+) - API response objects
│
├── ❌ Exceptions (50 files)
│   └── Custom exceptions with global error handling
│
├── 🗺️ Mappers (13 files)
│   └── Entity-DTO transformation layer
│
├── 🔒 Security (6 files)
│   ├── JWT token provider & validation
│   ├── Spring Security configuration
│   └── Custom authentication filters
│
└── 🛠️ Utils (3 files)
    └── Helper utilities and validators
```

### Frontend Architecture (React + TypeScript)

```
📦 Frontend
├── 📄 Pages (16 files)
│   ├── LandingPage - Marketing & feature showcase
│   ├── LoginPage / RegisterPage - Authentication
│   ├── CalendarPage - Workout scheduling & calendar
│   ├── ExercisesPage - Exercise library & search
│   ├── WorkoutModePage - Active workout tracking
│   ├── ProgressPage - Achievements & leaderboards
│   ├── AnalyticsPage - Performance analytics & charts
│   ├── AchievementsPage - Achievement gallery
│   ├── CommunityPage - Social feed
│   ├── MessagesPage - Messaging & conversations
│   ├── SettingsPage - User preferences
│   └── BillingPage - Subscription management
│
├── 🧩 Components (80+ files)
│   ├── AnalyticsPage (7) - Charts, stats, records
│   ├── CalendarPage (14) - Workout display & config
│   ├── WorkoutModePage (9) - Exercise trackers
│   ├── ProgressPage (7) - Achievements & leaderboard
│   ├── ExercisePage (4) - Filters & exercise cards
│   ├── Gamification (2) - Progress widgets
│   ├── LandingPage (9) - Marketing components
│   ├── Layout (7) - Navigation & modals
│   └── UI (9) - Reusable design system components
│
├── 🪝 Hooks (11 files)
│   ├── Calendar hooks - Data & actions
│   ├── Exercise hooks - Config, filters, selection
│   ├── Workout hooks - Mode, analysis, events
│   └── UI hooks - Modal state management
│
├── 🔌 Services (11 files)
│   ├── API clients for all backend endpoints
│   ├── Authentication service
│   └── Data transformers
│
├── 📝 Types (7 files)
│   └── TypeScript type definitions
│
├── 🔄 Contexts (2 files)
│   ├── AuthContext - Authentication state
│   └── WorkoutContext - Workout state
│
└── 🛠️ Utils (6 files)
    └── Date, formatting, validation utilities
```

### Database Schema

```
🗄️ PostgreSQL Database (17 migrations)
├── Core System
│   ├── users - User accounts & authentication
│   ├── subscriptions - Subscription management
│   └── professional_profiles - Trainer profiles
│
├── Exercise System
│   ├── exercises - Exercise library
│   ├── fitness_goals - Goal definitions
│   ├── user_exercise_favorites - Favorite exercises
│   └── user_exercise_ratings - Exercise ratings
│
├── Workout System
│   ├── workout_plans - Workout plan definitions
│   ├── workout_programs - Multi-week programs
│   ├── workout_sessions - Completed workouts
│   ├── scheduled_workouts - Calendar entries
│   ├── plan_exercises - Exercise configurations
│   └── performance_records - Performance tracking
│
├── Gamification System
│   ├── user_progression - XP, ranks, levels
│   ├── achievements - Achievement definitions
│   ├── user_achievements - User achievement progress
│   ├── seasons - Seasonal competitions
│   ├── season_history - Historical season data
│   └── leaderboard_entries - Leaderboard rankings
│
├── Social System
│   ├── social_posts - User posts
│   ├── social_comments - Post comments
│   ├── post_likes - Like tracking
│   └── user_relationships - Follow system
│
└── Messaging System
    ├── conversations - Conversation threads
    ├── conversation_participants - Participant tracking
    └── messages - Message content
```

## 🚀 API Endpoints

### 🔐 Authentication
```
POST   /api/auth/register              # Register new user
POST   /api/auth/login                 # Login (returns JWT)
GET    /api/auth/me                    # Get current user
POST   /api/auth/refresh               # Refresh JWT token
```

### 💪 Exercise Management
```
GET    /api/exercises                  # Get all exercises (paginated)
GET    /api/exercises/{id}             # Get exercise by ID
POST   /api/exercises                  # Create custom exercise
PUT    /api/exercises/{id}             # Update exercise
DELETE /api/exercises/{id}             # Delete exercise
GET    /api/exercises/search           # Search exercises
GET    /api/exercises/favorites        # Get user favorites
POST   /api/exercises/{id}/favorite    # Add to favorites
POST   /api/exercises/{id}/rate        # Rate exercise
GET    /api/exercises/{id}/analytics   # Exercise analytics
```

### 📅 Workout Planning
```
GET    /api/workout-plans              # Get user workout plans
POST   /api/workout-plans              # Create workout plan
GET    /api/workout-plans/{id}         # Get plan details
PUT    /api/workout-plans/{id}         # Update workout plan
DELETE /api/workout-plans/{id}         # Delete workout plan
POST   /api/workout-plans/{id}/schedule # Schedule workout
GET    /api/workout-plans/{id}/analytics # Plan analytics
```

### 🏋️ Workout Programs
```
GET    /api/programs                   # Get workout programs
POST   /api/programs                   # Create program
GET    /api/programs/{id}              # Get program details
PUT    /api/programs/{id}              # Update program
DELETE /api/programs/{id}              # Delete program
POST   /api/programs/{id}/enroll       # Enroll in program
GET    /api/programs/{id}/progress     # Get program progress
```

### 📊 Workout Sessions
```
GET    /api/workout-sessions           # Get user sessions
POST   /api/workout-sessions           # Create workout session
GET    /api/workout-sessions/{id}      # Get session details
PUT    /api/workout-sessions/{id}      # Update session
DELETE /api/workout-sessions/{id}      # Delete session
POST   /api/workout-sessions/{id}/complete # Complete workout
GET    /api/workout-sessions/analytics # Session analytics
```

### 📈 Performance Tracking
```
POST   /api/performance                # Record performance
GET    /api/performance/{id}           # Get performance record
PUT    /api/performance/{id}           # Update performance
DELETE /api/performance/{id}           # Delete record
GET    /api/performance/exercise/{id}  # Exercise performance history
GET    /api/performance/personal-records # Get all PRs
GET    /api/performance/analytics      # Performance analytics
```

### 📊 Analytics
```
GET    /api/analytics/overview         # Analytics overview
GET    /api/analytics/performance-tracker # Performance metrics
GET    /api/analytics/workout-breakdown # Workout type breakdown
GET    /api/analytics/top-exercises    # Most performed exercises
GET    /api/analytics/personal-records # Personal records
GET    /api/analytics/progress         # Progress over time
```

### 🎮 Gamification & Progress
```
GET    /api/progress/user              # Get user progression
GET    /api/progress/achievements      # Get achievements
GET    /api/progress/leaderboard       # Get leaderboard
GET    /api/progress/season            # Get current season
GET    /api/progress/season/history    # Season history
POST   /api/progress/update            # Update progression
GET    /api/progress/stats             # User stats
```

### 👥 Social Features
```
GET    /api/social/feed                # Get social feed
POST   /api/social/posts               # Create post
GET    /api/social/posts/{id}          # Get post
DELETE /api/social/posts/{id}          # Delete post
POST   /api/social/posts/{id}/like     # Like post
POST   /api/social/posts/{id}/comment  # Comment on post
GET    /api/social/users/{id}/profile  # Get user profile
POST   /api/social/users/{id}/follow   # Follow user
```

### 💬 Messaging
```
GET    /api/conversations              # Get user conversations
POST   /api/conversations              # Create conversation
GET    /api/conversations/{id}         # Get conversation
DELETE /api/conversations/{id}         # Delete conversation
POST   /api/conversations/{id}/messages # Send message
GET    /api/messages                   # Get messages
PUT    /api/messages/{id}              # Edit message
DELETE /api/messages/{id}              # Delete message
```

### 👤 User Management
```
GET    /api/users/profile              # Get user profile
PUT    /api/users/profile              # Update profile
GET    /api/users/preferences          # Get preferences
PUT    /api/users/preferences          # Update preferences
GET    /api/users/subscription         # Get subscription
POST   /api/users/subscription         # Upgrade subscription
GET    /api/users/search               # Search users
```

### 📅 Scheduled Workouts
```
GET    /api/scheduled-workouts         # Get scheduled workouts
POST   /api/scheduled-workouts         # Schedule workout
GET    /api/scheduled-workouts/{id}    # Get scheduled workout
PUT    /api/scheduled-workouts/{id}    # Update scheduled workout
DELETE /api/scheduled-workouts/{id}    # Delete scheduled workout
GET    /api/scheduled-workouts/calendar # Get calendar view
POST   /api/scheduled-workouts/{id}/start # Start workout
```

## 💻 Getting Started

### Prerequisites
```bash
# Backend
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

# Frontend
- Node.js 18+
- npm or yarn
```

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/workout-tracker.git
cd workout-tracker
```

#### 2. Backend Setup

##### Using Docker Compose (Recommended)
```bash
cd backend
docker-compose up -d
```

##### Manual Setup
```bash
# Configure database
createdb workout_tracker

# Update application.properties
cd backend/src/main/resources
cp application.properties.example application.properties
# Edit application.properties with your database credentials

# Build and run
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

**Backend Configuration (application.properties):**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/workout_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key-change-in-production
jwt.expiration=86400000

# Server
server.port=8080
```

#### 3. Frontend Setup
```bash
cd frontend
npm install
npm start
```

**Frontend Configuration (.env):**
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_JWT_SECRET=your-secret-key
```

#### 4. Access the Application
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **API Documentation:** http://localhost:8080/swagger-ui.html (if enabled)

### Default Users
```
Admin User:
Email: admin@workout.com
Password: admin123

Test User:
Email: user@workout.com
Password: user123
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### API Testing
Use the provided HTTP test files in `api-tests/`:
```
api-tests/
├── auth-tests.http              # Authentication tests
├── exercise-library-tests.http  # Exercise management tests
├── workout-tests.http           # Workout operations tests
├── performance-tests.http       # Performance tracking tests
└── subscription-tests.http      # Subscription tests
```

### Frontend Tests
```bash
cd frontend
npm test
npm run test:coverage
```

## 📦 Deployment

### Docker Deployment
```bash
# Build images
docker-compose build

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### Production Build
```bash
# Backend
cd backend
./mvnw clean package -DskipTests
java -jar target/workout-tracker-0.0.1-SNAPSHOT.jar

# Frontend
cd frontend
npm run build
# Deploy build/ directory to your hosting service
```

## 🗺️ Roadmap

### Phase 1: Core Improvements (Current)
- [x] Advanced analytics dashboard
- [x] Gamification system
- [x] Social features
- [x] Messaging system
- [ ] Mobile responsive design improvements
- [ ] PWA support

### Phase 2: Enhanced Features (Q1 2026)
- [ ] Workout video tutorials
- [ ] Exercise form checking (AI)
- [ ] Nutrition tracking
- [ ] Meal planning
- [ ] Integration with fitness devices
- [ ] Mobile app (React Native)

### Phase 3: Advanced Features (Q2 2026)
- [ ] AI workout recommendations
- [ ] Virtual personal trainer
- [ ] Live workout streaming
- [ ] Group workout challenges
- [ ] Marketplace for workout programs
- [ ] Advanced analytics with ML

### Phase 4: Enterprise Features (Q3 2026)
- [ ] Gym management system
- [ ] Multi-gym support
- [ ] Equipment tracking
- [ ] Class scheduling
- [ ] Payment processing
- [ ] White-label solution

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards
- Follow Java code conventions for backend
- Use ESLint/Prettier for frontend
- Write unit tests for new features
- Update documentation as needed

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Chidi Tuke** - *Initial work* - [GitHub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Spring Boot framework and community
- React and TypeScript communities
- shadcn/ui for the beautiful component library
- All contributors and testers

## 📞 Support

For support, email support@workouttracker.com or open an issue on GitHub.

## 🌟 Show Your Support

Give a ⭐️ if this project helped you!

---

**Built with ❤️ by fitness enthusiasts, for fitness enthusiasts**
