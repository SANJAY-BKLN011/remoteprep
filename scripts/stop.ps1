# ==============================================================================
# RemotePrep — Production Windows Desktop Application Shutdown Helper
# ==============================================================================

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Resolve-Path "$ScriptDir\..").Path

# Configuration & Data Directories
$DataDir = if ($env:REMOTEPREP_DATA_DIR) { $env:REMOTEPREP_DATA_DIR } else { "$env:LOCALAPPDATA\RemotePrep" }
$PidFile = Join-Path $DataDir "remoteprep.pid"
$LegacyPidFile = Join-Path $ProjectRoot "remoteprep.pid"

$Port = if ($env:PORT) { $env:PORT } elseif ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8080" }

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "         RemotePrep Application Shutdown               " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$stopped = $false

# 1. Stop process recorded in PID file
$pidFiles = @($PidFile, $LegacyPidFile)
foreach ($pf in $pidFiles) {
    if (Test-Path $pf) {
        $rawPid = (Get-Content -Path $pf -ErrorAction SilentlyContinue | Out-String).Trim()
        if ($rawPid -match '^\d+$') {
            $targetPid = [int]$rawPid
            $p = Get-Process -Id $targetPid -ErrorAction SilentlyContinue
            if ($p -and $p.ProcessName -ieq "java") {
                Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
                Write-Host "[INFO] Stopped RemotePrep backend process (PID: $($p.Id))." -ForegroundColor Yellow
                $stopped = $true
            }
        }
        Remove-Item -Path $pf -Force -ErrorAction SilentlyContinue
    }
}

# 2. Stop any process listening on the application port if it is Java
try {
    $conns = Get-NetTCPConnection -LocalPort [int]$Port -State Listen -ErrorAction SilentlyContinue
    foreach ($conn in $conns) {
        $p = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        if ($p -and $p.ProcessName -ieq "java") {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
            Write-Host "[INFO] Stopped Java process listening on port $Port (PID: $($p.Id))." -ForegroundColor Yellow
            $stopped = $true
        }
    }
} catch {}

if ($stopped) {
    Write-Host "=======================================================" -ForegroundColor Green
    Write-Host "[SUCCESS] RemotePrep application has been safely stopped." -ForegroundColor Green
    Write-Host "=======================================================" -ForegroundColor Green
} else {
    Write-Host "[INFO] RemotePrep is not currently running. No action needed."
}

exit 0
