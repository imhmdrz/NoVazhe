# NoVazheh — Engineering Project Overview

**Status:** Stable KMP MVP (v1.0)  
**Last Full Review:** August 2025  

---

## 1. Project Identification

### What Is This?
NoVazheh (نواژه) is a **Kotlin Multiplatform speech-language therapy app** targeting Android, iOS, and a companion JVM/Ktor server. It helps parents record Persian/English words with audio for their children (ages 1–18), and provides gamified vocabulary learning through flashcards, quizzes, memory games, color-sorting, shadow-matching, and odd-one-out activities.

### Problem Solved
Children who need speech therapy — particularly in bilingual (Persian/English) contexts — benefit from repeated audio-visual word exposure. Parents record the words; the app delivers them back through engaging mini-games with progress tracking.

### Application Type
| Layer | Type |
|---|---|
| Mobile App | Cross-platform (Android + iOS via Compose Multiplatform) |
| Backend API | JVM Ktor server (Netty, optional PostgreSQL / H2) |
| Shared Library | Kotlin Multiplatform (networking, data models, DI) |

### Main Technologies
- **Kotlin 2.2.21** with Kotlin Multiplatform
- **Compose Multiplatform 1.9.3** for shared UI
- **Ktor 3.3.3** for both client (shared) and server
- **Exposed ORM 0.61.0** for database access
- **Koin 4.1.1** for dependency injection
- **Coil 3.3.0** for image loading
- **Navite Compose Navigation 2.9.1** for routing

---

## 2. Repository Structure

```
NoVazheh/
├── build.gradle.kts                  # Root Gradle (plugin declarations)
├── settings.gradle.kts               # Multi-module project: composeApp, server, shared
├── gradle.properties                 # Gradle JVM args + Android flags
├── gradle/libs.versions.toml         # Version catalog (ALL dependency versions here)
├── gradlew / gradlew.bat             # Gradle wrapper scripts
│
├── composeApp/                       # ── Mobile UI module (Android + iOS targets) ──
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/...     # Shared Compose screens, ViewModels, DI, theme
│       ├── androidMain/kotlin/...    # Android-specific: MainActivity, AppLogger, player, recorder, permissions
│       ├── iosMain/kotlin/...        # iOS-specific: MainViewController, platform implementations
│       └── commonTest/               # Compose UI tests
│
├── shared/                           # ── Shared code library (Android + iOS + JVM) ──
│   └── src/
│       ├── commonMain/kotlin/...     # Repositories, network client, DI module, token storage, models
│       ├── androidMain/              # Android platform implementations
│       ├── iosMain/                  # iOS platform implementations
│       └── jvmMain/                  # JVM desktop implementations
│
├── server/                           # ── Ktor API Server (JVM only) ──
│   ├── build.gradle.kts
│   ├── docker-compose.yml            # Local PostgreSQL helper
│   └── src/
│       ├── main/kotlin/...           # Application, routes, services, security, DB tables/seed
│       ├── main/resources/openapi/documentation.yaml  # OpenAPI spec (Swagger UI)
│       └── test/kotlin/              # Ktor test-host unit test
│
├── iosApp/                           # ── iOS Xcode project entry point ──
│   ├── iosApp.xcodeproj/project.pbxproj
│   └── iosApp/iOSApp.swift           # SwiftUI host wrapping Compose UI
│
├── data/                             # ── H2 database files (development only) ──
│   ├── novazheh.mv.db              # Persistent H2 file DB
│   └── novazheh.trace.db           # Trace/debug log
│
├── doc/                              # ── Thesis PDF source (LaTeX — IUST academic thesis) ──
│   └── main.tex + .bib + fonts + images
│
├── docs/                             # ── Generated Swagger HTML ──
│   └── index.html
│
└── .gitignore
```

### Entry Points
| Target | File | Description |
|---|---|---|
| Android App | `composeApp/src/androidMain/kotlin/.../MainActivity.kt` | Launches `NovazhehApp` (Koin + Compose) |
| iOS App | `iosApp/iosApp/iOSApp.swift` → `ContentView.swift` → `MainViewController.kt` | SwiftUI host for Compose framework |
| Server | `server/src/main/kotlin/.../Application.kt:main()` | Ktor embedded server on Netty, port 8080 |

### Key Configuration Files
- `gradle/libs.versions.toml` — **single source of truth** for all dependency versions
- `settings.gradle.kts` — module declarations
- `composeApp/build.gradle.kts` — Compose Multiplatform + Android config (compileSdk 36, minSdk 24)
- `server/build.gradle.kts` — Ktor server plugins and Exposed ORM dependencies
- `shared/build.gradle.kts` — shared module: Ktor client, coroutines, Koin, XML serialization

---

## 3. Technology Stack

### Frontend Framework
| Library | Version | Purpose | Critical? |
|---|---|---|---|
| Compose Multiplatform | 1.9.3 | Cross-platform UI (Android + iOS) | **Critical** — the entire mobile UI |
| Material 3 | bundled | Design system, components, theming | **Critical** |
| Navigation Compose | 2.9.1 | Screen routing & transitions | **Critical** |
| Coil 3 | 3.3.0 | Image loading (AsyncImage) with disk cache | **Critical** — images in every screen |
| Compottie | 2.2.4 | Lottie animations (win/lose celebrations) | High — game feedback |
| Backdrop | 2.0.0-alpha03 | Liquid-glass bottom navigation effect | Medium — UI polish only |
| Accompanist Permissions | 0.37.3 | Runtime camera/mic permissions | High for recording feature |

