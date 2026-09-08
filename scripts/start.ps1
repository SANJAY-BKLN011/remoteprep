# ==============================================================================
# RemotePrep — Production Windows Desktop Application Launcher
# ==============================================================================

param (
    [switch]$Detach,
    [switch]$Background
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Resolve-Path "$ScriptDir\..").Path

# Configuration & Data Directories (Windows user-specific, non-repo path)
$DataDir = if ($env:REMOTEPREP_DATA_DIR) { $env:REMOTEPREP_DATA_DIR } else { "$env:LOCALAPPDATA\RemotePrep" }
$LogDir = Join-Path $DataDir "logs"
if (-not (Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
}

$LogFile = Join-Path $LogDir "remoteprep.log"
$StartupLog = Join-Path $LogDir "remoteprep-startup.log"
$StartupErrLog = Join-Path $LogDir "remoteprep-startup-err.log"
$PidFile = Join-Path $DataDir "remoteprep.pid"

# Port Configuration
$Port = if ($env:PORT) { $env:PORT } elseif ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8080" }

# Database Configuration
$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DbName = if ($env:DB_NAME) { $env:DB_NAME } else { "remoteprep" }
$DbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "root" }
$DbPass = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "" }

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "         RemotePrep Offline Assessment Platform        " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

# 1. Pre-flight Check: Verify Java Runtime Environment
$javaExe = "java"
$javaCmd = Get-Command $javaExe -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        $javaExe = "$env:JAVA_HOME\bin\java.exe"
    } else {
        Write-Host ""
        Write-Host "[ERROR] Java 17 or later is required to run RemotePrep." -ForegroundColor Red
        Write-Host "[ERROR] Java was not found on this computer." -ForegroundColor Red
        Write-Host "[ERROR] Please install Java 17+ or ensure 'java' is added to your PATH." -ForegroundColor Red
        exit 1
    }
}

# 2. Pre-flight Check: Verify MySQL Service Connectivity
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $tcpClient.Connect($DbHost, [int]$DbPort)
    $tcpClient.Close()
} catch {
    Write-Host ""
    Write-Host "[ERROR] Unable to connect to MySQL database on ${DbHost}:${DbPort}!" -ForegroundColor Red
    Write-Host "[ERROR] Please ensure your local MySQL server is running before launching RemotePrep." -ForegroundColor Red
    Write-Host "[ERROR] If using Windows Services: Start 'MySQL84' or 'MySQL80' via 'net start MySQL84'." -ForegroundColor Red
    exit 1
}

# 3. Pre-flight Check: Check if RemotePrep is already running
try {
    $existingResp = Invoke-WebRequest -Uri "http://127.0.0.1:$Port/api/test" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
    if ($existingResp -and $existingResp.StatusCode -eq 200 -and $existingResp.Content -match "RemotePrep") {
        Write-Host ""
        Write-Host "[INFO] RemotePrep is already running and ready on port $Port." -ForegroundColor Green
        Write-Host "[INFO] Opening application in default web browser..." -ForegroundColor Green
        Start-Process "http://127.0.0.1:$Port/"
        exit 0
    }
} catch {
    # Not running yet, proceed with startup
}

# 4. Locate Application JAR dynamically (Path-independent resolution)
$CandidateJarPaths = @(
    (Join-Path $ProjectRoot "backend\target\backend-0.0.1-SNAPSHOT.jar"),
    (Join-Path $ProjectRoot "backend\backend-0.0.1-SNAPSHOT.jar"),
    (Join-Path $ProjectRoot "app\backend-0.0.1-SNAPSHOT.jar"),
    (Join-Path $ProjectRoot "backend-0.0.1-SNAPSHOT.jar"),
    (Join-Path $ScriptDir "backend-0.0.1-SNAPSHOT.jar")
)

$JarPath = $null
foreach ($cand in $CandidateJarPaths) {
    if (Test-Path $cand) {
        $JarPath = (Resolve-Path $cand).Path
        break
    }
}

if (-not $JarPath) {
    # If in source directory and maven is available, attempt build
    $BackendPom = Join-Path $ProjectRoot "backend\pom.xml"
    if (Test-Path $BackendPom) {
        Write-Host "[INFO] Packaged JAR not found. Building application package with Maven..."
        Push-Location (Join-Path $ProjectRoot "backend")
        try {
            & mvn clean package -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Host "[ERROR] Maven build failed to produce backend-0.0.1-SNAPSHOT.jar!" -ForegroundColor Red
                exit 1
            }
            $JarPath = (Resolve-Path "target\backend-0.0.1-SNAPSHOT.jar").Path
        } finally {
            Pop-Location
        }
    } else {
        Write-Host "[ERROR] Could not locate backend-0.0.1-SNAPSHOT.jar!" -ForegroundColor Red
        Write-Host "[ERROR] Please ensure the application is properly packaged or installed." -ForegroundColor Red
        exit 1
    }
}

