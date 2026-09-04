# ==============================================================================
# RemotePrep — Windows Desktop Application Shutdown Helper
# ==============================================================================

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "         RemotePrep Application Shutdown               " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Resolve-Path "$ScriptDir\..").Path
$PidFile = Join-Path $ProjectRoot "remoteprep.pid"

$stopped = $false

# 1. Stop any process listening on port 8080 if it is Java
try {
    $conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $p = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        if ($p -and $p.ProcessName -ieq "java") {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
            Write-Host "[INFO] Stopped RemotePrep process on port 8080 (PID: $($p.Id))."
            $stopped = $true
        }
    }
} catch {}

# 2. Check recorded PID file
if (Test-Path $PidFile) {
    $rawPid = (Get-Content -Path $PidFile -ErrorAction SilentlyContinue | Out-String).Trim()
    if ($rawPid -match '^\d+$') {
        $targetPid = [int]$rawPid
        $p = Get-Process -Id $targetPid -ErrorAction SilentlyContinue
        if ($p -and $p.ProcessName -ieq "java") {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
            Write-Host "[INFO] Stopped RemotePrep backend process (PID: $($p.Id))."
            $stopped = $true
        }
    }
    Remove-Item -Path $PidFile -Force -ErrorAction SilentlyContinue
}

if ($stopped) {
    Write-Host "=======================================================" -ForegroundColor Green
    Write-Host "[SUCCESS] RemotePrep application has been safely stopped." -ForegroundColor Green
    Write-Host "=======================================================" -ForegroundColor Green
} else {
    Write-Host "[INFO] RemotePrep is not currently running. No action needed."
}

exit 0
