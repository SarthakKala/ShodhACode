# Shodh-a-Code Contest Platform

A real-time coding contest platform built with **Spring Boot** (backend) and **React** (frontend), featuring live code judging, instant feedback, and dynamic leaderboards.

![Tech Stack](https://img.shields.io/badge/Backend-Spring%20Boot-green) ![Frontend](https://img.shields.io/badge/Frontend-React%20+%20Vite-blue) ![Database](https://img.shields.io/badge/Database-H2-orange) ![Styling](https://img.shields.io/badge/Styling-Tailwind%20CSS-purple)

## 🎯 Project Overview

This platform enables students to participate in coding contests with real-time judging and leaderboard updates. The system accepts code submissions, evaluates them against test cases, and provides instant feedback while maintaining live rankings.

## 🏗️ Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   React     │────▶│  Spring Boot │────▶│  H2 Database│
│   Frontend  │◀────│   Backend    │◀────│   (In-Memory)│
└─────────────┘     └─────────────┘     └─────────────┘
     │                    │
     │                    │
     ▼                    ▼
 [User Interface]    [Code Judge Engine]
```

## 🚀 Quick Start

### Prerequisites

- **Docker** and **Docker Compose** (recommended for production-like setup)
- OR **Java 21+**, **Node.js 18+**, **Maven 3.8+** (for local development)

---

### Option 1: Docker Setup (Recommended)

This is the **easiest and recommended** way to run the entire application with Docker containers.

#### Step 1: Build the Judge Container Image

```bash
# Build the code execution environment
docker build -t shodh-judge:latest ./docker/judge
```

#### Step 2: Start All Services with Docker Compose

```bash
# From project root directory
docker-compose up --build
```

This will:

- Build and start the **backend** on `http://localhost:8080`
- Build and start the **frontend** on `http://localhost:3000`
- Create the **judge container** for secure code execution

#### Step 3: Access the Application

1. Open `http://localhost:3000` in your browser
2. Use **Contest ID: 1** for the demo contest
3. Enter any username to join

#### To Stop All Services

```bash
docker-compose down
```

---

### Option 2: Local Development Setup

For development without Docker (code will execute locally - less secure):

#### 1. Start the Backend

```bash
# Navigate to backend directory
cd backend

# Install dependencies and run
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

#### 2. Start the Frontend

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install --legacy-peer-deps

# Start development server
npm run dev
```

The frontend will start on `http://localhost:5173`

#### 3. Access the Application

1. Open `http://localhost:5173` in your browser
2. Use **Contest ID: 1** for the demo contest
3. Enter any username to join

## 📁 Project Structure

```
shodh-a-code/
├── backend/                  # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/shodh/backend/
│   │       ├── config/      # Configuration (CORS, Async, Data Init)
│   │       ├── controller/  # REST API endpoints
│   │       ├── dto/        # Data Transfer Objects
│   │       ├── model/      # JPA Entities
│   │       ├── repository/ # Database repositories
│   │       └── service/    # Business logic
│   └── pom.xml
│
├── frontend/                # React Frontend
│   ├── src/
│   │   ├── components/    # Reusable components
│   │   ├── pages/        # Page components
│   │   ├── services/     # API service layer
│   │   └── index.css     # Global styles
│   └── package.json
│
└── README.md
```

## 🔧 API Endpoints

### Contest Management

| Method | Endpoint                                | Description                       |
| ------ | --------------------------------------- | --------------------------------- |
| GET    | `/api/contests/{contestId}`             | Get contest details with problems |
| GET    | `/api/contests/{contestId}/leaderboard` | Get live leaderboard              |

### Submission Management

| Method | Endpoint                          | Description             |
| ------ | --------------------------------- | ----------------------- |
| POST   | `/api/submissions`                | Submit code for judging |
| GET    | `/api/submissions/{submissionId}` | Get submission status   |

### Request/Response Examples

#### Submit Code

```json
POST /api/submissions
{
  "username": "alice",
  "contestId": 1,
  "problemId": 1,
  "code": "class Solution { ... }",
  "language": "java"
}
```

#### Response

```json
{
  "submissionId": 1,
  "status": "PENDING",
  "username": "alice",
  "problemId": 1,
  ...
}
```

## 🎨 Features

### Frontend Features

- **Modern Dark Theme** with glass morphism effects
- **Monaco Code Editor** with syntax highlighting
- **Real-time Status Updates** (polling every 2-3 seconds)
- **Live Leaderboard** (updates every 15 seconds)
- **Responsive Design** for all screen sizes

### Backend Features

- **Asynchronous Code Judging** with thread pool execution
- **Multiple Submission Statuses**: PENDING, RUNNING, ACCEPTED, WRONG_ANSWER, etc.
- **Test Case Management** with sample and hidden test cases
- **Pre-populated Data** for immediate testing
- **CORS Configuration** for frontend integration

## 💾 Database Schema

### Core Entities

- **User**: Stores participant information
- **Contest**: Contest details and timing
- **Problem**: Problem statements with constraints
- **TestCase**: Input/output test cases
- **Submission**: Code submissions and results

### Pre-populated Data

- **2 Contests**:
  - Contest 1 (Active): 3 problems - Two Sum, Palindrome Number, FizzBuzz
  - Contest 2 (Future): 1 problem
- **3 Sample Users**: alice, bob, charlie

## 🔄 Submission Flow

1. User submits code through the frontend
2. Backend receives submission and returns `submissionId`
3. Submission queued with `PENDING` status
4. Judge service processes submission asynchronously
5. Status updates to `RUNNING`
6. Code executed against test cases
7. Final verdict determined (`ACCEPTED`, `WRONG_ANSWER`, etc.)
8. Frontend polls for status updates
9. Leaderboard updates automatically

## 🛠️ Technology Stack

### Backend

- **Spring Boot 3.5.7** - REST API framework
- **Spring Data JPA** - Database ORM
- **H2 Database** - In-memory database
- **Lombok** - Reduce boilerplate code
- **Maven** - Dependency management

### Frontend

- **React 18** - UI library
- **Vite** - Build tool
- **Tailwind CSS** - Utility-first CSS
- **Monaco Editor** - Code editor
- **Axios** - HTTP client
- **React Router** - Navigation
- **Framer Motion** - Animations
- **React Hot Toast** - Notifications

## 🎯 Design Decisions

### Architectural Choices

1. **Microservice-Ready Architecture**: Backend and frontend are completely decoupled, communicating only through REST APIs.

2. **Asynchronous Processing**: Code judging happens asynchronously to prevent blocking API responses.

3. **Polling vs WebSockets**: Chose polling for simplicity and to avoid WebSocket complexity in the prototype.

4. **In-Memory Database**: H2 for rapid development and testing without external dependencies.

### Frontend Design

1. **Glass Morphism UI**: Modern, visually appealing dark theme with transparency effects.

2. **Component-Based Architecture**: Reusable components for maintainability.

3. **State Management**: Local component state with props drilling (sufficient for current scope).

### Backend Design

1. **Service Layer Pattern**: Clear separation between controllers, services, and repositories.

2. **DTO Pattern**: Separate data transfer objects from entities for API flexibility.

3. **Repository Pattern**: Abstract database operations for easy switching between databases.

## 🐳 Docker Architecture

> **📚 For detailed Docker setup, see [DOCKER_SETUP.md](DOCKER_SETUP.md)**

### Code Execution Flow with Docker

1. **User submits code** → Backend receives submission
2. **Backend writes code** to temporary file in `/tmp/judge`
3. **Backend spawns Docker container** (`shodh-judge:latest`) with:
   - User code mounted as volume
   - Memory limit (e.g., 256MB)
   - CPU limit (1 core)
   - Network disabled (security)
   - Time limit enforced
4. **Container compiles code** (if needed: Java, C++)
5. **Container executes code** against test cases
6. **Backend captures stdout** and compares with expected output
7. **Container auto-removed** after execution
8. **Temp files cleaned up**

### Security Features

- **Network Isolation**: Containers run with `--network none`
- **Non-root User**: Code executes as `coderunner` user (UID 1000)
- **Resource Limits**: Memory and CPU constraints enforced
- **Auto-cleanup**: Containers and temp files removed after execution
- **Time Limits**: Process killed if exceeds time limit

### Judge Container Contents

The `shodh-judge:latest` image includes:

- **OpenJDK 17** (Java)
- **Python 3**
- **GCC/G++** (C++)
- **Node.js 18** (JavaScript)

## ⚠️ Current Limitations

1. **Authentication**: No user authentication system
2. **Database**: Uses in-memory H2 (data lost on restart)
3. **Memory Tracking**: Approximate memory usage (requires Docker stats API for precision)

## 🔮 Future Enhancements

- [x] Docker containerization for secure code execution ✅
- [x] Support for multiple programming languages (Java, Python, C++, JavaScript) ✅
- [ ] User authentication and authorization
- [ ] WebSocket for real-time updates
- [ ] PostgreSQL for production database
- [ ] Admin panel for contest management
- [ ] Code execution metrics and analytics
- [ ] Problem difficulty ratings
- [ ] User profiles and statistics
- [ ] Advanced Docker resource monitoring with stats API

## 🧪 Testing the Application

1. **Join Contest**: Use Contest ID `1` and any username
2. **Select Problem**: Choose from Two Sum, Palindrome Number, or FizzBuzz
3. **Write Code**: Use the Monaco editor to write your solution
4. **Submit**: Click submit and watch the real-time status updates
5. **View Leaderboard**: Switch to leaderboard tab to see rankings

### Sample Solutions (Function-Only, LeetCode Style)

#### Two Sum (Java)

```java
public int[] twoSum(int[] nums, int target) {
    java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[] { map.get(complement), i };
        }
        map.put(nums[i], i);
    }
    return new int[] { 0, 0 };
}
```

#### Palindrome Number (Java)

```java
public boolean isPalindrome(int x) {
    if (x < 0) return false;
    int original = x, reversed = 0;
    while (x != 0) {
        int digit = x % 10;
        reversed = reversed * 10 + digit;
        x /= 10;
    }
    return reversed == original;
}
```

#### FizzBuzz (Java)

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

#### Two Sum (Python)

```python
def two_sum(nums, target):
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return [0, 0]
```

#### Two Sum (C++)

```cpp
vector<int> twoSum(vector<int>& nums, int target) {
    unordered_map<int, int> seen;
    for (int i = 0; i < nums.size(); i++) {
        int complement = target - nums[i];
        if (seen.count(complement)) {
            return {seen[complement], i};
        }
        seen[nums[i]] = i;
    }
    return {0, 0};
}
```

#### Two Sum (JavaScript)

```javascript
function twoSum(nums, target) {
  const seen = new Map();
  for (let i = 0; i < nums.length; i++) {
    const complement = target - nums[i];
    if (seen.has(complement)) {
      return [seen.get(complement), i];
    }
    seen.set(nums[i], i);
  }
  return [0, 0];
}
```

## 🔧 Docker Troubleshooting

### Issue: Backend can't spawn Docker containers

**Problem**: Backend is running in Docker but can't execute judge containers.

**Solution**: Ensure Docker socket is mounted in `docker-compose.yml`:

```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

### Issue: Permission denied on Docker socket

**Solution**:

```bash
# On Linux, add your user to docker group
sudo usermod -aG docker $USER
# Then log out and back in
```

### Issue: Judge image not found

**Solution**: Build the judge image first:

```bash
docker build -t shodh-judge:latest ./docker/judge
```

### Issue: Port already in use

**Solution**: Change ports in `docker-compose.yml` or stop conflicting services:

```bash
# Check what's using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Mac/Linux
```

## 📝 License

This project is built as an assessment for Shodh AI.

## 🤝 Contributing

This is an assessment project. For any questions or issues, please refer to the documentation above.

---

Built with ❤️ using Spring Boot and React
