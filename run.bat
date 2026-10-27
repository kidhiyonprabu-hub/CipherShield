@echo off
REM ============================================================
REM  CipherShield - Run Script for Windows
REM  Launches the CipherShield desktop application.
REM ============================================================

echo.
echo  ================================================
echo    CipherShield - Launching Application
echo  ================================================
echo.

REM Check if compiled
if not exist "out\com\ciphershield\CipherShieldApp.class" (
    echo [INFO] Application not compiled. Running build first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed. Cannot start application.
        exit /b 1
    )
)

REM Check for Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] java not found. Please install JDK 8 or later.
    exit /b 1
)

echo [INFO] Starting CipherShield...
echo.

java -cp out com.ciphershield.CipherShieldApp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application exited with error code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
)
