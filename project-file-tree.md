# 🏋️ Workout Tracker Application - Complete File Tree

## 📊 Project Statistics

| Category | Count | Description |
|----------|-------|-------------|
| **Java Files** | 265 | Backend source code and tests |
| **TypeScript/TSX** | 72 | Frontend React components and logic |
| **JavaScript/JSX** | 9 | Frontend utilities and configs |
| **SQL Migrations** | 25 | Database schema and data files |
| **Test Files** | 14 | Backend unit and integration tests |
| **Config Files** | 20 | Application and build configuration |
| **HTTP Tests** | 7 | API testing files |

## 🏗️ Architecture Overview

**Backend**: Spring Boot REST API with PostgreSQL database
**Frontend**: React TypeScript SPA with Tailwind CSS
**Database**: PostgreSQL with Flyway migrations
**Authentication**: JWT-based security
**Build Tools**: Maven (Backend), npm (Frontend)

---

## 📁 Complete Project Structure

```
🏋️ workout-tracker/
├── 📋 CURRENT-FILE-TREE.md
├── 📖 README.md
├── 🗃️ backend-workout-modules-diagram.md
├── 📦 package.json                    # Root package.json
├── 🔒 package-lock.json               # Root lockfile
├── 📋 project-file-tree.md            # This file
├── ⚙️ .gitattributes                  # Git attributes config
├── 🚫 .gitignore                      # Git ignore rules
│
├── 🧪 api-tests/                      # API Testing Files
│   ├── 🔐 auth-tests.http
│   ├── 💪 exercise-library-tests.http
│   ├── 📈 performance-tests.http
│   ├── 💳 subscription-tests.http
│   └── 🏋️ workout-tests.http
│
├── 🔧 .claude/                        # Claude Code Configuration
│   └── ⚙️ settings.local.json
│
├── 🖥️ backend/                        # Spring Boot Backend
│   ├── 🐳 docker-compose.yml          # Development database
│   ├── 🚨 error-details.txt           # Error logs
│   ├── 🗃️ init-permissions.sql        # Database permissions
│   ├── ⚙️ mvnw                        # Maven wrapper (Unix)
│   ├── ⚙️ mvnw.cmd                    # Maven wrapper (Windows)
│   ├── 📋 pom.xml                     # Maven configuration
│   ├── 🎯 test-output.txt             # Test results
│   │
│   ├── 🔧 .mvn/                       # Maven Wrapper
│   │   └── wrapper/
│   │       └── maven-wrapper.properties
│   │
│   └── 📁 src/
│       ├── 🏗️ main/
│       │   ├── ☕ java/com/chidituke/workout_tracker/
│       │   │   ├── 🚀 WorkoutTrackerApplication.java    # Main application
│       │   │   │
│       │   │   ├── ⚙️ config/                           # Configuration
│       │   │   │   └── ExerciseDataLoader.java
│       │   │   │
│       │   │   ├── 🎮 controller/                       # REST Controllers (18 files)
│       │   │   │   ├── 🔐 auth/
│       │   │   │   │   └── AuthController.java
│       │   │   │   ├── 📅 calendar/
│       │   │   │   │   └── CalendarController.java
│       │   │   │   ├── 💬 messaging/
│       │   │   │   │   ├── ConversationController.java
│       │   │   │   │   └── MessagingController.java
│       │   │   │   ├── 🌐 social/
│       │   │   │   │   └── SocialController.java
│       │   │   │   ├── 🏥 system/
│       │   │   │   │   └── HealthController.java
│       │   │   │   ├── 🧪 test/
│       │   │   │   │   └── TestController.java
│       │   │   │   ├── 👥 user/
│       │   │   │   │   ├── ProfessionalProfileController.java
│       │   │   │   │   ├── SubscriptionController.java
│       │   │   │   │   └── UserController.java
│       │   │   │   └── 🏋️ workout/
│       │   │   │       ├── ExerciseController.java
│       │   │   │       ├── PerformanceController.java
│       │   │   │       ├── PlanExerciseController.java
│       │   │   │       ├── ProgramPlanController.java
│       │   │   │       ├── ScheduledWorkoutController.java
│       │   │   │       ├── WorkoutPlanController.java
│       │   │   │       ├── WorkoutProgramController.java
│       │   │   │       └── WorkoutSessionController.java
│       │   │   │
│       │   │   ├── 📦 dto/                              # Data Transfer Objects (85 files)
│       │   │   │   ├── 📥 request/
│       │   │   │   │   ├── 🔐 auth/ (2 files)
│       │   │   │   │   ├── 💪 exercise/ (7 files)
│       │   │   │   │   ├── 💬 messaging/ (8 files)
│       │   │   │   │   ├── 📈 performance/ (1 file)
│       │   │   │   │   ├── 🎯 plan_exercise/ (1 file)
│       │   │   │   │   ├── 👨‍⚕️ professional_user/ (4 files)
│       │   │   │   │   ├── 📋 program_plan/ (3 files)
│       │   │   │   │   ├── 📅 scheduled_workouts/ (3 files)
│       │   │   │   │   ├── 💳 subscription/ (2 files)
│       │   │   │   │   ├── 👤 user/ (2 files)
│       │   │   │   │   ├── 📝 workout_plan/ (2 files)
│       │   │   │   │   ├── 🏆 workout_program/ (2 files)
│       │   │   │   │   └── 📊 workout_session/ (1 file)
│       │   │   │   └── 📤 response/
│       │   │   │       ├── 🔐 auth/ (1 file)
│       │   │   │       ├── 🔗 common/ (2 files)
│       │   │   │       ├── 💪 exercise/ (4 files)
│       │   │   │       ├── 💬 messaging/ (10 files)
│       │   │   │       ├── 📈 performance/ (1 file)
│       │   │   │       ├── 🎯 plan_exercise/ (3 files)
│       │   │   │       ├── 👨‍⚕️ professionional_user/ (4 files)
│       │   │   │       ├── 📋 program_plan/ (3 files)
│       │   │   │       ├── 📅 scheduled_workouts/ (6 files)
│       │   │   │       ├── 💳 subscription/ (3 files)
│       │   │   │       ├── 👤 user/ (3 files)
│       │   │   │       ├── 📝 workout_plan/ (2 files)
│       │   │   │       ├── 🏆 workout_program/ (4 files)
│       │   │   │       └── 📊 workout_session/ (2 files)
│       │   │   │
│       │   │   ├── ⚠️ exceptions/                        # Exception Handling (68 files)
│       │   │   │   ├── ErrorResponse.java
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── 🔐 auth/ (4 files)
│       │   │   │   ├── 🔧 common/ (7 files)
│       │   │   │   ├── 💪 exercise/ (3 files)
│       │   │   │   ├── 📈 performance/ (3 files)
│       │   │   │   ├── 📋 plan_program/ (5 files)
│       │   │   │   ├── 📅 scheduled_workout/ (10 files)
│       │   │   │   ├── 💳 subscription/ (4 files)
│       │   │   │   ├── 👤 user/ (3 files)
│       │   │   │   ├── 🏋️ workout/ (4 files)
│       │   │   │   ├── 📝 workout_plan/ (3 files)
│       │   │   │   ├── 🏆 workout_program/ (1 file)
│       │   │   │   └── 📊 workout_session/ (1 file)
│       │   │   │
│       │   │   ├── 🗂️ mapper/                           # Entity-DTO Mappers (16 files)
│       │   │   │   ├── 💬 messaging/ (2 files)
│       │   │   │   ├── 👤 user/ (3 files)
│       │   │   │   └── 🏋️ workout/ (8 files)
│       │   │   │
│       │   │   ├── 🏗️ model/                            # JPA Entities (43 files)
│       │   │   │   ├── 🔧 common/
│       │   │   │   │   └── BaseEntity.java
│       │   │   │   ├── 💬 messaging/
│       │   │   │   │   ├── Conversation.java
│       │   │   │   │   ├── ConversationParticipant.java
│       │   │   │   │   ├── Message.java
│       │   │   │   │   └── 🎯 enums/ (4 files)
│       │   │   │   ├── 🌐 social/
│       │   │   │   │   ├── ContentReport.java
│       │   │   │   │   ├── PostHashtag.java
│       │   │   │   │   ├── PostLike.java
│       │   │   │   │   ├── SocialComment.java
│       │   │   │   │   └── SocialPost.java
│       │   │   │   ├── 👤 user/
│       │   │   │   │   ├── ProfessionalProfile.java
│       │   │   │   │   ├── Subscription.java
│       │   │   │   │   ├── User.java
│       │   │   │   │   ├── UserRelationship.java
│       │   │   │   │   └── 🎯 enums/ (3 files)
│       │   │   │   └── 🏋️ workout/
│       │   │   │       ├── Exercise.java
│       │   │   │       ├── ExerciseGoalMapping.java
│       │   │   │       ├── ExerciseGoalMappingId.java
│       │   │   │       ├── FitnessGoal.java
│       │   │   │       ├── PerformanceRecord.java
│       │   │   │       ├── PlanExercise.java
│       │   │   │       ├── ProgramPlan.java
│       │   │   │       ├── ScheduledWorkout.java
│       │   │   │       ├── UserExerciseHistory.java
│       │   │   │       ├── UserExerciseRating.java
│       │   │   │       ├── WorkoutPlan.java
│       │   │   │       ├── WorkoutProgram.java
│       │   │   │       └── WorkoutSession.java
│       │   │   │
│       │   │   ├── 🗃️ repository/                        # JPA Repositories (22 files)
│       │   │   │   ├── 💬 messaging/ (3 files)
│       │   │   │   ├── 🌐 social/ (3 files)
│       │   │   │   ├── 👤 user/ (4 files)
│       │   │   │   └── 🏋️ workout/ (12 files)
│       │   │   │
│       │   │   ├── 🔐 security/                          # Security Configuration (6 files)
│       │   │   │   ├── CurrentUser.java
│       │   │   │   ├── CustomUserDetailsService.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   └── UserPrincipal.java
│       │   │   │
│       │   │   ├── 🎯 service/                           # Business Logic (21 files)
│       │   │   │   ├── 📅 calendar/ (1 file)
│       │   │   │   ├── 💬 messaging/ (2 files)
│       │   │   │   ├── 🔔 notification/ (1 file)
│       │   │   │   ├── 🌐 social/ (4 files)
│       │   │   │   ├── 👤 user/ (4 files)
│       │   │   │   └── 🏋️ workout/ (9 files)
│       │   │   │
│       │   │   └── 🛠️ util/                             # Utilities (2 files)
│       │   │       ├── SecurityUtil.java
│       │   │       └── WorkoutPlanMethodFinder.java
│       │   │
│       │   └── 📦 resources/                            # Application Resources
│       │       ├── 🧪 api-test.http                     # API testing
│       │       ├── ⚙️ application.properties            # Main config
│       │       ├── 🧪 application-test.properties       # Test config
│       │       └── 🗃️ db/migration/                     # Flyway Migrations (12 files)
│       │           ├── V001__Create_Core_User_System.sql
│       │           ├── V002__Create_Functions_And_Triggers.sql
│       │           ├── V003__Create_Exercise_System.sql
│       │           ├── V004__Create_Exercise_System_Triggers.sql
│       │           ├── V005__Create_Workout_Tracking_System.sql
│       │           ├── V006__Create_Workout_Tracking_Triggers.sql
│       │           ├── V007__Create_Program_System.sql
│       │           ├── V008__Create_Program_System_Triggers.sql
│       │           ├── V009__Create_Social_System.sql
│       │           ├── V010__Social_System_Optimizations.sql
│       │           ├── V011__Create_Messaging_System.sql
│       │           └── V012__Add_Foundation_Exercises.sql
│       │
│       └── 🧪 test/                                    # Test Sources
│           ├── ☕ java/com/chidituke/workout_tracker/
│           │   ├── WorkoutTrackerApplicationTests.java
│           │   ├── ⚙️ config/
│           │   │   └── BaseIntegrationTest.java
│           │   ├── 🎮 controller/
│           │   │   ├── 🔐 auth/
│           │   │   │   └── AuthControllerTest.java
│           │   │   ├── 👤 user/
│           │   │   │   └── UserControllerTest.java
│           │   │   └── 🏋️ workout/
│           │   │       └── ExerciseControllerTest.java
│           │   ├── 🗃️ migration/                        # Migration Tests (7 files)
│           │   │   ├── V001MigrationTest.java
│           │   │   ├── V003MigrationTest.java
│           │   │   ├── V005MigrationTest.java
│           │   │   ├── V007MigrationTest.java
│           │   │   ├── V009MigrationTest.java
│           │   │   ├── V010MigrationTest.java
│           │   │   └── V011MigrationTest.java
│           │   └── 🔐 security/
│           │       └── SecurityConfigTest.java
│           └── 📦 resources/
│               ├── ⚙️ application-test.properties
│               └── ⚙️ application-test.yml
│
└── 🎨 frontend/                                       # React Frontend
    ├── 📖 README.md                                   # Frontend docs
    ├── 📦 package.json                                # Dependencies
    ├── 🔒 package-lock.json                           # Lockfile
    ├── 🎨 tailwind.config.js                          # Tailwind CSS config
    ├── 📝 tsconfig.json                               # TypeScript config
    ├── 🎨 postcss.config.js                           # PostCSS config
    ├── 🔧 components.json                             # Shadcn/ui config
    ├── 🔐 .env                                        # Environment variables
    ├── 🚫 .gitignore                                  # Git ignore
    │
    ├── 🌍 public/                                     # Static Assets
    │   ├── 🔖 favicon.ico
    │   ├── 📄 index.html
    │   ├── 🖼️ logo192.png
    │   ├── 🖼️ logo512.png
    │   ├── 📋 manifest.json
    │   └── 🤖 robots.txt
    │
    └── 📁 src/                                        # Source Code
        ├── 🎨 App.css                                 # Global styles
        ├── 🧪 App.test.js                             # App tests
        ├── 🚀 App.tsx                                 # Main App component
        ├── 🎨 index.css                               # Global CSS
        ├── 🚀 index.js                                # App entry point
        ├── 🖼️ logo.svg                                # Logo asset
        ├── 📊 reportWebVitals.js                      # Performance monitoring
        ├── 🧪 setupTests.js                           # Test setup
        ├── 🎨 theme.js                                # Theme configuration
        │
        ├── 🧩 components/                             # React Components
        │   ├── 🧪 ApiTestPanel.tsx                    # API testing UI
        │   ├── 📋 index.ts                            # Component exports
        │   │
        │   ├── 📅 CalendarPage/                       # Calendar Components (4 files)
        │   │   ├── ExerciseConfigModal.tsx
        │   │   ├── ExerciseSelector.tsx
        │   │   ├── InWorkoutExerciseSelector.tsx
        │   │   └── index.ts
        │   │
        │   ├── 💪 ExercisePage/                       # Exercise Components (4 files)
        │   │   ├── DesktopFilters.tsx
        │   │   ├── ExerciseCard.tsx
        │   │   ├── MobileFilterDrawerProps.tsx
        │   │   └── index.ts
        │   │
        │   ├── 🌟 LandingPage/                        # Landing Components (9 files)
        │   │   ├── BetaAccess.tsx
        │   │   ├── ExerciseLibrary.tsx
        │   │   ├── FinalCTA.tsx
        │   │   ├── HeroSection.tsx
        │   │   ├── Navigation.tsx
        │   │   ├── PricingSection.tsx
        │   │   ├── ProblemSection.tsx
        │   │   ├── SolutionSection.tsx
        │   │   └── index.ts
        │   │
        │   ├── 📊 WorkoutTracking/                    # Workout Components (1 file)
        │   │   └── WorkoutTrackingInterface.tsx
        │   │
        │   ├── 🔐 auth/                               # Auth Components (2 files)
        │   │   ├── LoginForm.tsx
        │   │   └── RegisterForm.tsx
        │   │
        │   ├── 🎨 layout/                             # Layout Components (7 files)
        │   │   ├── BottomNavigation.tsx
        │   │   ├── FloatingActionButton.tsx
        │   │   ├── MobileLayout.tsx
        │   │   ├── QuickWorkoutModal.tsx
        │   │   ├── SearchModal.tsx
        │   │   ├── TopNavigation.tsx
        │   │   └── WorkoutModeOverlay.tsx
        │   │
        │   └── 🎯 ui/                                 # UI Components (7 files)
        │       ├── badge.tsx
        │       ├── button.tsx
        │       ├── card.tsx
        │       ├── input.tsx
        │       ├── label.tsx
        │       ├── select.tsx
        │       └── textarea.tsx
        │
        ├── 🔄 contexts/                               # React Contexts (2 files)
        │   ├── AuthContext.tsx
        │   └── WorkoutContext.tsx
        │
        ├── 🪝 hooks/                                  # Custom Hooks (2 files)
        │   ├── index.ts
        │   └── useExerciseFilters.ts
        │
        ├── 🛠️ lib/                                    # Utilities (1 file)
        │   └── utils.ts
        │
        ├── 📄 pages/                                  # Page Components (14 files)
        │   ├── BillingPage.tsx
        │   ├── CalendarPage.tsx
        │   ├── CommunityPage.tsx
        │   ├── ExercisesPage.tsx
        │   ├── HelpPage.tsx
        │   ├── LandingPage.tsx
        │   ├── LoginPage.tsx
        │   ├── MessagesPage.tsx
        │   ├── NotificationsPage.tsx
        │   ├── ProgressPage.tsx
        │   ├── RegisterPage.tsx
        │   ├── SettingsPage.tsx
        │   ├── WelcomePage.tsx
        │   └── WorkoutModePage.tsx
        │
        ├── 🌐 services/                               # API Services (7 files)
        │   ├── apiClient.ts
        │   ├── authService.ts
        │   ├── calendarApi.ts
        │   ├── exerciseApi.ts
        │   ├── index.ts
        │   ├── mockData.ts
        │   └── transformers.ts
        │
        ├── 🏷️ types/                                  # TypeScript Types (5 files)
        │   ├── api.ts
        │   ├── auth.ts
        │   ├── enums.ts
        │   ├── exercise.ts
        │   └── index.ts
        │
        └── 🛠️ utils/                                  # Utility Functions (4 files)
            ├── dateUtils.ts
            ├── exerciseFormatters.ts
            ├── index.ts
            └── validation.ts
```

## 🚀 Key Features & Modules

### 🔐 Authentication & User Management
- JWT-based authentication
- User profiles and professional accounts
- Subscription management
- Social relationships

### 💪 Exercise Library & Management
- Comprehensive exercise database
- Custom exercise creation
- Exercise ratings and history
- Goal mapping and analytics

### 📅 Workout Planning & Scheduling
- Workout plan templates
- Program creation and enrollment
- Scheduled workouts with calendar integration
- Progress tracking and analytics

### 💬 Social & Messaging
- User conversations and messaging
- Progress check-ins and assignments
- Social posts and community features
- Professional-client communication

### 📊 Performance & Analytics
- Performance tracking and records
- Workout analytics and insights
- Progress visualization
- Goal achievement monitoring

### 🎨 Modern Frontend
- Responsive React TypeScript application
- Tailwind CSS for styling
- Mobile-first design approach
- Component-based architecture

---

*Generated on: August 8, 2025*
*Total Files Analyzed: 400+*
*Project Status: Active Development*