# 5. Launch Spring Boot Application
Write-Host "STARTING..." -ForegroundColor Cyan
Write-Host "Logs directory: $LogDir" -ForegroundColor Gray

$JarFileName = Split-Path -Leaf $JarPath
$JarDir = Split-Path -Parent $JarPath

$jvmArgs = @(
    "-Dspring.profiles.active=prod",
    "-Dserver.port=$Port",
    "-DLOG_DIR=$LogDir",
    "-DDB_HOST=$DbHost",
    "-DDB_PORT=$DbPort",
    "-DDB_NAME=$DbName",
    "-DDB_USERNAME=$DbUser"
)

if ($DbPass) {
    $jvmArgs += "-DDB_PASSWORD=$DbPass"
}

$jvmArgs += @("-jar", $JarFileName)

$proc = Start-Process -FilePath $javaExe `
                      -WorkingDirectory $JarDir `
                      -ArgumentList $jvmArgs `
                      -PassThru `
                      -WindowStyle Hidden `
                      -RedirectStandardOutput $StartupLog `
                      -RedirectStandardError $StartupErrLog

$proc.Id | Out-File -FilePath $PidFile -Encoding ascii

# 6. Readiness Check with retries (Up to 60 seconds)
Write-Host "Waiting for RemotePrep..." -ForegroundColor Cyan

$isHealthy = $false
$maxAttempts = 60

for ($i = 1; $i -le $maxAttempts; $i++) {
    Write-Host "Attempt $i/$maxAttempts..."
    Start-Sleep -Seconds 1

    # Check if process crashed early
    if ($proc.HasExited) {
        Write-Host "[ERROR] RemotePrep process terminated unexpectedly (Exit Code: $($proc.ExitCode))!" -ForegroundColor Red
        break
    }

    try {
        $resp = Invoke-WebRequest -Uri "http://127.0.0.1:$Port/api/test" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($resp -and $resp.StatusCode -eq 200 -and $resp.Content -match "RemotePrep") {
            $isHealthy = $true
            break
        }
    } catch {
        # Server still initializing
    }
}

if ($isHealthy) {
    # Confirm listening process
    try {
        $conn = Get-NetTCPConnection -LocalPort [int]$Port -State Listen -ErrorAction SilentlyContinue
        if ($conn) {
            $conn.OwningProcess | Out-File -FilePath $PidFile -Encoding ascii
        }
    } catch {}

    Write-Host ""
    Write-Host "RemotePrep is ready." -ForegroundColor Green
    Write-Host "Opening browser: http://127.0.0.1:$Port/" -ForegroundColor Green
    Start-Process "http://127.0.0.1:$Port/"

    if ($Detach -or $Background) {
        Write-Host "[INFO] RemotePrep is running in background (PID: $($proc.Id))."
        Write-Host "[INFO] To stop: scripts\stop-remoteprep.bat"
        exit 0
    }

    Write-Host "=======================================================" -ForegroundColor Cyan
    Write-Host "RemotePrep is running." -ForegroundColor Cyan
    Write-Host "Application URL: http://127.0.0.1:$Port/" -ForegroundColor Cyan
    Write-Host "Log file:        $LogFile" -ForegroundColor Cyan
    Write-Host "Backend PID:     $($proc.Id)" -ForegroundColor Cyan
    Write-Host "=======================================================" -ForegroundColor Cyan
    Write-Host "To stop RemotePrep, run: scripts\stop-remoteprep.bat" -ForegroundColor Gray
    Write-Host "Or press Ctrl+C in this window to stop the application." -ForegroundColor Gray

    # Interactive foreground wait: keeps console open and terminates Java if window is closed
    try {
        while (-not $proc.HasExited) {
            Start-Sleep -Seconds 1
        }
    } finally {
        if (-not $proc.HasExited) {
            Write-Host "`n[INFO] Stopping RemotePrep (PID: $($proc.Id))..." -ForegroundColor Yellow
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            Remove-Item -Path $PidFile -Force -ErrorAction SilentlyContinue
            Write-Host "[INFO] RemotePrep stopped." -ForegroundColor Green
        }
    }
    exit 0

} else {
    Write-Host ""
    Write-Host "RemotePrep failed to start." -ForegroundColor Red
    Write-Host "Please check the log file:" -ForegroundColor Red
    Write-Host "$LogFile" -ForegroundColor Yellow
    if (Test-Path $StartupLog) {
        Write-Host "Startup log:" -ForegroundColor Red
        Write-Host "$StartupLog" -ForegroundColor Yellow
    }
    # Clean up crashed process if still hanging
    if (-not $proc.HasExited) {
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
    }
    Remove-Item -Path $PidFile -Force -ErrorAction SilentlyContinue
    exit 1
}
