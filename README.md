# RemotePrep — Offline Assessment Platform

RemotePrep is a robust, offline-capable college examination platform designed for low-specification computer laboratories. It provides automated Aptitude test generation and evaluation, real-time multi-language DSA code execution (Java, Python, C, C++), and server-authoritative scoring.

---

## Assessment Workflow & Scoring

1. **Student Registration**: Student enters Name and Roll Number (`POST /api/students/start`).
2. **Topic Selection**: Student chooses 1–3 aptitude topics.
3. **Aptitude Examination**: Backend generates exactly 20 randomized questions (`POST /api/aptitude/exam`). 30-minute exam timer with automatic submission upon timeout.
4. **Aptitude Evaluation**: Server scores answers server-side (`POST /api/aptitude/submit`).
5. **DSA Examination**: Backend assigns exactly 2 DSA problems: 1 Easy (+1) and 1 Medium (+2) (`POST /api/dsa/exam`).
6. **Code Execution & Submission**:
   - **Run**: Tests against 2 visible sample test cases (`POST /api/dsa/run`).
   - **Submit**: Evaluates against visible and hidden test cases (`POST /api/dsa/submit`).
7. **Assessment Completion**: Server finalizes scoring and locks assessment immutability (`POST /api/assessment/{id}/complete`).
8. **Results**: Authoritative final score retrieval (`GET /api/assessment/{id}/result`).

### Scoring System
- **Aptitude Total**: `20 marks` (1 mark per correct question)
- **DSA Total**: `3 marks` (Easy: `+1 mark`, Medium: `+2 marks`)
- **Total Overall Score**: `23 marks`

---

## Windows Deployment Architecture

The target deployment model is a single-click Windows Desktop Application (`RemotePrep.msi` / `RemotePrep.exe`) for lab computers without requiring manual developer tooling, command lines, or runtime pre-installations.

```
RemotePrep MSI
    |
    +-- Java Runtime
    |
    +-- Spring Boot Application
    |
    +-- Frontend
    |
    +-- DSA Execution Tools
    |
    +-- Local Database
    |
    +-- Launcher
```

### Component Status Breakdown

| Component | Status | Details |
| :--- | :--- | :--- |
| **Frontend** | **READY** | Packed inside Spring Boot (`backend/src/main/resources/static/`). Served from embedded Tomcat at `http://localhost:8080/`. No Node.js, Python server, or Live Server required. |
| **Spring Boot Application** | **READY** | Runs as a single fat JAR (`backend-0.0.1-SNAPSHOT.jar`). Exposes REST API, static assets, and execution engine on port 8080. |
| **CORS Policy** | **READY** | Open wildcards removed. Restricted to localhost same-origin (`http://localhost:8080`, `http://127.0.0.1:8080`) in production, with dev fallback for separate ports. |
| **Java Runtime** | **NOT READY** (Bundling) | Java 17 LTS is currently required on the host system `PATH`. Future MSI will bundle a trimmed OpenJDK JRE via `jlink`. |
| **DSA Execution Tools** | **REQUIRES DECISION** | Currently requires system `PATH` executables (`javac`/`java`, `python`, `gcc`, `g++`). MSI bundling options: package portable MinGW/Python runtimes vs. pre-flight installer requirement. |
| **Local Database** | **REQUIRES DECISION** | Currently requires a local MySQL 8 instance on port 3306 with schema pre-loaded. MSI options: bundle silent MySQL installer, portable MySQL, or evaluate embedded local database. |
| **Launcher** | **NOT READY** | Currently started via `scripts/start-remoteprep.bat` or `java -jar`. Future launcher (`RemotePrep.exe`) will verify health and open default browser automatically. |
| **MSI Package** | **NOT READY** | WiX Toolset installer compilation will be implemented in a dedicated packaging phase once runtime bundling decisions are finalized. |

---

## DSA Execution Dependency Matrix

| Language | Required Executables | Current Detection Method | Config Property | Default Value |
| :--- | :--- | :--- | :--- | :--- |
| **Java** | `javac` (compiler), `java` (runtime) | System `PATH` / `%JAVA_HOME%\bin` | `remoteprep.execution.java-compiler`, `remoteprep.execution.java-runtime` | `javac`, `java` |
| **Python** | `python` (interpreter) | System `PATH` | `remoteprep.execution.python-runtime` | `python` |
| **C** | `gcc` (compiler) | System `PATH` | `remoteprep.execution.c-compiler` | `gcc` |
| **C++** | `g++` (compiler) | System `PATH` | `remoteprep.execution.cpp-compiler` | `g++` |

### Temporary Directory & Workspace Isolation
- Execution workspaces are created under `remoteprep.execution.temp-dir`, defaulting to `${java.io.tmpdir}/remoteprep-executions`.
- Each execution runs in an isolated `exec-<UUID>` folder.
- Temporary files (source code, compiled `.class` or `.exe` binaries) are cleaned up recursively in `finally` blocks upon completion, compilation failure, runtime failure, or timeout.
- Process environments are stripped of database credentials (`DB_PASSWORD`, `MYSQL_PWD`).
- *Note*: The execution engine enforces strict execution timeouts and output limits; it is not a virtualized OS container sandbox.

---

## Database Dependency Audit

- **DBMS**: MySQL Server 8.x
- **Database Name**: `remoteprep` (configurable via `DB_NAME`, default `remoteprep`)
- **Default Port**: `3306` (configurable via `DB_PORT`, default `3306`)
- **Host**: `localhost` / `127.0.0.1` (configurable via `DB_HOST`, default `localhost`)
- **Schema Management**: `spring.jpa.hibernate.ddl-auto=validate`. Hibernate validates entities against the database and does **not** auto-create or alter tables.
- **Schema Initialization**: Must be pre-initialized via `backend/database/schema.sql` and seeded with `backend/database/seed.sql`.
- **Startup Requirement**: MySQL service must be running before Spring Boot starts.

---

## Running the Application

### Option A: Standalone Packaged JAR (Production)
```powershell
# 1. Package the application (bundles frontend into JAR)
cd backend
mvn clean package -DskipTests

# 2. Run the unified application
java -DDB_PASSWORD=YOUR_MYSQL_PASSWORD -jar target/backend-0.0.1-SNAPSHOT.jar

# 3. Access in browser
# http://localhost:8080/
```

### Option B: Using Windows Startup Script
```cmd
scripts\start-remoteprep.bat
```

### Running Backend Tests
```powershell
cd backend
mvn clean test -DDB_PASSWORD=YOUR_MYSQL_PASSWORD
```