### Backend Framework
| Library | Version | Purpose | Critical? |
|---|---|---|---|
| Ktor Server (Netty) | 3.3.3 | HTTP server, routing, plugins | **Critical** — the entire API layer |
| Exposed ORM | 0.61.0 | Database abstraction (SQL DSL) | **Critical** |
| HikariCP | 7.0.2 | Connection pooling for PostgreSQL / H2 | **Critical** |
| Auth0 JWT | bundled via Ktor plugin | Token verification & signing | **Critical** — auth layer |

### Languages
- **Kotlin (primary)** — all application code is Kotlin, both client and server
- **SwiftUI (minimal host wrapper)** — `iOSApp.swift` / `ContentView.swift` just wrap the Compose framework entry point
- **LaTeX / BibTeX** — thesis documentation only (`doc/`)

### Database
| Environment | Technology | File Location |
|---|---|---|
| Development (default) | H2 in file mode (`MODE=PostgreSQL`) | `data/novazheh.mv.db` |
| Production | PostgreSQL 16 | Via `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` env vars |

### Authentication System
- **JWT tokens** — HMAC-SHA256 signed, 30-day validity
- Secret: configurable via `JWT_SECRET` env var (fallback: `"novazheh-super-secret-key-2024"`)
- Password hashing: PBKDF2-like custom implementation using SHA-256 with 10,000 iterations + random salt
- Token claims contain only `parentId` — used for all authorization decisions

### External Services (none configured; previously imagekit.io was referenced but is now replaced)
The old codebase apparently hosted images/audio on **imagekit.io**, but the current code (see `StorageService.kt`) serves content from a local filesystem directory via Ktor's static file handler at `/content/*`. The external service has been fully removed.

### External APIs / Integrations
- None currently configured. All media is served locally by the server.
- Quiz questions use **random selection** — no AI or NLP backend.

---

## 4. Application Architecture

### High-Level Data Flow

```
┌─────────────┐       HTTPS        ┌──────────────────┐       JDBC      ┌──────────┐
│  Android /  │ ◄────────────────► │   Ktor Server    │ ◄──────────────► │ PostgreSQL│
│   iOS App   │                    │   (Netty :8080)   │                │ or H2    │
└─────────────┘                    └──────────────────┘                └──────────┘
      ▲                                    │
      │                                    │ static files
      │                              /content/*
      │                                   ▼
      │                            ┌──────────────┐
      │                            │ Local FS     │
      │                            │ (images/     │
      │                            │  audio/)     │
      │                            └──────────────┘
```

### Component Communication

1. **Mobile → Server**: Compose Multiplatform app calls `shared` module repositories (`ContentRepository`, `ChildRepository`, etc.) which use a shared Ktor HTTP client to hit the server's REST API. JWT is attached as a Bearer token via `TokenStorage`.

2. **Server ↔ Database**: Ktor routes delegate to Kotlin objects (services) that perform Exposed ORM transactions directly against PostgreSQL or H2. No connection pooling beyond HikariCP; all DB calls are synchronous within suspending coroutines.

3. **State Management Pattern**:
   - **Server-side**: Stateless — no session management, every request validated via JWT.
   - **Client-side**: Koin-scoped ViewModels hold `MutableStateFlow` UI state. Repositories in the shared module return `Result<T>`. The app listens to auth events (401 logout) via a singleton `AuthStateManager`.

4. **DI Wiring**:
   ```
   AppModule (composeApp) → provides all ViewModels
       ↑
   SharedModule (shared) → provides HttpClient, TokenStorage, all Repositories
       ↑
   Platform-specific implementations (expect/actual: createHttpClient, ApiConfig, TokenStorage, AudioPlayer, etc.)
   ```

### Architectural Patterns Used
| Pattern | Where | Notes |
|---|---|---|
| MVVM | All screens — Compose Screen + ViewModel + State data class | Standard Android/Compose pattern |
| Repository | `shared/data/*Repository.kt` | Wraps HTTP calls, returns `Result<T>` |
| expect/actual | Platform abstractions (HTTP client, settings, audio player, recorder) | KMP required for cross-platform I/O |
| Singleton objects (Kotlin) | All server Services (`AuthService`, `ContentService`, etc.) and DB tables | Static-style services with no DI — direct object access |
| Shared Element Transitions | Game screens use Compose's experimental shared transitions API | Category image morphs between dashboard → game screen |

### User Roles
1. **Guest** (not logged in): Can browse categories, play games as a child without progress tracking.
2. **Parent/Mother**: Registered account. Can manage children (max 2), add custom words with audio recordings, set timer durations, view progress stats.
3. **Child**: Not a separate user — just an associated record belonging to a parent. Has no login. Progress is tracked per child.

---

## 5. Frontend Deep Analysis

### Screens (Routes) — defined in `composeApp/src/commonMain/kotlin/.../App.kt`

| Route | Screen | Orientation | Purpose |
|---|---|---|---|
| `mother` | `MotherScreen` | Vertical only | Dashboard + Profile. Entry point of the app. Bottom nav with Dashboard/Profile tabs. |
| `auth` | `AuthScreen` | Vertical only | Login / Register form (guests redirected here from locked profile tab). |
| `game` | `GameScreen` | Vertical only | Flashcard learning mode — category hero image, word cards, prev/next buttons, timer, shared-element morph. |
| `quiz` | `QuizScreen` | Vertical only | 4-option multiple-choice quiz (audio plays, tap correct word). Win/lose overlays with Lottie. |
| `face-game` | `FaceGameScreen` | Vertical only | Face recognition / matching mini-game using camera (likely front-facing). |
| `memory-game/{categoryId}` | `MemoryGameScreen` | Vertical only | Card-flip memory game for a specific category. |
| `color-sorting` | `ColorSortingScreen` | Vertical only | Drag-and-drop color sorting activity. |
| `shadow-match/{categoryId}` | `ShadowMatchScreen` | Vertical only | Match objects to their shadow outlines. |
| `odd-one-out` | `OddOneOutScreen` | Vertical only | Find the item that doesn't belong in a group. |
| `add-word` | `AddWordScreen` | Vertical only | Parent adds custom words with text, optional English translation, image, and audio recording. |

