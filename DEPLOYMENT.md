# RemotePrep — Deployment & Windows Desktop Runtime Guide

This document provides complete instructions for running, operating, and packaging the **RemotePrep Offline Assessment Platform** on Windows desktop environments.

---

## 1. System Architecture Overview

RemotePrep operates across two primary deployment architectures:

```
+-----------------------------------------------------------------------------+
|                          Development Architecture                            |
|                                                                             |
|  [Browser] ---> [Static Dev Server: 3000 / 5000] (frontend/ - hot reload)   |
|         \                                                                   |
|          +-----> [Spring Boot Server: 8080] (backend/ REST API)             |
|                               |                                             |
|                               v                                             |
|                     [MySQL Database: 3306]                                  |
+-----------------------------------------------------------------------------+

+-----------------------------------------------------------------------------+
|                    Production Desktop Runtime Architecture                  |
|                                                                             |
|  [Desktop User]                                                             |
|         | (Click Desktop Shortcut or run start-remoteprep.bat)              |
|         v                                                                   |
|  [start-remoteprep.bat]                                                     |
|         |                                                                   |
|         +--> 1. Validates Java 17+ and MySQL (localhost:3306)               |
|         +--> 2. Starts Spring Boot Application (prod profile) in background |
|         +--> 3. Polls /api/test health check until ready                    |
|         +--> 4. Opens default browser to http://localhost:8080/             |
|                                                                             |
|  [Default Browser]                                                          |
|         |                                                                   |
|         v                                                                   |
|  [Spring Boot Embedded Server (Port 8080)]                                  |
|         +--> GET /          --> Serves frontend/index.html (bundled static) |
|         +--> GET /css/*     --> Serves bundled stylesheets                  |
|         +--> GET /js/*      --> Serves bundled scripts                      |
|         +--> /api/*         --> Dispatches to Spring REST Controllers       |
|                                        |                                    |
|                                        v                                    |
|                              [MySQL Database: 3306]                         |
+-----------------------------------------------------------------------------+
```

### Key Highlights of Desktop Runtime
1. **Single Port Serving**: Spring Boot serves both the static frontend assets (`/index.html`, `/css/**`, `/js/**`) and backend REST endpoints (`/api/**`) on port `8080`.
2. **Zero CORS Issues**: Because both the UI and REST endpoints share the exact same origin (`http://localhost:8080`), browser CORS preflight requests and cross-origin restrictions are completely eliminated.
3. **Zero Node.js Dependency**: The desktop user does not require Node.js, npm, `npx serve`, or Python HTTP servers.
4. **Local Isolation**: Spring Boot production profile (`application-prod.properties`) binds strictly to `localhost` (`127.0.0.1`), ensuring the server is not exposed to untrusted external network adapters.

---

## 2. Software & Runtime Requirements

To operate RemotePrep in the Windows desktop environment, the target host must have:

| Component | Minimum Requirement | Recommended | Purpose |
| :--- | :--- | :--- | :--- |
| **Operating System** | Windows 10 (64-bit) | Windows 11 (64-bit) | Host platform |
| **Java Runtime** | OpenJDK / Oracle JDK 17 | JDK 17 or JDK 21 LTS | Runs the Spring Boot application |
| **MySQL Server** | MySQL Community Server 8.0+ | MySQL 8.0.30+ | Relational data persistence & scoring |
| **Web Browser** | Google Chrome, Edge, Firefox | Google Chrome / Microsoft Edge | Candidate examination interface |
| **RAM** | 4 GB | 8 GB+ | JVM & local process execution |
| **Disk Space** | 500 MB free | 2 GB free | Logs, DB storage, and build artifacts |

---

## 3. Environment Variables & Configuration

RemotePrep supports flexible configuration via standard environment variables:

