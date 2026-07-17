# Restores a .sql.zip backup produced by backup-db.ps1 into the running
# "mysql-server" Docker container. This OVERWRITES the target database.
param(
    [Parameter(Mandatory = $true)][string]$BackupZip,
    [string]$DbContainer = "mysql-server",
    [string]$DbName = "printing_shop_db",
    [string]$DbUser = "root"
)

$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "backup.env.ps1"
if (Test-Path $envFile) { . $envFile }

if (-not $DbPassword) {
    if ($env:DB_PASSWORD) {
        $DbPassword = $env:DB_PASSWORD
    } else {
        Write-Error "DB password not set. Copy scripts\backup\backup.env.ps1.example to backup.env.ps1 and fill in `$DbPassword."
        exit 1
    }
}

if (-not (Test-Path $BackupZip)) {
    Write-Error "Backup file not found: $BackupZip"
    exit 1
}

Write-Warning "This will OVERWRITE database '$DbName' in container '$DbContainer' with the contents of $BackupZip"
$confirm = Read-Host "Type YES to continue"
if ($confirm -ne "YES") {
    Write-Output "Aborted."
    exit 1
}

$tempDir = Join-Path $env:TEMP "db_restore_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir | Out-Null
try {
    Expand-Archive -Path $BackupZip -DestinationPath $tempDir
    $sqlFile = Get-ChildItem $tempDir -Filter "*.sql" | Select-Object -First 1
    if (-not $sqlFile) {
        Write-Error "No .sql file found inside $BackupZip"
        exit 1
    }

    $restoreArgs = @("exec", "-i", $DbContainer, "mysql", "-u$DbUser", "-p$DbPassword", $DbName)
    Get-Content $sqlFile.FullName -Raw | & docker @restoreArgs

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Restore failed"
        exit 1
    }
    Write-Output "Restore complete from $BackupZip"
} finally {
    Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
}