### Component Architecture (`ui/components/`)

There are ~25 reusable components organized by function:

**Dialogs:** `AddChildDialog`, `PinCodeDialog`, `KidsModeGuideDialog`
**Layout helpers:** `DashboardButton`, `EmptyChildrenContent`, `LoadingContent`, `ErrorMessageCard`
**Forms / Inputs:** `InputSection`, `AgeSelector`, `GenderButton`, `ImageSelectionSection`
**Audio:** `AudioRecordingSection` — recording UI with waveform visualization
**Game feedback:** `WinCelebrationOverlay`, `LoseOverlay` — Lottie-based celebrations
**Child-specific:** `ChildAppBarComponent`, `ChildContent`, `ChildrenListContent`
**Word management:** `CustomWordContent`, `AddWordHeader`
**Utility:** `LocalDragTargetInfo`, `AppPinToggle` (padlock overlay on all non-auth screens)

### State Management

| Layer | Mechanism | File |
|---|---|---|
| Global auth events | Singleton `AuthStateManager` with `SharedFlow<AuthEvent>` | `shared/.../data/AuthStateManager.kt` |
| Per-screen state | `MutableStateFlow<T>` inside Koin-scoped ViewModels | `ui/viewmodels/models/*.kt` |
| Persistent settings | `TokenStorage` backed by multiplatform-settings (SharedPreferences on Android, UserDefaults on iOS) | `shared/.../data/TokenStorage.kt` |

**UI State data classes:**
- `ChildUiState` — categories, selected category, words list, current word index, progress stats, timer state
- `MotherUiState` — children list, custom words list, selected tab (Dashboard/Profile), auth status
- `QuizUiState` — question/option lists, current answer tracking

### Data Fetching

All HTTP calls go through repositories in the shared module. Each repository method returns `Result<T>` where T is a response DTO wrapper (`ApiResponse`, `ListResponse`). The ViewModels unwrap via `.fold(onSuccess = ...)` and update state flows. No caching layer beyond Coil's image cache.

### UI Libraries & Styling
- **Material 3** with custom `AppTheme` (`ui/theme/Color.kt`, `Type.kt`) using IRANSans font (Persian)
- **Coil 3** for async image loading with disk + memory caching
- **Compottie** for Lottie JSON animations (win celebrations: `corner_winning.json`, `main_winning*.json`)
- **Backdrop** library for glass-blur bottom navigation (`io.github.kyant0:backdrop`)
- **Navigation Compose** with experimental shared transitions API (`ExperimentalSharedTransitionApi`)

### Complex UI Flows
1. **Dashboard → Game**: Category tile on dashboard shares its image via `sharedElementKey` — the image morphs into the game screen's category hero pill using Compose's shared transition animation system.
2. **Game → Quiz**: Tap "learned X of Y" badge → opens quiz for that category with zoom-enter transition.
3. **Audio Player lifecycle**: Single `AudioProvider` composable wraps all screens; audio continues across navigation within the same player scope.

---

## 6. Backend Deep Analysis

### API Structure — Full Route Map

| Method | Path | Auth Required | Service | Description |
|---|---|---|---|---|
| `GET` | `/` | No | — | Health/welcome message |
| `GET` | `/health` | No | — | JSON health check |
| `POST` | `/api/auth/register` | No | `AuthService.register()` | Register parent account, return JWT token |
| `POST` | `/api/auth/login` | No | `AuthService.login()` | Login, return JWT token |
| `GET` | `/api/auth/me` | Yes (JWT) | `AuthService.getParentById()` | Get current parent profile |
| `GET` | `/api/children` | Yes (JWT) | `ChildService.getChildren()` | List all children for logged-in parent |
| `GET` | `/api/children/{id}` | Yes (JWT) | `ChildService.getChildById()` | Single child details |
| `POST` | `/api/children` | Yes (JWT) | `ChildService.createChild()` | Add new child (max 2 per parent) |
| `PUT` | `/api/children/{id}` | Yes (JWT) | `ChildService.updateChild()` | Edit child fields |
| `DELETE` | `/api/children/{id}` | Yes (JWT) | `ChildService.deleteChild()` | Soft-delete child (`isActive = false`) |
| `GET` | `/api/categories` | No | `ContentService.getAllCategories()` | Public category listing with word counts |
| `POST` | `/api/categories` | Yes (JWT) | `ContentService.createCategory()` | Create shared global category |
| `GET` | `/api/categories/{id}` | No | `ContentService.getCategoryById()` | Single category details |
| `GET` | `/api/categories/{id}/words` | No | `ContentService.getWordsByCategory()` | All words in a category |
| `GET` | `/api/categories/{categoryId}/words/progress?childId=X` | Optional (JWT) | `ContentService.getWordsByCategoryWithProgress()` | Words with per-child progress (ownership verified) |
| `GET` | `/api/words/{id}` | No | `ContentService.getWordById()` | Single word details |
| `GET` | `/api/custom-words` | Yes (JWT) | `ContentService.getCustomWords()` | Parent's custom words |
| `POST` | `/api/custom-words` | Yes (JWT) | `ContentService.createCustomWord()` | Add custom word with audio |
| `DELETE` | `/api/custom-words/{id}` | Yes (JWT) | `ContentService.deleteCustomWord()` | Remove custom word |
| `GET` | `/api/quiz/question?categoryId=X&childId=Y` | Optional (JWT) | `QuizService.generateQuizQuestion()` | Single quiz question (4 options, audio URL) |
| `GET` | `/api/quiz/questions?categoryId=X&childId=Y&count=N` | Optional (JWT) | `QuizService.generateQuizQuestions()` | Batch of N questions |
| `POST` | `/api/quiz/submit` | Yes (JWT) | `QuizService.submitQuizAnswer()` | Submit answer, record attempt + progress |
| `GET` | `/api/progress/child/{childId}` | Yes (JWT) | `ProgressService.getChildProgressStats()` | Full progress stats with per-category breakdown |
| `GET` | `/api/progress/child/{childId}/recent?limit=N` | Yes (JWT) | `ProgressService.getRecentAttempts()` | Last N quiz attempts |
| `GET` | `/api/progress/child/{childId}/word/{wordId}` | Yes (JWT) | `ProgressService.getWordProgress()` | Single word progress for a child |

