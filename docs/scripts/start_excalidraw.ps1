<#
.SYNOPSIS
    Starts a local Excalidraw instance using Docker or Podman.

.DESCRIPTION
    Pulls and runs the Excalidraw container image. Auto-detects whether Docker
    or Podman is available, or allows the user to force a specific engine.
    Cleans up any existing container with the same name before starting.

.PARAMETER Engine
    Force a specific container engine: "docker" or "podman".
    If not specified, the script auto-detects the available engine.

.PARAMETER Port
    Host port to bind (default: 5000).

.PARAMETER ContainerName
    Name for the container (default: "excalidraw").

.EXAMPLE
    .\start_excalidraw.ps1
.EXAMPLE
    .\start_excalidraw.ps1 -Port 8080
.EXAMPLE
    .\start_excalidraw.ps1 -Engine docker -Port 3000 -ContainerName my-excalidraw
#>

[CmdletBinding()]
param(
    [ValidateSet("docker", "podman")]
    [string]$Engine,

    [ValidateRange(1, 65535)]
    [int]$Port = 5000,

    [string]$ContainerName = "excalidraw"
)

$ErrorActionPreference = "Stop"

$Image         = "excalidraw/excalidraw:latest"
$ContainerPort = 80

# --- Detect container engine ---
function Find-ContainerEngine {
    if (Get-Command "podman" -ErrorAction SilentlyContinue) { return "podman" }
    if (Get-Command "docker" -ErrorAction SilentlyContinue) { return "docker" }
    return $null
}

if ($Engine) {
    if (-not (Get-Command $Engine -ErrorAction SilentlyContinue)) {
        Write-Error "Error: '$Engine' is not installed or not in PATH."
        exit 1
    }
}
else {
    $Engine = Find-ContainerEngine
    if (-not $Engine) {
        Write-Error "Error: No container engine found. Please install Docker or Podman."
        exit 1
    }
    Write-Host "Auto-detected container engine: $Engine" -ForegroundColor Cyan
}

# --- Remove existing container (if any) ---
# Temporarily allow native command stderr so "no such container" doesn't become
# a terminating error in PowerShell 5.1 with $ErrorActionPreference = "Stop".
$savedEAP = $ErrorActionPreference
$ErrorActionPreference = "Continue"
try {
    & $Engine container inspect $ContainerName 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Stopping existing '$ContainerName' container..." -ForegroundColor Yellow
        & $Engine stop $ContainerName 2>&1 | Out-Null
        & $Engine rm $ContainerName 2>&1 | Out-Null
    }
}
catch {
    # Container does not exist — nothing to clean up
}
finally {
    $ErrorActionPreference = $savedEAP
}

# --- Start container ---
Write-Host "Starting Excalidraw on http://localhost:$Port using $Engine..." -ForegroundColor Cyan
& $Engine run --rm -dit `
    --name $ContainerName `
    -p "${Port}:${ContainerPort}" `
    $Image

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to start Excalidraw container."
    exit 1
}

Write-Host ""
Write-Host "Excalidraw is running at: http://localhost:$Port" -ForegroundColor Green
Write-Host "   Container name: $ContainerName"
Write-Host "   Engine: $Engine"
Write-Host ""
Write-Host "To stop: $Engine stop $ContainerName" -ForegroundColor DarkGray
