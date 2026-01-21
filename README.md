# Shodh-a-Code Contest Platform

A real-time coding contest platform built with **Spring Boot** (backend) and **React** (frontend), featuring live code judging, instant feedback, and dynamic leaderboards.

## Live Demo

- **Frontend**: https://shodh-a-code-66uq.vercel.app/
- **Backend API**: https://shodh-ai-backend.onrender.com
- **Health Check**: https://shodh-ai-backend.onrender.com/api/health

Test with **Contest ID: 1** and any username.

---

## Setup Instructions

### Prerequisites

- **Docker** and **Docker Compose**
- OR **Java 21+**, **Node.js 18+**, **Maven 3.8+** (for local development)

### Running with Docker Compose (Recommended)

This is the simplest way to run the entire application locally using a single `docker-compose.yml` file.

**Step 1: Build the Judge Container**

```bash
docker build -t shodh-judge:latest ./docker/judge
```

**Step 2: Start All Services**

```bash
docker-compose up --build
```

This single command will:

- Build and start the backend on `http://localhost:8080`
- Build and start the frontend on `http://localhost:3000`
- Configure the judge container for secure code execution

**Step 3: Access the Application**

Open `http://localhost:3000` in your browser, enter Contest ID `1` and any username.

**To Stop:**

```bash
docker-compose down
```

### Local Development (Without Docker)

**Backend:**

```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080
```

**Frontend:**

```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
# Runs on http://localhost:5173
```

---

## API Design

### Endpoints Overview

| Method | Endpoint                                | Description                                    |
| ------ | --------------------------------------- | ---------------------------------------------- |
| GET    | `/api/contests/{contestId}`             | Fetch contest details and problems             |
| POST   | `/api/submissions`                      | Submit code for judging (returns submissionId) |
| GET    | `/api/submissions/{submissionId}`       | Get submission status                          |
| GET    | `/api/contests/{contestId}/leaderboard` | Get live leaderboard                           |

### Request/Response Formats

#### Submit Code

**Request:**

```json
POST /api/submissions
{
  "username": "alice",
  "contestId": 1,
  "problemId": 1,
  "code": "public int[] twoSum(int[] nums, int target) { ... }",
  "language": "java"
}
```

**Response:**

```json
{
  "submissionId": 42,
  "status": "PENDING",
  "username": "alice",
  "problemId": 1,
  "problemTitle": "Two Sum",
  "submittedAt": "2025-10-30T02:15:30"
}
```

#### Get Submission Status

**Request:**

```
GET /api/submissions/42
```

**Response:**

```json
{
  "submissionId": 42,
  "status": "ACCEPTED",
  "executionTime": 145,
  "memoryUsed": 2048,
  "output": "0 1",
  "error": null
}
```

**Status Values:** `PENDING`, `RUNNING`, `ACCEPTED`, `WRONG_ANSWER`, `TIME_LIMIT_EXCEEDED`, `RUNTIME_ERROR`, `COMPILATION_ERROR`

#### Get Contest

**Response:**

```json
{
  "id": 1,
  "title": "Weekly Coding Contest #1",
  "isActive": true,
  "participantsCount": 5,
  "problems": [
    {
      "id": 1,
      "title": "Two Sum",
      "description": "...",
      "sampleInput": "4 9\n2 7 11 15",
      "sampleOutput": "0 1",
      "timeLimit": 1,
      "memoryLimit": 256,
      "points": 100
    }
  ]
}
```

#### Get Leaderboard

**Response:**

```json
{
  "contestId": 1,
  "lastUpdated": "2025-10-30T02:30:00",
  "entries": [
    {
      "rank": 1,
      "username": "alice",
      "problemsSolved": 3,
      "totalPoints": 350,
      "totalTime": 1800000
    }
  ]
}
```

---

## Design Choices & Justification

### How Services are Structured on the Backend

The backend follows a **layered service architecture**:

**1. Controller Layer** (`controller/`)

- Handles HTTP requests and responses
- Validates input using `@Valid` annotations
- Delegates business logic to services
- Returns DTOs, never exposes JPA entities directly

**2. Service Layer** (`service/`)

- **ContestService**: Retrieves contest data and transforms entities to DTOs
- **SubmissionService**: Manages submission lifecycle, creates users, triggers async judging
- **JudgeService**: Orchestrates Docker containers, wraps user functions, executes code, validates output
- **LeaderboardService**: Calculates rankings with multi-criteria sorting (problems solved → points → time)

**3. Repository Layer** (`repository/`)

- JPA interfaces extending `JpaRepository`
- Custom queries for complex operations (e.g., finding accepted submissions)
- Abstracts database operations

**Why this structure?**

- Clear separation of concerns makes code maintainable
- Easy to test (mock services in unit tests)
- Flexible to swap implementations (H2 → PostgreSQL requires only config changes)

### Frontend State Management Approach

**Chosen Approach:** Local component state with React hooks (`useState`, `useEffect`)

**Why not Redux/Context API?**

- **Simplicity**: State is limited to contest data, code, and leaderboard
- **Shallow component tree**: Only 2-3 levels, prop drilling is manageable
- **Performance**: No global state overhead, components re-render only when their data changes
- **Development speed**: Faster iteration without boilerplate