### Services (Server-side Business Logic)

All services are **Kotlin singleton objects** — no DI, no interface abstraction. Each is a file in `server/src/main/kotlin/.../services/`:

| Service | Purpose |
|---|---|
| `AuthService` | Register/login parent accounts; JWT token generation & validation (via `JwtConfig`) |
| `ChildService` | CRUD for children with ownership verification (`parentId` + `isActive` checks) |
| `ContentService` | Categories, words, custom words — shared categories are global; custom words belong to a parent |
| `QuizService` | Random question generation (4 options: 1 correct + 3 wrong), answer submission, progress tracking. LEARNED_THRESHOLD = 3 consecutive correct answers. |
| `ProgressService` | Aggregated stats: learned/in-progress word counts per category, overall accuracy, recent attempts |
| `StorageService` | URL builder for local static files (images/audio). R
---

## 7. Database / Data Model

### Technology
- **Development**: H2 database in file mode with PostgreSQL compatibility mode (`MODE=PostgreSQL`)
- **Production**: PostgreSQL 16 via JDBC
- **ORM**: JetBrains Exposed DSL v0.61.0 (SQL builder, no auto-migrations)
- **Migration system**: None - tables are created via `SchemaUtils.create()` on every startup. The seeder deletes all data and re-seeds from scratch each time.

### Entity Relationship Diagram

```
Parents (1) --< Children (*)          -- CASCADE delete on parent deletion
  |                    |
  |                    |--< QuizAttempts (*)
  |                    |--< ChildProgress (*)   -- UNIQUE(childId, wordId)
  |
  |--< CustomWords (*)         -- FK -> Categories (SET NULL on category delete)

Categories (1) --< Words (*)          -- CASCADE delete on category deletion
  |
  +--< CustomWords (*)              -- Optional link from custom words to categories
```

### Table Definitions

#### `parents` - Mother accounts
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated via Exposed IntIdTable |
| username | VARCHAR(100) | Unique index, must be >=3 chars on client side |
| password_hash | VARCHAR(255) | PBKDF2 hash + salt (base64 encoded) |
| display_name | VARCHAR(100) | Display name shown in app |
| is_active | BOOLEAN | Default true; used for soft-deactivation |
| created_at, updated_at | DATETIME | Timestamps |

#### `children` - Child records (no login)
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated |
| parent_id (FK->parents) | INT | CASCADE delete; max 2 active children per parent |
| name | VARCHAR(100) | Child's name (Persian/English) |
| age | INT | 1-18 validated on client |
| gender | VARCHAR(10) | "BOY" or "GIRL" -- also used for avatar selection in UI |
| avatar_url | VARCHAR(255) nullable | Avatar image URL (base64 data URL supported) |
| is_active, timestamps | -- | Soft delete via isActive=false |

#### `categories` - Vocabulary categories
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated |
| name_fa | VARCHAR(100) | Persian name (unique per active category, enforced on create) |
| name_en | VARCHAR(100) | English name |
| icon_url | TEXT nullable | Category icon image URL |
| display_order | INT | Sort order; auto-incremented if not specified |
| is_active | BOOLEAN | Default true |

#### `words` - Vocabulary entries
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated |
| category_id (FK->categories) | INT | CASCADE delete |
| word_fa | VARCHAR(100) | Persian word |
| word_en | VARCHAR(100) | English translation |
| image_url | TEXT nullable | Base64 data URL or external URL |
| audio_url | TEXT | Base64 data URL -- the core of speech therapy (word pronunciation) |
| display_order, is_active | -- | Standard ordering + soft-delete |

#### `custom_words` - Parent-recorded words
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated |
| parent_id (FK->parents) | INT | CASCADE delete; per-parent ownership |
| word_fa | VARCHAR(100) | Persian word |
| word_en | VARCHAR(100) nullable | Optional English translation |
| image_url, audio_url | TEXT | Base64 data URLs -- parent records audio via microphone |
| category_id (FK->categories) | INT nullable | Optional link; SET NULL if deleted |

#### `quiz_attempts` - Individual answer records
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated |
| child_id (FK->children) | INT | CASCADE delete |
| word_id (FK->words) | INT | CASCADE delete |
| is_correct | BOOLEAN | Correct or incorrect answer |
| response_time_ms | INTEGER nullable | Time from question display to selection |
| attempted_at | DATETIME | Timestamp of attempt |