| Environment Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_HOST` | `localhost` | MySQL database host address |
| `DB_PORT` | `3306` | MySQL database connection port |
| `DB_NAME` | `remoteprep` | Database schema name |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | *(empty string)* | Database user password |
| `REMOTEPREP_DATA_DIR` | `%LOCALAPPDATA%\RemotePrep` | Base directory for application data & logs |

### Setting Environment Variables on Windows

#### In PowerShell:
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="remoteprep"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_LOCAL_DATABASE_PASSWORD"
```

#### In Windows Command Prompt (CMD):
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=remoteprep
set DB_USERNAME=root
set DB_PASSWORD=YOUR_LOCAL_DATABASE_PASSWORD
```

---

## 4. Scripted Startup & Shutdown

The repository includes pre-built Windows batch scripts located in the `scripts/` directory.

### Starting RemotePrep: `scripts/start-remoteprep.bat`

```cmd
scripts\start-remoteprep.bat
```

**Automated Workflow:**
1. **Pre-flight Checks**:
   - Verifies that `java` (Java 17+) is present in the system `PATH`.
   - Tests TCP socket connection to MySQL on `%DB_HOST%:%DB_PORT%`.
2. **Duplicate Detection**:
   - Checks if an instance of RemotePrep is already healthy on `http://localhost:8080/api/test`.
   - If running, immediately opens the browser and exits cleanly.
3. **Packaging Check**:
   - Checks for `backend/target/backend-0.0.1-SNAPSHOT.jar`. If missing, automatically builds it using `mvn package -DskipTests`.
4. **Background Process Launch**:
   - Launches `java -Dspring.profiles.active=prod -jar backend-0.0.1-SNAPSHOT.jar` in the background with hidden console window.
   - Records the process ID (PID) into `remoteprep.pid`.
5. **Health Polling & Verification**:
   - Polls `http://localhost:8080/api/test` every second (up to 60 seconds) until a healthy response is returned.
   - Captures the exact listening Java PID.
6. **Application Launch**:
   - Automatically opens `http://localhost:8080/` in the user's default browser.

### Stopping RemotePrep: `scripts/stop-remoteprep.bat`

```cmd
scripts\stop-remoteprep.bat
```

**Targeted, Safe Termination:**
- Reads the verified PID recorded in `remoteprep.pid`.
- Confirms the target process is indeed a Java runtime before terminating.
- Falls back to querying `Get-NetTCPConnection` on port `8080` to safely terminate only the process listening on port 8080.
- **Never terminates unrelated Java processes, Maven builds, IDE instances, or MySQL services**.
- Cleans up `remoteprep.pid` upon completion.

---

## 5. Manual Startup & Shutdown (For Developers & Sysadmins)

If you prefer to run RemotePrep manually from the terminal:

### Step 1: Package the Application (Bundling Frontend)
From the project root:
```powershell
cd backend
mvn clean package -DskipTests
```
This triggers the `maven-resources-plugin` to copy all static files from `../frontend/` into `target/classes/static/`, producing a self-contained executable JAR at `backend/target/backend-0.0.1-SNAPSHOT.jar`.

### Step 2: Run with Production Profile
```powershell
java -Dspring.profiles.active=prod `
     -DDB_HOST=localhost `
     -DDB_PORT=3306 `
     -DDB_NAME=remoteprep `
     -DDB_USERNAME=root `
     -DDB_PASSWORD="YOUR_LOCAL_DATABASE_PASSWORD" `
     -jar backend/target/backend-0.0.1-SNAPSHOT.jar
```

### Step 3: Access Application
Open your browser and navigate to:
```
http://localhost:8080/
```

### Step 4: Terminate
Press `Ctrl + C` in the console window where the JAR is running.

---

## 6. Health Checks & Diagnostics

RemotePrep provides two health check endpoints for uptime monitoring and automated tooling:

### 1. Primary Health Check: `GET /api/test`
- **URL**: `http://localhost:8080/api/test`
- **Method**: `GET`
- **Response Format**: `application/json`
- **Sample Response**:
  ```json
  {
    "status": "UP",
    "message": "RemotePrep backend is working"
  }
  ```

