# RemotePrep DSA Execution & Submission APIs (Phase 11 & Phase 12)

## Overview & Architecture Distinction

RemotePrep provides two distinct endpoints for candidate code interaction during DSA assessments:

| Feature | `POST /api/dsa/run` (Phase 11) | `POST /api/dsa/submit` (Phase 12) |
| :--- | :--- | :--- |
| **Purpose** | Interactive testing during coding | Official grading against full test suite |
| **Test Cases Evaluated** | Visible / Demo test cases only (exactly 2) | All final test cases (Visible + Hidden) |
| **Hidden Tests Access** | Strictly prohibited from running | Evaluated internally; details strictly hidden |
| **Database Persistence** | None (no rows created or updated) | Creates a new row in `dsa_submissions` |
| **Final Verdicts** | None (per-test-case execution outcome) | Official verdict (`ACCEPTED`, `WRONG_ANSWER`, etc.) |
| **Assessment Scores** | Untouched | Recorded in submission record; exam finalized separately |
| **Multiple Invocations** | Temporary ephemeral process runs | Every submit creates a separate historical record |

---

## 1. POST /api/dsa/run (Run API)

### Purpose
Allows candidates to test their solution interactively against two visible demo test cases before submitting.

### Request
```json
POST /api/dsa/run
Content-Type: application/json

{
  "assessmentId": 1,
  "questionId": 15,
  "language": "JAVA",
  "sourceCode": "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        System.out.println(n * 2);\n    }\n}"
}
```

### Response
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "assessmentId": 1,
  "questionId": 15,
  "language": "JAVA",
  "testCases": [
    {
      "testCaseNumber": 1,
      "input": "2",
      "expectedOutput": "4",
      "actualOutput": "4\n",
      "status": "SUCCESS",
      "error": null,
      "executionTimeMs": 132
    },
    {
      "testCaseNumber": 2,
      "input": "5",
      "expectedOutput": "10",
      "actualOutput": "10\n",
      "status": "SUCCESS",
      "error": null,
      "executionTimeMs": 128
    }
  ]
}
```

---

## 2. POST /api/dsa/submit (Final Submission & Evaluation API)

### Purpose
Judges candidate code against the complete suite of final test cases (including hidden verification cases), produces an official verdict, and persists the submission to the database.

### Submission Lifecycle
```
Client Request
      ↓
Validate Request & Assessment Assignment
      ↓
Validate & Extract Complete Test Case Suite (Visible + Hidden)
      ↓
Create New Submission Record (result_status = 'PENDING')
      ↓
Commit Initial Record to Database
      ↓
Execute Candidate Code Across All Test Cases (Outside DB Transaction)
      ↓
Judge Outputs with Normalized Comparison
      ↓
Determine Final Verdict (Early Termination on Failure)
      ↓
Update Same Submission Record with Verdict
      ↓
Return Safe Response
```

### Request
```json
POST /api/dsa/submit
Content-Type: application/json

{
  "assessmentId": 1,
  "questionId": 15,
  "language": "JAVA",
  "sourceCode": "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        System.out.println(n * 2);\n    }\n}"
}
```

* `assessmentId` *(Required)*: Must be an existing assessment in `IN_PROGRESS` status associated with a valid student.
* `questionId` *(Required)*: Must be assigned to this assessment in `dsa_exam_questions`.
* `language` *(Required)*: Supported languages: `JAVA`, `CPP`, `C`, `PYTHON` (case-insensitive, normalized to uppercase).
* `sourceCode` *(Required)*: Candidate source code (cannot be null or blank).

### Response
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "submissionId": 42,
  "assessmentId": 1,
  "questionId": 15,
  "language": "JAVA",
  "status": "ACCEPTED",
  "submittedAt": "2026-09-04T15:40:00",
  "totalTestCases": 5,
  "passedTestCases": 5,
  "failedTestCases": 0,
  "executionTimeMs": 624
}
```

### Final Verdict Classifications
1. `ACCEPTED`: Candidate program executed successfully on all test cases and all outputs matched the expected output.
2. `WRONG_ANSWER`: Candidate program exited with code 0, but its stdout differed from the expected output.
3. `COMPILATION_ERROR`: The compiler reported syntax, type, or linking errors.
4. `RUNTIME_ERROR`: Process crashed or exited with a non-zero exit code (e.g., uncaught exception, segmentation fault).
5. `TIME_LIMIT_EXCEEDED`: Process execution exceeded the configured timeout (default: 5000ms).
6. `OUTPUT_LIMIT_EXCEEDED`: Process stdout/stderr exceeded the configured output byte limit (default: 1 MB).
7. `EXECUTION_ERROR`: System error occurred while preparing or launching execution.

---

## 3. Test Case Security & Hidden Test Protection

- **Server-Owned Data**: All test cases (both visible demo cases and hidden judge cases) are stored server-side. The client never supplies test inputs or expected outputs.
- **Zero Hidden Leakage**: Hidden test inputs, hidden expected outputs, and execution details for hidden test cases are **never** returned in the API response or leaked through exception messages.
- **Early Termination**: Evaluation halts immediately upon encountering the first failure (e.g. `COMPILATION_ERROR`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `WRONG_ANSWER`), preserving evaluation resources while keeping hidden failure indexes confidential.

---

## 4. Output Comparison & Whitespace Normalization

Comparison is performed by the dedicated `DsaOutputComparator` component according to strict normalization rules:
1. **Line Ending Standardization**: CRLF (`\r\n`) and CR (`\r`) are converted to LF (`\n`).
2. **Line-Level Trimming**: Trailing whitespace on every line is stripped.
3. **Overall Trimming**: Leading and trailing overall whitespace is stripped.
4. **Exact Equality**: After normalization, strings must match exactly. Substring matching is strictly avoided (e.g. `"5"` and `"6"` never match; `"5"` and `"5\n"` match).

---

## 5. Multiple Submissions & Immutability

- Every call to `POST /api/dsa/submit` generates a **new** record in `dsa_submissions`.
- Previous submission records are **never overwritten**. If a candidate first receives `WRONG_ANSWER` and later submits code receiving `ACCEPTED`, both records are preserved with their respective timestamps and verdicts.
- The `dsa_exam_questions` table remains immutable throughout code submissions.

---

## 6. Security Sandbox Disclaimer

The RemotePrep local execution engine is designed for offline college computer labs. While timeouts, process resource isolation, and output volume limits are enforced, it is not a complete kernel-level security sandbox.
