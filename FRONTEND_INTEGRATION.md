# RemotePrep — Frontend ↔ Backend Integration (Phase 15)

## Overview

Phase 15 integrates the vanilla HTML5/CSS3/JavaScript frontend with the Spring Boot REST backend (`http://localhost:8080`) for the RemotePrep assessment platform. All previous local mock generation, mock scoring, and mock evaluation runners have been replaced with authoritative backend API services.

The application adheres strictly to the offline computer laboratory constraints:
- Zero external CDN or internet dependencies.
- Standard vanilla JavaScript modules communicating via `fetch()`.
- Resilient network error handling and student input preservation.
- Authoritative server-side question generation, answer evaluation, code execution, scoring, and assessment lifecycle management.
- Preserved existing Orange/Yellow theme and UI structure across all 6 pages.

---

## Architecture & Communication Flow

```
[Page 1: Student Details] ──POST /api/students/start───────────► (Assessment & Student created)
           │
           ▼
[Page 2: Topic Selection] ──Topic codes mapped to DB IDs─────► (Aptitude & DSA topics selected)
           │
           ▼
[Page 3: Instructions]   ──Candidate proceeds to exam───────►
           │
           ▼
[Page 4: Aptitude Exam]   ──POST /api/aptitude/exam───────────► (20 randomized questions generated)
                          ──POST /api/aptitude/submit─────────► (Server-side score calculation)
           │
           ▼
[Page 5: DSA Coding Exam] ──POST /api/dsa/exam────────────────► (1 Easy + 1 Medium assigned)
                          ──POST /api/dsa/run─────────────────► (Demo test case execution)
                          ──POST /api/dsa/submit──────────────► (Final hidden judge evaluation)
           │
           ▼
[Complete & Final Result] ──POST /api/assessment/{id}/complete► (Assessment finalized)
                          ──GET /api/assessment/{id}/result───► (Authoritative scores retrieved)
           │
           ▼
[Page 6: Final Results]   ──Renders verified final scores─────►
```

---

## API Endpoints Integrated

| # | Endpoint | Method | Purpose | Key Request Fields | Key Response Fields |
|---|---|---|---|---|---|
| 1 | `/api/students/start` | POST | Register student & start assessment | `name`, `email`, `rollNumber` | `assessmentId`, `studentId`, `status`, `startedAt` |
| 2 | `/api/aptitude/exam` | POST | Generate 20 randomized aptitude MCQs | `assessmentId`, `topicIds` (1–3) | `assessmentId`, `questions` (20 items: `id`, `questionText`, `options`, `difficulty`) |
| 3 | `/api/aptitude/submit` | POST | Submit MCQ answers for server evaluation | `assessmentId`, `answers` (`questionId`, `selectedOption`) | `assessmentId`, `score`, `totalQuestions`, `percentage` |
| 4 | `/api/dsa/exam` | POST | Assign 1 Easy + 1 Medium DSA problem | `assessmentId`, `topicIds` | `assessmentId`, `questions` (2 items: `id`, `title`, `description`, `difficulty`, `examples`, `constraints`, `starterCode`) |
| 5 | `/api/dsa/run` | POST | Run code against 2 visible demo test cases | `assessmentId`, `questionId`, `language`, `sourceCode` | `assessmentId`, `questionId`, `language`, `testCases` (`testCaseNumber`, `input`, `expectedOutput`, `actualOutput`, `status`, `error`, `executionTimeMs`) |
| 6 | `/api/dsa/submit` | POST | Judge solution against all visible & hidden tests | `assessmentId`, `questionId`, `language`, `sourceCode` | `submissionId`, `assessmentId`, `questionId`, `language`, `status`, `totalTestCases`, `passedTestCases`, `failedTestCases`, `executionTimeMs` |
| 7 | `/api/assessment/{id}/complete` | POST | Finalize assessment and trigger final score calculation | Path variable: `id` | `assessmentId`, `studentId`, `studentName`, `rollNumber`, `aptitudeScore`, `aptitudeTotal`, `dsaScore`, `dsaTotal`, `totalScore`, `totalMarks`, `status`, `completedAt` |
| 8 | `/api/assessment/{id}/result` | GET | Retrieve finalized authoritative score report | Path variable: `id` | `assessmentId`, `studentId`, `studentName`, `rollNumber`, `aptitudeScore`, `aptitudeTotal`, `dsaScore`, `dsaTotal`, `totalScore`, `totalMarks`, `status`, `startedAt`, `completedAt` |

