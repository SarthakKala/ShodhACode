# Shodh-a-Code Backend API

## Overview

Spring Boot backend for the Shodh-a-Code Contest Platform, featuring REST APIs for contest management, code submission, and real-time judging.

## Prerequisites

- Java 21+
- Maven 3.8+
- (Optional) Docker for containerized code execution

## Running the Application

### Development Mode

```bash
# Navigate to backend directory
cd backend

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Database

- Uses H2 in-memory database (for development)
- H2 Console available at: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (leave empty)

## API Endpoints

### Health Check

- **GET** `/api/health`
  - Returns application health status

### Contest Endpoints

#### Get Contest Details

- **GET** `/api/contests/{contestId}`
- **Response:**

```json
{
  "id": 1,
  "title": "Weekly Coding Contest #1",
  "description": "Test your programming skills!",
  "startTime": "2024-01-01T10:00:00",
  "endTime": "2024-01-01T13:00:00",
  "problems": [
    {
      "id": 1,
      "title": "Two Sum",
      "description": "...",
      "constraints": "...",
      "sampleInput": "4 9\n2 7 11 15",
      "sampleOutput": "0 1",
      "timeLimit": 1,
      "memoryLimit": 256,
      "points": 100,
      "totalSubmissions": 0,
      "acceptedSubmissions": 0
    }
  ],
  "participantsCount": 0,
  "isActive": true
}
```

#### Get Contest Leaderboard

- **GET** `/api/contests/{contestId}/leaderboard`
- **Response:**

```json
{
  "contestId": 1,
  "contestTitle": "Weekly Coding Contest #1",
  "lastUpdated": "2024-01-01T12:00:00",
  "entries": [
    {
      "rank": 1,
      "username": "alice",
      "problemsSolved": 3,
      "totalPoints": 350,
      "totalTime": 3600000,
      "lastAcceptedAt": "2024-01-01T11:30:00"
    }
  ]
}
```

### Submission Endpoints

#### Submit Code

- **POST** `/api/submissions`
- **Request Body:**

```json
{
  "username": "alice",
  "contestId": 1,
  "problemId": 1,
  "code": "class Solution { ... }",
  "language": "java"
}
```

- **Response:**

```json
{
  "submissionId": 1,
  "username": "alice",
  "problemId": 1,
  "problemTitle": "Two Sum",
  "code": "...",
  "language": "java",
  "status": "PENDING",
  "executionTime": null,
  "memoryUsed": null,
  "output": null,
  "error": null,
  "submittedAt": "2024-01-01T11:00:00"
}
```

#### Get Submission Status

- **GET** `/api/submissions/{submissionId}`
- **Response:** Same as submission response above, with updated status
- **Possible Status Values:**
  - `PENDING` - Submission queued for processing
  - `RUNNING` - Currently being executed
  - `ACCEPTED` - All test cases passed
  - `WRONG_ANSWER` - Output doesn't match expected
  - `TIME_LIMIT_EXCEEDED` - Execution took too long
  - `MEMORY_LIMIT_EXCEEDED` - Used too much memory
  - `RUNTIME_ERROR` - Code crashed during execution
  - `COMPILATION_ERROR` - Code failed to compile

## Pre-populated Test Data

The application automatically creates sample data on startup:

### Contests

1. **Contest ID: 1** - "Weekly Coding Contest #1" (Active)

   - 3 problems: Two Sum, Palindrome Number, FizzBuzz
   - Each problem has 2-3 test cases

2. **Contest ID: 2** - "Monthly Challenge Contest" (Future)
   - 1 problem: Reverse Integer

### Sample Users

- alice
- bob
- charlie

## Project Structure

```
backend/
├── src/main/java/com/shodh/backend/
│   ├── config/          # Configuration classes (CORS, Async, Data initialization)
│   ├── controller/      # REST controllers
│   ├── dto/            # Data Transfer Objects
│   ├── exception/      # Global exception handling
│   ├── model/          # JPA entities
│   ├── repository/     # JPA repositories
│   └── service/        # Business logic services
└── src/main/resources/
    └── application.properties  # Application configuration
```

## Code Submission Flow

1. User submits code via POST `/api/submissions`
2. Submission is saved with `PENDING` status
3. Asynchronous processing begins:
   - Status changes to `RUNNING`
   - Code is executed against test cases
   - Results are compared with expected outputs
   - Final status is updated (ACCEPTED, WRONG_ANSWER, etc.)
4. Frontend polls GET `/api/submissions/{id}` for status updates

## Security Notes

⚠️ **Current implementation executes Java code locally for testing purposes only.**

In production, code execution should be:

- Containerized using Docker
- Resource-limited (CPU, memory, disk)
- Network-isolated
- Time-bounded

## Future Enhancements

- [ ] Docker integration for secure code execution
- [ ] Support for multiple programming languages (Python, C++, JavaScript)
- [ ] WebSocket support for real-time updates
- [ ] User authentication and authorization
- [ ] PostgreSQL for production database
- [ ] Rate limiting for submissions
- [ ] Admin panel for contest management