#### `child_progress` - Aggregated per-word progress (UNIQUE childId+wordId)
| Column | Type | Notes |
|---|---|---|
| id (PK) | INT | Auto-generated |
| child_id, word_id (FKs) | INT | UNIQUE composite index |
| correct_attempts | INTEGER | Running count of correct answers |
| total_attempts | INTEGER | Total number of attempts |
| is_learned | BOOLEAN | True when correct_attempts >= 3 (LEARNED_THRESHOLD) |
| last_attempt_at, learned_at | DATETIME nullable | Timing metadata |

### Seeder Behavior
The seeder (`DatabaseSeeder.kt`) **deletes ALL data** on every startup and re-inserts ~90+ seed words across 10 categories with images/audio URLs. This means:
- No incremental migrations exist.
- Custom words created by parents persist (they're not deleted by the seeder).
- A test parent account (`test` / `test123`) is seeded alongside sample children (`���`, age 5; `����`, age 4) with pre-recorded quiz history for demo purposes.

---

## 8. Development Workflow

### Prerequisites
- **JDK 17+** (Gradle uses JVM 11 target, but runtime requires JDK 17+)
- **Android SDK** (compileSdk 36, minSdk 24) with Android Emulator or physical device
- **Xcode 15+** for iOS builds (via iOS Simulator)
- **Kotlin 2.2.21** and **Compose Multiplatform 1.9.3** tooling in Android Studio / IntelliJ

### Run Commands

| Target | Command | Output |
|---|---|---|
| Android app (dev build) | `./gradlew :composeApp:assembleDebug` (macOS/Linux)<br>`.\gradlew.bat :composeApp:assembleDebug` (Windows) | APK at `composeApp/build/outputs/apk/debug/` |
| Android app (run directly) | Run configuration from IDE toolbar, or:<br>`./gradlew :composeApp:installDebug` + launch on device/emulator | -- |
| Server (dev mode) | `./gradlew :server:run` | Ktor server at http://localhost:8080 |
| iOS app | Open `iosApp/` in Xcode, select simulator target, Cmd+R | -- |

### Environment Variables (Server)

| Variable | Default | Description |
|---|---|---|
| `USE_POSTGRES` | unset -> H2 | Set to `true` for PostgreSQL instead of H2 |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/novazheh` | PostgreSQL JDBC URL |
| `DATABASE_USER` | `postgres` | DB username |
| `DATABASE_PASSWORD` | `password` | DB password |
| `JWT_SECRET` | `"novazheh-super-secret-key-2024"` | JWT signing secret (INSECURE default!) |
| `JWT_ISSUER` | `"novazheh-server"` | JWT issuer claim |
| `JWT_AUDIENCE` | `"novazheh-users"` | JWT audience claim |
| `CONTENT_DIR` | `"content"` | Directory name for static media files (relative to server working dir) |
| `CONTENT_BASE_URL` | `"http://10.0.2.2:8080"` | Base URL clients use to reach the server (emulator default; override for real device) |

### Build Commands

```bash
# Clean build of all modules
./gradlew clean build

# Android debug APK only
./gradlew :composeApp:assembleDebug

# Server standalone JAR + run
./gradlew :server:run
```

### Testing Setup
- **Server unit test**: Single test in `server/src/test/kotlin/mohaamadreza/saemipour/no/vazheh/ApplicationTest.kt` - tests that the root endpoint returns OK. Uses Ktor's `testApplication`.
- **Compose UI test**: Empty placeholder at `composeApp/src/commonTest/kotlin/.../ComposeAppCommonTest.kt` and `shared/src/commonTest/kotlin/.../SharedCommonTest.kt` - not yet implemented.
- No integration tests, no end-to-end tests exist.

### Docker Usage
A minimal `docker-compose.yml` is present at `server/docker-compose.yml` that only runs a **PostgreSQL 16 container** for development:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      POSTGRES_DB: novazheh
    ports: ["5432:5432"]
    healthcheck: pg_isready -U postgres
```

There is **no Dockerfile** for the Ktor server itself - it's not containerized. The docker-compose file only assists local PostgreSQL development.

---

## 9. Deployment Architecture

### Containerization Status
- **No production Docker setup exists.** There is no Dockerfile for any component.
- The only Docker artifact is `server/docker-compose.yml` which launches a PostgreSQL helper container locally.
- For production, the project would need:
  1. A Dockerfile for the Ktor server (based on an Alpine/JRE image)
  2. The server JAR built via `./gradlew :server:installDist` or `:server:build`
  3. NGINX or similar reverse proxy configuration (not present in repo)

### Hosting Assumptions
Based on the code structure, the intended deployment model is:

1. **Server**: A VPS or cloud VM running the Ktor server as a systemd service or inside a Docker container, with PostgreSQL 16 as its database.
2. **Static Content**: Files in `content/` directory served directly by Ktor (no CDN configured). For production, this should be moved to a CDN (S3 + CloudFront, or similar) since the server is not optimized for large static file serving.
3. **Mobile Apps**: Published to Google Play Store and Apple App Store.

### Production Build Process (Implied)
```bash
# 1. Build Android release APK/AAB
./gradlew :composeApp:assembleRelease

# 2. Build iOS framework (requires macOS + Xcode)
# Open in Android Studio, switch target to iOS, build ComposeApp.framework

# 3. Build server JAR
./gradlew :server:installDist   # Produces runnable distribution in server/build/install/
```

### Infrastructure Needs (Not Yet Built)
- [ ] Dockerfile for Ktor server
- [ ] CI/CD pipeline (GitHub Actions, GitLab CI, etc.) - none present
- [ ] NGINX/Apache reverse proxy config
- [ ] SSL/TLS termination (HTTPS)
- [ ] Monitoring & logging aggregation (only local logback XML exists)
- [ ] Database backup strategy
- [ ] Content delivery network for images/audio

### Security Notes for Deployment
1. **JWT_SECRET must be changed** from the default `"novazheh-super-secret-key-2024"` in production - this is a critical security requirement.
2. CORS currently allows `anyHost()` - restrict to known domains before production.
3. The database password defaults to `"password"` - must be overridden via env vars.
4. No rate limiting exists on any endpoint (including auth endpoints).
5. HTTPS is not enforced by the server itself (Ktor's Netty does not include TLS configuration in this project).

---
---

## 10. Current Risks and Technical Debt

### Security Concerns (HIGH PRIORITY)

| Risk | Severity | Details | File(s) |
|---|---|---|---|
| Default JWT secret is insecure | **Critical** | `"novazheh-super-secret-key-2024"` is hardcoded as fallback. Anyone can forge tokens. | `server/.../security/JwtConfig.kt` |
| No rate limiting on auth endpoints | High | Brute-force attacks against `/api/auth/login` and `/api/auth/register` are unblocked. | `server/.../routes/AuthRoutes.kt` |
| Password hashing is non-standard | Medium | Custom PBKDF2-like implementation (SHA-256 repeated 10k times). Not bcrypt, scrypt, or Argon2. Reversible in theory. | `server/.../security/PasswordUtils.kt` |
| CORS allows any host in dev | Medium | `anyHost()` is enabled by default - fine for dev but must be restricted before production deployment. | `server/.../Application.kt:configureCORS()` |
| Kids PIN stored as plaintext | Low (by design) | The kids-mode 4-digit PIN is stored unencrypted in shared preferences. Comment in code acknowledges this is a soft lock, not security. | `shared/.../data/TokenStorage.kt` |

### Fragile Areas & Code Smells

| Issue | Location | Description |
|---|---|---|
| Services are singleton objects with no interfaces | All `server/.../services/*.kt` | Cannot be mocked in tests; tightly coupled to Exposed ORM and DB access. No DI container manages them (they bypass Koin entirely). |
| Seeder deletes ALL data on startup | `DatabaseSeeder.kt` | Any parent-created custom words survive, but categories are wiped. If a category is deleted by the seeder and then re-created with a different ID, child progress records pointing to old IDs become orphaned (but the FK is CASCADE, so they'd be deleted too). |
| No database migrations | All DB code | Adding a column or table requires modifying `SchemaUtils.create()` AND manually handling existing data. |
| Quiz question generation can produce duplicates | `QuizService.kt` | Wrong options are randomly selected from all words in the category; if fewer than 4 wrong words exist, the same word could appear multiple times as an option. The service checks `allWords.size < OPTIONS_COUNT` but doesn't check for duplicate wrong options after shuffle. |
| LEARNED_THRESHOLD is duplicated | Client: `GameScreen.kt`, Server: `QuizService.kt` | Both define `LEARNED_THRESHOLD = 3`. If one changes, the other becomes stale and progress tracking breaks silently. |
| Hardcoded audio advance delay | `GameScreen.kt` | `AUTO_ADVANCE_DELAY_MS = 4000L` - hardcoded in pixels/characters but used only once. Not configurable by parents or users. |

### Performance Considerations

| Concern | Impact | Notes |
|---|---|---|
| No pagination on content endpoints | Medium | `/api/categories/{id}/words` returns ALL words; with hundreds of categories and thousands of words this could be slow. Current seed data is small (~90 words). |
| No database indexing beyond what Exposed creates | Low | Only `username` (uniqueIndex) and composite `(childId, wordId)` are indexed. Foreign key columns (`categoryId`, `parentId`) are not explicitly indexed but H2/PostgreSQL usually create implicit indexes on FKs. |
| Quiz progress endpoint does N+1 queries per category | Medium | `ProgressService.getChildProgressStats()` loops over categories and for each one runs separate queries to count learned words. Could be optimized with JOIN or batch query. |
| Image URLs stored as absolute paths in DB | Low | If `CONTENT_BASE_URL` changes, ALL existing records need updating (or a URL rewrite layer). |

### Outdated / Alpha Dependencies

| Library | Version | Risk | Notes |
|---|---|---|---|
| Backdrop | 2.0.0-alpha03 | Medium | Still alpha; the catalog comment says "2.0.0 stable requires compileSdk 37; alpha03 is the latest that builds with compileSdk 36." If you upgrade compileSdk, this library will need to be replaced or upgraded. |
| Compose Multiplatform | 1.9.3 | Low | Not bleeding edge (latest is likely newer). Stable but may miss new features. |

### Possible Bugs / Unknowns

| Area | Uncertainty | Risk Level |
|---|---|---|
| iOS audio playback | `AudioPlayer.ios.kt` exists as an expect/actual interface, but actual implementation quality unknown without reading the file (file was not fully inspected). | Medium - may not work on iOS. |
| iOS image loading | Similar to audio - `AppImageLoader.ios.kt` is a platform-specific Coil setup. Unverified functionality. | Low-Medium |
| Backward compatibility of seed data URLs | The seeder references imagekit.io-style paths in some URLs (e.g., `?updatedAt=...`). These are now served by local Ktor at `/content/`. If the content directory doesn't have matching files, images will 404. | High if content dir is empty or missing. |
| Multi-child quiz ownership edge case | When guest user passes a childId in quiz query params, it's ignored (safe). But when an authenticated parent passes their OWN childId vs another parent's childId - the `verifyChildOwnership` check prevents cross-parent access. Verified as correct behavior. | Low - handled properly. |

---

## 11. Important Files Map

### Files Developers Will Frequently Modify
| File | Why | Risk if Changed |
|---|---|---|
| `gradle/libs.versions.toml` | Central version catalog - changing a version affects the entire project | HIGH - can break compilation across all modules |
| `composeApp/build.gradle.kts` | Android SDK versions, Compose Multiplatform config, dependencies | MEDIUM - may break iOS or Android builds independently |
| `shared/src/commonMain/kotlin/.../data/*Repository.kt` | API calls and data transformations for every feature | HIGH - changes affect all screens that use the repository |
| `server/src/main/kotlin/.../services/*.kt` | Business logic for auth, children, content, quiz, progress | MEDIUM-HIGH - each is self-contained but interrelated via shared DTOs |
| `composeApp/src/commonMain/kotlin/mohaamadreza/saemipour/no/vazheh/App.kt` | Route definitions, navigation structure, global state wiring | HIGH - changes affect all screen transitions and DI setup |
| `server/src/main/kotlin/.../database/tables/Tables.kt` | Database schema (column types, relationships) | MEDIUM - changing requires updating seeder and all queries referencing those columns |

### Files That Should Rarely Be Touched
| File | Why | Risk if Changed |
|---|---|---|
| `settings.gradle.kts` | Module structure definition | CRITICAL - breaking this prevents the entire build from resolving |
| `server/src/main/kotlin/.../Application.kt` | Server bootstrap, plugin installation order | HIGH - wrong plugin ordering can silently break auth or CORS |
| `shared/src/commonMain/kotlin/.../di/SharedModule.kt` | Koin DI wiring for repositories and HTTP client | MEDIUM - misconfiguration causes runtime injection errors |

### Core Business Logic Locations
| Feature | Location | Description |
|---|---|---|
| Quiz logic (question gen + scoring) | `server/.../services/QuizService.kt` | Random question generation, answer validation, progress tracking with LEARNED_THRESHOLD=3 |
| Parent-child ownership enforcement | Every service that references Children table | FK check on parentId ensures parents only access their own children's data |
| Audio player state management | `composeApp/.../player/AudioProvider.kt` + platform implementations | Cross-screen audio playback, lifecycle tied to Compose composition |
| Timer / Kids Mode | `composeApp/.../ui/viewmodels/ChildViewModel.kt:startTimer()`, `stopTimer()` | Countdown timer with configurable duration; when finished navigates back to mother screen |
| PIN-locked kids mode | `composeApp/.../ui/components/AppPinToggle.kt` + `TokenStorage.getKidsPin()` | Padlock overlay that requires a 4-digit PIN to disable |

---

## 12. How to Safely Modify This Project with AI

### Files AI Should Inspect Before Making Changes

Before any modification, an AI coding agent MUST read these files first:

1. **`gradle/libs.versions.toml`** - Understand which dependency versions are in use and what's available
2. **`composeApp/build.gradle.kts`** - Know the Android target SDK, minSdk, and what dependencies are actually declared for composeApp (not just listed in the version catalog)
3. **`shared/build.gradle.kts`** - Know what dependencies the shared module has access to
4. **`server/build.gradle.kts`** - Know server-side dependencies
5. **`composeApp/src/commonMain/kotlin/mohaamadreza/saemipour/no/vazheh/App.kt`** - Understand routing, screen transitions, and which ViewModels are Koin-scoped vs per-composition
6. **`shared/src/commonMain/kotlin/.../data/AuthStateManager.kt`** - Understand the auth event system before touching any authentication-related code
7. **`server/src/main/kotlin/.../database/tables/Tables.kt`** - Know the exact schema before writing new queries or adding columns

### Common Mistakes an AI Coding Agent Could Make

| Mistake | Why It Happens | How to Avoid |
|---|---|---|
| Adding a dependency that's already in `libs.versions.toml` but not included in the right module's `build.gradle.kts` | Version catalog and actual usage are decoupled | Always check BOTH the version catalog AND the target module's build file |
| Creating new Exposed table definitions without updating `SchemaUtils.create()` in Application.kt | The seeder + SchemaUtils.create must be kept in sync manually | Read Tables.kt AND Application.kt before adding any DB changes |
| Using `kotlinx.coroutines` `launch` instead of `viewModelScope.launch` inside ViewModels | Standard Kotlin coroutines pattern doesn't respect ViewModel lifecycle | Always use `viewModelScope` inside ViewModels for coroutines that should be cancelled on ViewModel destruction |
| Modifying the seeder's data deletion logic incorrectly | The seeder deletes ALL data every startup - a new developer might not realize this | Read DatabaseSeeder.kt thoroughly before modifying seed data; understand that parent-created custom words survive but categories/words are wiped |
| Assuming iOS platform functions work identically to Android | expect/actual implementations may differ in behavior (audio, permissions, file paths) | Always check BOTH androidMain AND iosMain platform implementations before writing new cross-platform code |
| Hardcoding `LEARNED_THRESHOLD` in a new screen | It's duplicated between server and client - changing one without the other causes silent bugs | Reference the existing constant or document the synchronization requirement explicitly |
| Adding routes to App.kt without adding them to the OpenAPI spec (documentation.yaml) | The Swagger docs are manually maintained and out of sync with code | Always update `server/src/main/resources/openapi/documentation.yaml` when adding new endpoints |

### Important Constraints

1. **No backend migrations**: Database schema changes require updating BOTH `Tables.kt` AND `SchemaUtils.create()` in Application.kt. There is no Flyway, Liquibase, or Exposed migration DSL in use.
2. **Server services bypass Koin**: Services are Kotlin object singletons that directly access the database via Exposed transactions. They do NOT go through the DI container. Do not try to inject them with Koin annotations - it won't work.
3. **Audio player is global**: The `AudioProvider` composable wraps the entire screen tree. You cannot have two independent audio players active simultaneously; they share one underlying `AudioPlayer` instance per platform.
4. **Children are not users**: There is no child account system. Children belong to parents. All child-scoped data access requires a parent's JWT token with matching parentId.
5. **RTL layout is forced**: `LocalLayoutDirection provides LayoutDirection.RTL` is set in most screens. Do NOT remove this - the app targets Persian-speaking users and all layouts are built with RTL as the base direction.

### Recommended Workflow for Future Tasks

```
1. Read gradle/libs.versions.toml to understand available dependencies
2. Read the relevant build.gradle.kts to see what's actually declared for the target module
3. Read existing code in the area you're modifying (patterns, naming conventions)
4. For backend changes: check Tables.kt first - any new DB feature needs a table or column here
5. For frontend changes: check App.kt for routing setup; check existing ViewModels for state patterns
6. If adding an API endpoint: also update documentation.yaml so Swagger stays current
7. Test on Android emulator FIRST (most common development target); iOS requires separate macOS environment
8. Run `./gradlew :server:run` to verify server changes before touching the app
9. Be aware that DatabaseSeeder deletes ALL seed data on startup - test with real data if needed
```

---

## 13. Final Summary

### One-Page Overview for a New Engineer

**NoVazheh (��ǎ�) is a Kotlin Multiplatform speech therapy application** built as a three-module Gradle project: `composeApp` (Android + iOS UI via Compose Multiplatform), `shared` (cross-platform networking, repositories, DI), and `server` (Ktor REST API with PostgreSQL/H2 backend).

The app serves two user groups: **parents** (registered accounts who manage their children's learning) and **children** (unauthenticated records associated with a parent, whose progress is tracked through gamified vocabulary quizzes). Parents can record custom Persian/English words with audio using the device microphone. The child-facing interface presents words as flashcards with audio playback, then tests retention through multiple mini-games: quiz (4-option multiple choice), memory card matching, color sorting, shadow matching, and odd-one-out identification.

The backend is a simple stateless Ktor server on Netty (port 8080). Authentication uses JWT tokens signed with HMAC-SHA256. The database uses JetBrains Exposed ORM - in development it's H2 in file mode; production targets PostgreSQL 16. A database seeder wipes and re-seeds ~90 words across 10 categories on every startup, with parent-created custom words persisting across restarts.

The frontend is a Material 3 Compose Multiplatform app with Navite Compose Navigation for routing. State lives in Koin-scoped ViewModels backed by MutableStateFlow. Audio playback uses a single global `AudioProvider` composable that wraps all screens. Images load via Coil 3 with disk and memory caching. The bottom navigation bar uses the Backdrop library for a glass-blur liquid effect.

The project is an academic/thesis companion app (LaTeX thesis source exists in `doc/`) and remains at v1.0 MVP stage. No CI/CD, no production Docker setup, no encryption beyond JWT signing. The codebase is functional but needs security hardening before any public deployment.

---

### Top 10 Things a Developer Must Know Before Touching This Codebase

1. **This is KMP with three modules** (`composeApp`, `shared`, `server`). Changes to dependencies in the version catalog (`gradle/libs.versions.toml`) affect all targets - verify each module's build file actually includes what you think it should.

2. **The server has NO database migrations.** Schema changes require manual updates to both `Tables.kt` and `SchemaUtils.create()` in Application.kt, plus the seeder must be updated if seed data references new columns.

3. **JWT secret defaults to an insecure value** (`novazheh-super-secret-key-2024`). You MUST set `JWT_SECRET` environment variable before any production or semi-public deployment. Anyone with this default can forge valid tokens.

4. **Children are not users** - they have no login, no separate accounts, and their data is always accessed through a parent's JWT token via ownership checks (`parentId` matching). Do not build a child authentication system; it doesn't exist and the architecture assumes parents own children.

5. **All server services are Kotlin object singletons**, not injected by Koin. They directly call Exposed ORM transactions. You cannot mock them in tests without refactoring, and you should not add `@Inject` annotations to them - they bypass DI entirely.

6. **The database seeder wipes all seed data on startup.** If a parent has created custom words or categories, those survive (the seeder doesn't touch `custom_words`). But if the seeder is modified to also clear custom words, parent data will be lost. Always read `DatabaseSeeder.kt` before editing it.

7. **RTL layout is fundamental** - nearly every screen wraps its content with `LocalLayoutDirection provides LayoutDirection.RTL`. The entire UI was designed for Persian-speaking users from the ground up. Do not remove or change this; it would break all layouts.

8. **Audio player lifecycle is global and cross-screen.** A single `AudioProvider` composable owns one audio player per platform. Audio continues playing when navigating between screens within that composition scope. There is no per-screen audio isolation.

9. **iOS support exists but was not fully verified in this review.** The iOS module has the expected/actual scaffolding (`iosMain/kotlin/...`) for HTTP client, settings storage, image loading, audio player, and audio recorder - but the actual implementation quality of these platform-specific files was outside the scope of this analysis. Expect potential issues on iOS first-time runs.

10. **No testing infrastructure exists beyond a single server unit test.** There are no Compose UI tests, no integration tests, and no end-to-end tests. Before making changes to critical paths (auth, quiz scoring, progress tracking), manually verify behavior - the codebase relies entirely on manual QA.

---

*Document generated August 2025 based on full source code review of F:/University-Project/NoVazhe.*