### 2. Standby Health Check: `GET /api/health`
- **URL**: `http://localhost:8080/api/health`
- **Method**: `GET`
- **Response Format**: `application/json`
- **Sample Response**:
  ```json
  {
    "status": "UP",
    "message": "RemotePrep backend is working"
  }
  ```

---

## 7. Logging & Diagnostics

In production mode (`spring.profiles.active=prod`), RemotePrep uses structured file-based logging to prevent console pollution:

- **Log File Path**: `%LOCALAPPDATA%\RemotePrep\logs\remoteprep.log`
  *(e.g., `C:\Users\<Username>\AppData\Local\RemotePrep\logs\remoteprep.log`)*
- **Log Rolling Policy**: Rotates daily or when file size reaches 10MB.
- **Log Retention**: Preserves up to 30 days of historical logs, capped at 100MB total archive size.
- **Log Levels**:
  - `com.remoteprep`: `INFO`
  - `org.springframework`: `WARN`
  - `org.hibernate`: `ERROR`

To view live application logs in PowerShell:
```powershell
Get-Content -Path "$env:LOCALAPPDATA\RemotePrep\logs\remoteprep.log" -Wait -Tail 50
```

---

## 8. Offline Lab Operation

RemotePrep is architected for completely isolated, air-gapped lab environments:

1. **No External CDN Dependencies**:
   - All CSS stylesheets and JavaScript libraries are served locally from `frontend/` (bundled in the JAR).
   - Fonts and styling fall back gracefully to native system fonts (`Segoe UI`, `system-ui`, `sans-serif`).
2. **Local Code Execution Engine**:
   - DSA code evaluation (C, C++, Java, Python) runs entirely on the local host machine using installed compilers/interpreters via temporary sandbox directories.
   - Does not contact any third-party judge API or cloud execution cluster.
3. **Local Database Storage**:
   - All student profiles, exam sessions, question banks, aptitude results, and DSA submissions reside inside the local MySQL instance.
   - An active internet connection is never required for taking or evaluating assessments.

---

## 9. Blueprint for Future Windows MSI / EXE Packaging (Phase 17+)

When moving from runtime preparation to single-click Windows installation, the application packaging pipeline will follow this design:

```
+-----------------------------------------------------------------------------+
|                           Future Packaging Flow                             |
|                                                                             |
|  [Source: Backend JAR + Static Frontend]                                    |
|                            |                                                |
|                            v                                                |
|  [jpackage (JDK 17+ Tooling)]                                               |
|    - Bundles minimal Java Runtime (via jlink)                               |
|    - Bundles backend-0.0.1-SNAPSHOT.jar                                     |
|    - Generates native Windows launcher: RemotePrep.exe                      |
|                            |                                                |
|                            v                                                |
|  [WiX Toolset v3 / v4]                                                      |
|    - Packages RemotePrep.exe + JRE runtime + pre-configured MySQL service   |
|    - Configures Windows Desktop Shortcut & Start Menu entry                 |
|    - Compiles into single installer: RemotePrep-Setup.msi                   |
|                            |                                                |
|                            v                                                |
|  [End User Experience]                                                      |
|    1. Run RemotePrep-Setup.msi once                                         |
|    2. Double-click "RemotePrep" Desktop Shortcut                            |
|    3. Assessment platform opens automatically in browser                    |
+-----------------------------------------------------------------------------+
```

### Pre-requisites for Phase 17 Packaging:
- **`jpackage`**: Built into OpenJDK 17+, eliminates the requirement for the end user to manually install Java.
- **Embedded MySQL / SQLite option or bundled silent MySQL MSI**: Provides zero-config database provisioning for unattended lab machines.
- **WiX Toolset**: Standard Windows installer generator that creates clean MSI packages with Add/Remove Programs integration.
