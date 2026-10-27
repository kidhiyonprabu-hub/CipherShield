@echo off
REM ============================================================
REM  CipherShield - Test Script for Windows
REM  Runs all automated tests.
REM ============================================================

echo.
echo  ================================================
echo    CipherShield - Running Automated Tests
echo  ================================================
echo.

REM Check if compiled
if not exist "out\com\ciphershield\CipherShieldApp.class" (
    echo [INFO] Application not compiled. Running build first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed. Cannot run tests.
        exit /b 1
    )
)

echo [INFO] Running CipherShield Test Suite...
echo.

java -cp out com.ciphershield.test.TestRunner

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [WARNING] Some tests may have failed. See output above.
    exit /b %ERRORLEVEL%
)

echo.
echo [INFO] Test execution complete.
echo.