---

## State Transitions & Error Resilience

1. **Page 1: Registration (`app.js`)**
   - Submits `ApiClient.startAssessment({ name, email, rollNumber })`.
   - On error: Displays a clear error banner; input values remain intact.
   - On success: Records `assessmentId` and `studentId` in `AppState` and transitions to Page 2 (`Topic Selection`).

2. **Page 2: Topic Selection (`topicSelection.js`, `topicData.js`)**
   - Preserves candidate topic selection UI.
   - `TopicData.mapAptitudeTopicIds` and `mapDsaTopicIds` translate frontend topic codes (`apt_pct`, `dsa_arrays`, etc.) to backend database IDs (1–32 and 1–10).
   - Aptitude topic IDs are capped at 3 per backend constraint.

3. **Page 3: Instructions (`instructions.js`)**
   - Displays rules and timing constraints.
   - "Start Examination" calls `Aptitude.startExam()`.

4. **Page 4: Aptitude Examination (`aptitude.js`)**
   - Fetches 20 randomized questions via `ApiClient.generateAptitudeExam()`.
   - 30-minute exam timer with auto-submit on expiration.
   - On submission: Calls `ApiClient.submitAptitudeAnswers()`, records server score, and transitions to the DSA assessment stage.

5. **Page 5: DSA Coding Examination (`dsa.js`)**
   - Fetches 2 assigned problems (1 Easy + 1 Medium) via `ApiClient.generateDsaExam()`.
   - Supports Java, C++, C, and Python with multi-language starter code restored per problem.
   - **Reset**: Restores official starter code for active language.
   - **Run**: Calls `ApiClient.runDsaCode()`. Displays input, expected, actual output, and execution time for the 2 demo cases.
   - **Submit**: Calls `ApiClient.submitDsaCode()`. Evaluates all visible and hidden test cases, returning authoritative verdict (`ACCEPTED`, `WRONG_ANSWER`, `COMPILATION_ERROR`, etc.).
   - Timers: 25 minutes for Easy, 30 minutes for Medium.

6. **Page 6: Final Results (`dsa.js`, `page-result`)**
   - Calls `ApiClient.completeAssessment()` and `ApiClient.getAssessmentResult()`.
   - Displays candidate details, status (`COMPLETED`), authoritative Aptitude score (`/20`), DSA score (`/20`), and Total Overall Score (`/40`).

---

## Testing Instructions

### 1. Start the Backend Service
Ensure MySQL is running on port 3306 with the `remoteprep` database:
```powershell
cd "D:\projects cse\backend"
$env:DB_PASSWORD="sanjay@123"
mvn spring-boot:run
```

### 2. Launch the Frontend
Serve the `frontend/` directory using any local static file server:
```powershell
cd "D:\projects cse\frontend"
npx serve -l 3000 .
# Or: python -m http.server 3000
```
Open `http://localhost:3000` in a browser.

### 3. Verification Walkthrough
1. **Page 1**: Enter Name, Email, Roll Number &rarr; Click "Proceed to Topic Selection". Verify network tab creates `assessmentId`.
2. **Page 2**: Choose Topics &rarr; Click "Continue to Instructions".
3. **Page 3**: Click "Start Examination". Verify 20 questions load from `POST /api/aptitude/exam`.
4. **Page 4**: Answer questions &rarr; Click "Finish Aptitude". Verify `POST /api/aptitude/submit` returns server-calculated score.
5. **Page 5**:
   - Click "Start DSA Assessment". Verify 2 questions load from `POST /api/dsa/exam`.
   - Select Language &rarr; Click "Run Sample Tests". Verify `POST /api/dsa/run` returns demo case results.
   - Click "Submit Solution". Verify `POST /api/dsa/submit` evaluates all test cases.
   - Click "Finish DSA". Verify `POST /api/assessment/{id}/complete` and `GET /api/assessment/{id}/result` are called.
6. **Page 6**: Inspect final score card. Ensure authoritative marks match server results.
