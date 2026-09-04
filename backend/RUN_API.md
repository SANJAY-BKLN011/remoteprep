# RemotePrep DSA Run API Documentation (Phase 11)

## Overview
The `POST /api/dsa/run` endpoint allows candidates to execute their code interactively against visible demo test cases during the DSA portion of an assessment.

> **IMPORTANT DISTINCTION**:
> **Run is a temporary execution operation and does not represent a final submission.**
> - `Run` executes code against only visible **demo test cases**.
> - `Run` does **NOT** insert or update rows in `dsa_submissions`.
> - `Run` does **NOT** alter candidate or assessment scores.
> - `Run` does **NOT** mark questions as solved or completed.
> - `Run` does **NOT** produce final judging verdicts (`ACCEPTED` / `WRONG_ANSWER`).

---

## 1. API Endpoint

$$\text{\bf Endpoint: } \texttt{POST /api/dsa/run}$$
* **Status**: `200 OK` on success, `400 Bad Request`, `404 Not Found`, `409 Conflict`.
* **Content-Type**: `application/json`

---

## 2. Request Format

```json
{
  "assessmentId": 1,
  "questionId": 15,
  "language": "JAVA",
  "sourceCode": "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (sc.hasNextInt()) {\n            int n = sc.nextInt();\n            System.out.println(climbStairs(n));\n        }\n    }\n    public static int climbStairs(int n) {\n        if (n <= 2) return n;\n        int a = 1, b = 2;\n        for (int i = 3; i <= n; i++) {\n            int c = a + b;\n            a = b;\n            b = c;\n        }\n        return b;\n    }\n}"
}
```

* `assessmentId` *(Required)*: ID of candidate's assessment. Must be in `IN_PROGRESS` status and belong to a valid student.
* `questionId` *(Required)*: ID of the problem. Must be assigned to this assessment via `dsa_exam_questions`.
* `language` *(Required)*: Supported values: `JAVA`, `CPP`, `C`, `PYTHON` (case-insensitive).
* `sourceCode` *(Required)*: Source code to execute. Preserved verbatim.

---

## 3. Response Format

```json
{
  "assessmentId": 1,
  "questionId": 15,
  "language": "JAVA",
  "testCases": [
    {
      "testCaseNumber": 1,
      "input": "2",
      "expectedOutput": "2",
      "actualOutput": "2\n",
      "status": "SUCCESS",
      "error": null,
      "executionTimeMs": 145
    },
    {
      "testCaseNumber": 2,
      "input": "3",
      "expectedOutput": "3",
      "actualOutput": "3\n",
      "status": "SUCCESS",
      "error": null,
      "executionTimeMs": 138
    }
  ]
}
```

---

## 4. Demo Test Cases & Retrieval
* Exactly **TWO** demo test cases are retrieved server-side from the problem definition (`dsa_questions.test_cases` or `examples`).
* The client is never required to submit `stdin` or test cases.
* **Security**: Internal and hidden test cases used for final submissions are strictly withheld from the response.

---

## 5. Execution Lifecycle & Statuses
1. **Independent Execution**: Each demo test case runs in a completely separate, clean OS process with its own workspace and stdin stream.
2. **Early Exit on Compilation Failure**: If compilation fails on test case 1, test case 2 is skipped and a clear `COMPILATION_ERROR` is returned.
3. **Execution Statuses**:
   * `SUCCESS`: Process completed with exit code 0.
   * `COMPILATION_ERROR`: Compiler reported syntax or build errors.
   * `RUNTIME_ERROR`: Process terminated with a non-zero exit code or uncaught exception.
   * `TIME_LIMIT_EXCEEDED`: Process exceeded configured timeout limit.
   * `OUTPUT_LIMIT_EXCEEDED`: Process exceeded configured maximum output volume (1 MB).
   * `EXECUTION_ERROR`: System error occurred.
