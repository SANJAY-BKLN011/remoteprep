# ==============================================================================
# RemotePrep — Windows Desktop Application Startup Helper
# ==============================================================================

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "         RemotePrep Offline Assessment Platform        " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "[INFO] Initializing RemotePrep desktop runtime..."

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Resolve-Path "$ScriptDir\..").Path
$BackendDir = Join-Path $ProjectRoot "backend"
$JarPath = Join-Path $BackendDir "target\backend-0.0.1-SNAPSHOT.jar"
$PidFile = Join-Path $ProjectRoot "remoteprep.pid"

# Configuration & Data Directories
$DataDir = if ($env:REMOTEPREP_DATA_DIR) { $env:REMOTEPREP_DATA_DIR } else { "$env:LOCALAPPDATA\RemotePrep" }
$LogDir = Join-Path $DataDir "logs"
if (-not (Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
}

$LogFile = Join-Path $LogDir "remoteprep.log"

# Database Configuration
$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DbName = if ($env:DB_NAME) { $env:DB_NAME } else { "remoteprep" }
$DbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "root" }
$DbPass = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "" }

# 1. Pre-flight Check: Verify Java 17+
Write-Host "[INFO] Checking Java runtime environment..."
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "[ERROR] Java runtime not found!" -ForegroundColor Red
    Write-Host "[ERROR] RemotePrep requires Java 17 or higher to run." -ForegroundColor Red
    Write-Host "[ERROR] Please install Java 17+ or ensure 'java' is added to your PATH." -ForegroundColor Red
    exit 1
}

# 2. Pre-flight Check: Verify MySQL Service
Write-Host "[INFO] Verifying MySQL database service on ${DbHost}:${DbPort}..."
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $tcpClient.Connect($DbHost, [int]$DbPort)
    $tcpClient.Close()
    Write-Host "[INFO] MySQL database service is running and accessible."
} catch {
    Write-Host "[ERROR] Unable to connect to MySQL on ${DbHost}:${DbPort}!" -ForegroundColor Red
    Write-Host "[ERROR] Please ensure your local MySQL server is running before launching RemotePrep." -ForegroundColor Red
    Write-Host "[ERROR] If using Windows Services: Start 'MySQL80' or run 'net start MySQL'." -ForegroundColor Red
    exit 1
}

# 3. Check if RemotePrep is already running
$existingResp = & curl.exe -s http://localhost:8080/api/test 2>$null
if ($existingResp -and $existingResp -match "RemotePrep") {
    Write-Host "[INFO] RemotePrep is already running and active."
    Write-Host "[INFO] Opening application in default web browser..."
    Start-Process "http://localhost:8080/"
    exit 0
}

# 4. Check application JAR
if (-not (Test-Path $JarPath)) {
    Write-Host "[INFO] Packaged JAR not found. Compiling and packaging backend..."
    Push-Location $BackendDir
    try {
        & mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERROR] Failed to build the application package!" -ForegroundColor Red
            Pop-Location
            exit 1
        }
    } finally {
        Pop-Location
    }
}

# 5. Launch Spring Boot Backend
Write-Host "[INFO] Starting RemotePrep backend (Production Profile)..."
Write-Host "[INFO] Logs will be written to: $LogFile"

$jvmArgs = @(
    "-Dspring.profiles.active=prod",
    "-DLOG_DIR=$LogDir",
    "-DDB_HOST=$DbHost",
    "-DDB_PORT=$DbPort",
    "-DDB_NAME=$DbName",
    "-DDB_USERNAME=$DbUser"
)

if ($DbPass) {
    $jvmArgs += "-DDB_PASSWORD=$DbPass"
}

$jvmArgs += @("-jar", "target\backend-0.0.1-SNAPSHOT.jar")

$proc = Start-Process -FilePath "java" -WorkingDirectory $BackendDir -ArgumentList $jvmArgs -PassThru -WindowStyle Hidden
$proc.Id | Out-File -FilePath $PidFile -Encoding ascii
Write-Host "[INFO] Backend process started (PID: $($proc.Id))."

# 6. Health Check Polling (Up to 60 seconds)
Write-Host "[INFO] Waiting for server health check (http://localhost:8080/api/test)..."

$isHealthy = $false
$maxAttempts = 60
for ($i = 1; $i -le $maxAttempts; $i++) {
    Start-Sleep -Seconds 1
    try {
        $raw = & curl.exe -s http://localhost:8080/api/test 2>$null
        if ($raw -and $raw -match "RemotePrep") {
            $isHealthy = $true
            break
        }
    } catch {
        # Keep waiting
    }
}

if ($isHealthy) {
    # Capture listening PID
    try {
        $conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
        if ($conn) {
            $conn.OwningProcess | Out-File -FilePath $PidFile -Encoding ascii
            Write-Host "[INFO] Confirmed active server PID: $($conn.OwningProcess)"
        }
    } catch {}

    Write-Host "=======================================================" -ForegroundColor Green
    Write-Host "[SUCCESS] RemotePrep is online and healthy!" -ForegroundColor Green
    Write-Host "[INFO] Serving frontend and REST APIs at: http://localhost:8080/" -ForegroundColor Green
    Write-Host "=======================================================" -ForegroundColor Green
    Start-Process "http://localhost:8080/"
    exit 0
} else {
    Write-Host "[ERROR] RemotePrep server did not become healthy within 60 seconds!" -ForegroundColor Red
    Write-Host "[ERROR] Please inspect the backend logs for diagnostics:" -ForegroundColor Red
    Write-Host "       $LogFile" -ForegroundColor Red
    exit 1
}
