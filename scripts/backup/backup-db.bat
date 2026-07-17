@echo off
REM Double-click this to run a manual backup.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0backup-db.ps1"
pause
