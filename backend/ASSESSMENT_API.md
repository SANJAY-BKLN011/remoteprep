# RemotePrep Assessment Completion & Final Scoring API (Phase 13)

## Overview

The Assessment Completion API enables permanently finalizing a candidate's assessment attempt, calculating authoritative scores for both the Aptitude and DSA sections, updating the database atomically, and returning a comprehensive, client-safe result summary.

---

## Endpoint: Complete Assessment

`POST /api/assessment/{assessmentId}/complete`

### Purpose
Permanently locks the assessment, calculates final Aptitude and DSA scores server-side, records completion timestamps, transitions the status to `COMPLETED`, and returns the final grade.

### Security Guarantees
- The client cannot supply authoritative scores, status, or timestamps. Only the URL path parameter `assessmentId` is accepted.
- Hidden test cases, compiler logs, source code, and database internals are strictly omitted from responses.
- Completed assessments can never transition back to `IN_PROGRESS`.

---

## Complete Submission Flow

```
Student
   ↓
Start Assessment (`POST /api/students/start`)
   ↓
Generate Aptitude Exam (`POST /api/aptitude/generateExam`)
   ↓
Submit Aptitude Answers (`POST /api/aptitude/submitAnswers`)
   ↓
Generate DSA Exam (`POST /api/dsa/exam`)
   ↓
Run DSA Code (`POST /api/dsa/run`)
   ↓
Submit DSA Solutions (`POST /api/dsa/submit`)
   ↓
Complete Assessment (`POST /api/assessment/{assessmentId}/complete`)
   ↓
Calculate Final Score (Server-Side)
   ↓
Mark Assessment `COMPLETED`
   ↓
Return Final Result
```

---

## Scoring System

RemotePrep computes integer-based final scores server-side without percentages:

### 1. Aptitude Scoring
- Consists of exactly 20 assigned multiple-choice questions.
- Each correct answer equals 1 point. Unanswered or skipped questions count as 0.
- Maximum Aptitude Score: **20**

### 2. DSA Scoring
- Consists of exactly 2 assigned problems: 1 EASY and 1 MEDIUM.
- Evaluated strictly using the candidate's **latest submission** per problem (ordered by `submitted_at DESC, id DESC`).
- Scoring breakdown:
  - **EASY problem**:
    - `ACCEPTED` = 1 point
    - Any other verdict (`WRONG_ANSWER`, `COMPILATION_ERROR`, etc.) = 0 points
  - **MEDIUM problem**:
    - `ACCEPTED` = 2 points
    - Any other verdict = 0 points
- Maximum DSA Score: **3**

### 3. Total Score
$$\text{Total Score} = \text{Aptitude Score} + \text{DSA Score}$$
- Maximum Total Score: **23** (20 Aptitude + 3 DSA)

---

## Idempotency Rules
- Calling `POST /api/assessment/{assessmentId}/complete` multiple times on an already-`COMPLETED` assessment is **idempotent**.
- It returns HTTP `200 OK` with the exact previously finalized result.
- It will NOT recalculate scores, alter `completed_at`, or generate duplicate rows.

---

## Immutability Rules
Once an assessment is `COMPLETED`:
- `POST /api/aptitude/generateExam` $\rightarrow$ Rejected (`409 Conflict` / `400 Bad Request`)
- `POST /api/aptitude/submitAnswers` $\rightarrow$ Rejected (`409 Conflict` / `400 Bad Request`)
- `POST /api/dsa/exam` $\rightarrow$ Rejected (`409 Conflict` / `400 Bad Request`)
- `POST /api/dsa/run` $\rightarrow$ Rejected (`409 Conflict` / `400 Bad Request`)
- `POST /api/dsa/submit` $\rightarrow$ Rejected (`409 Conflict` / `400 Bad Request`)

---

## Request & Response Example

### Request
```http
POST /api/assessment/1/complete
Host: localhost:8080
```

### Successful Response (HTTP 200 OK)
```json
{
  "assessmentId": 1,
  "studentId": 5,
  "studentName": "Rahul",
  "rollNumber": "23A01",
  "aptitudeScore": 17,
  "aptitudeTotal": 20,
  "dsaScore": 3,
  "dsaTotal": 3,
  "totalScore": 20,
  "totalMarks": 23,
  "status": "COMPLETED",
  "completedAt": "2026-09-04T16:30:00"
}
```

---

## Error Handling

| Status Code | Description | Example Reason |
| :--- | :--- | :--- |
| **404 Not Found** | Assessment does not exist | `Assessment not found with ID: 999` |
| **400 Bad Request** | Missing student association | `Assessment belongs to no student` |
| **409 Conflict** | Incompletable assessment state | Aptitude exam ungenerated, Aptitude exam unsubmitted, DSA exam ungenerated, or DSA questions have 0 submissions |
| **500 Internal Error** | Server-side execution exception | Unexpected database failure |

---

# RemotePrep Final Result Retrieval API (Phase 14)

## Endpoint: Get Final Result

`GET /api/assessment/{assessmentId}/result`

### Purpose
Provides a strictly read-only endpoint that returns the authoritative server-side final result of an assessment after completion.

### Key Guarantees
- **Strictly Read-Only**: Performs zero score calculations, database mutations, or code executions.
- **Client-Safe**: Returns persisted summary values without exposing source code, hidden test cases, JPA internal graphs, or compiler outputs.
- **Deterministic**: Calling the endpoint repeatedly returns identical results.

### Request
```http
GET /api/assessment/{assessmentId}/result
Host: localhost:8080
```

### Successful Response (HTTP 200 OK)
```json
{
  "assessmentId": 1,
  "studentId": 5,
  "studentName": "Rahul",
  "rollNumber": "23A01",
  "aptitudeScore": 17,
  "aptitudeTotal": 20,
  "dsaScore": 3,
  "dsaTotal": 3,
  "totalScore": 20,
  "totalMarks": 23,
  "status": "COMPLETED",
  "startedAt": "2026-09-04T16:00:00",
  "completedAt": "2026-09-04T16:30:00"
}
```

### Error Responses

| Status Code | Condition | Example Response |
| :--- | :--- | :--- |
| **404 Not Found** | Assessment ID does not exist in the database | `{"error": "Assessment not found with ID: 999"}` |
| **400 Bad Request** | Assessment is missing student association | `{"error": "Assessment belongs to no student"}` |
| **409 Conflict** | Assessment status is not `COMPLETED` (e.g. `IN_PROGRESS`) | `{"error": "Assessment is not in COMPLETED state (current status: IN_PROGRESS)"}` |
| **500 Internal Server Error** | Unexpected server failure | `{"error": "An unexpected error occurred while retrieving the assessment result"}` |

