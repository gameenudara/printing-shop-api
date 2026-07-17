# Dumps the MySQL database running in the "mysql-server" Docker container
# to a timestamped, zipped .sql file, and prunes backups older than -RetentionDays.
param(
    [string]$BackupDir = (Join-Path $PSScriptRoot "..\..\backups"),
    [string]$DbContainer = "mysql-server",
    [string]$DbName = "printing_shop_db",
    [string]$DbUser = "root",
    [int]$RetentionDays = 30
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

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
$logFile = Join-Path $BackupDir "backup.log"

$timestamp = Get-Date -Format "yyyy-MM-dd_HHmm"
$sqlFile = Join-Path $BackupDir "$DbName`_$timestamp.sql"

$dumpArgs = @(
    "exec", $DbContainer, "mysqldump",
    "-u$DbUser", "-p$DbPassword",
    "--single-transaction", "--routines", "--triggers", $DbName
)
& docker @dumpArgs > $sqlFile

if ($LASTEXITCODE -ne 0 -or -not (Test-Path $sqlFile) -or (Get-Item $sqlFile).Length -eq 0) {
    "$(Get-Date -Format s) BACKUP FAILED" | Add-Content $logFile
    Remove-Item $sqlFile -ErrorAction SilentlyContinue
    Write-Error "Backup failed - see $logFile"
    exit 1
}

$zipFile = "$sqlFile.zip"
Compress-Archive -Path $sqlFile -DestinationPath $zipFile -Force
Remove-Item $sqlFile

"$(Get-Date -Format s) Backup succeeded: $zipFile" | Add-Content $logFile
Write-Output "Backup succeeded: $zipFile"

Get-ChildItem -Path $BackupDir -Filter "$DbName`_*.sql.zip" |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays) } |
    ForEach-Object {
        "$(Get-Date -Format s) Removing old backup: $($_.Name)" | Add-Content $logFile
        Remove-Item $_.FullName -Force
    }
