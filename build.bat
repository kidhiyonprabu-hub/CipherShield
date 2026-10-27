@echo off
REM ============================================================
REM  CipherShield - Build Script for Windows
REM  Compiles all Java source files into the 'out' directory.
REM ============================================================

echo.
echo  ================================================
echo    CipherShield - Build Script
echo  ================================================
echo.

REM Check for Java compiler
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] javac not found. Please install JDK 8 or later.
    echo         Download from: https://adoptium.net/
    exit /b 1
)

REM Display Java version
echo [INFO] Java Compiler:
javac -version 2>&1
echo.

REM Create output directory
if not exist "out" mkdir out

REM Collect all Java source files
echo [INFO] Collecting source files...
dir /s /b src\*.java > sources.txt 2>nul

REM Count source files
set /a count=0
for /f %%A in (sources.txt) do set /a count+=1
echo [INFO] Found %count% source files.
echo.

REM Compile
echo [INFO] Compiling...
javac -d out -encoding UTF-8 -source 1.8 -target 1.8 @sources.txt 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. See errors above.
    del sources.txt 2>nul
    exit /b 1
)

REM Clean up
del sources.txt 2>nul

echo.
echo [SUCCESS] Build completed successfully!
echo [INFO] Output directory: out\
echo.
echo To run: run.bat
echo To test: test.bat
echo.