**State Distribution:**

- `ContestPage`: Contest data, selected problem, active tab
- `CodeEditor`: Per-language code persistence, submission status
- `Leaderboard`: Rankings data with auto-refresh

**Persistence Strategy:**

- Code stored in `localStorage` per language and problem
- Survives browser refresh
- No backend storage needed for draft code

### Docker Orchestration Challenges & Trade-offs

#### Challenge 1: Docker-in-Docker for Code Execution

**Problem:** Backend (running in Docker) needs to spawn judge containers on the host

**Solution:** Mount Docker socket (`/var/run/docker.sock`) into backend container

**Trade-offs:**

- **Pros**: Simple implementation, works immediately, no nested Docker daemon
- **Cons**: Backend gets full Docker host access (security concern)
- **Production Alternative**: Use Kubernetes Jobs or Docker socket proxy with restricted permissions

#### Challenge 2: File Sharing Between Containers

**Problem:** Judge containers must access code files written by backend container

**Attempted Solution:** Docker volumes between containers - failed due to sibling container isolation

**Final Solution:** Host-mounted temp directory (`/tmp/judge`)

- Backend writes code files to host-mounted volume
- Judge containers mount the same host directory
- Both can read/write reliably

**Trade-offs:**

- **Pros**: Reliable, simple, works across all platforms
- **Cons**: Leaves temporary files on host (mitigated with cleanup code in `finally` blocks)

#### Challenge 3: Multi-Language Execution

**Problem:** Supporting Java, Python, C++, JavaScript with different compilation/execution workflows

**Solution:**

- Built single judge image with all runtimes (OpenJDK 17, Python 3, GCC, Node.js)
- Created language-specific code wrappers (`buildJavaProgramFromFunction`, etc.)
- Conditional compilation (Java/C++ compile first, Python/JS direct execution)

**Trade-offs:**

- **Large image size** (~500MB) vs **fast startup** (image pre-pulled, no per-language switching)
- **Complexity** in code parsing vs **better UX** (users write function-only code like LeetCode)

#### Challenge 4: Security and Resource Limits

**Problem:** Prevent malicious/buggy code from affecting host system

**Solutions Implemented:**

- **Network isolation**: `--network none` (no internet access)
- **Non-root execution**: Code runs as `coderunner` user (UID 1000)
- **Resource limits**: `--memory 256m`, `--cpus 1`
- **Time limits**: Process killed after timeout
- **Auto-cleanup**: Containers removed with `--rm` flag

**Trade-offs:**

- **Memory tracking**: Using Docker flags vs Docker Stats API
  - **Chose flags**: Simpler, good enough for prototype
  - **Missing**: Precise memory usage reporting
- **Cleanup reliability**: Manual temp file deletion vs Docker volume lifecycle
  - **Chose manual**: More control, immediate cleanup
  - **Risk**: Files left behind if JVM crashes (rare)

---

## Project Structure

```
shodh-a-code/
├── backend/                  # Spring Boot Backend
│   ├── src/main/java/com/shodh/backend/
│   │   ├── config/          # CORS, Async, Data Initialization
│   │   ├── controller/      # REST API Endpoints
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── model/          # JPA Entities
│   │   ├── repository/     # Database Repositories
│   │   └── service/        # Business Logic & Judge Engine
│   ├── Dockerfile          # Backend container image
│   └── pom.xml
├── frontend/                # React Frontend
│   ├── src/
│   │   ├── components/     # Reusable UI Components
│   │   ├── pages/         # Page Components
│   │   └── services/      # API Integration Layer
│   ├── Dockerfile         # Frontend container image
│   └── package.json
├── docker/
│   └── judge/
│       └── Dockerfile     # Multi-language execution environment
└── docker-compose.yml     # Single-file orchestration
```

---

## Technology Stack

**Backend:** Spring Boot 3.5.7, Spring Data JPA, H2 Database, Lombok, Maven

**Frontend:** React 18, Vite 5, Tailwind CSS, Monaco Editor, Axios, React Router

**DevOps:** Docker, Docker Compose, Nginx

---

## Sample Solutions

Users write **function-only code**. Backend wraps it into a complete program.

### Two Sum (Java)

```java
public int[] twoSum(int[] nums, int target) {
    java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        if (map.containsKey(target - nums[i])) {
            return new int[] { map.get(target - nums[i]), i };
        }
        map.put(nums[i], i);
    }
    return new int[] { 0, 0 };
}
```

### Palindrome Number (Java)

```java
public boolean isPalindrome(int x) {
    if (x < 0) return false;
    int original = x, reversed = 0;
    while (x != 0) {
        reversed = reversed * 10 + x % 10;
        x /= 10;
    }
    return reversed == original;
}
```

### FizzBuzz (Java)

```java
public java.util.List<String> fizzBuzz(int n) {
    java.util.List<String> result = new java.util.ArrayList<>();
    for (int i = 1; i <= n; i++) {
        if (i % 15 == 0) result.add("FizzBuzz");
        else if (i % 3 == 0) result.add("Fizz");
        else if (i % 5 == 0) result.add("Buzz");
        else result.add(String.valueOf(i));
    }
    return result;
}
```

---

## License

Built as an assessment for Shodh AI.
