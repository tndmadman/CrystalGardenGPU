@echo off
setlocal
title CrystalGardenGPU - Compile and Run
cd /d "%~dp0"

echo ========================================
echo   CrystalGardenGPU - Compile and Run
echo ========================================
echo.

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java was not found in PATH.
    echo Install JDK 21 and make sure java.exe is available from Command Prompt.
    echo.
    pause
    exit /b 1
)

set "GRADLE_CMD="
if exist "%~dp0gradlew.bat" (
    set "GRADLE_CMD=%~dp0gradlew.bat"
) else (
    where gradle >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Gradle was not found and gradlew.bat is not present.
        echo Install Gradle once, or generate/commit the Gradle wrapper.
        echo.
        pause
        exit /b 1
    )
    set "GRADLE_CMD=gradle"
)

echo [1/2] Compiling...
call "%GRADLE_CMD%" classes
if errorlevel 1 (
    echo.
    echo BUILD FAILED.
    pause
    exit /b 1
)

echo.
echo [2/2] Launching CrystalGardenGPU...
echo.
call "%GRADLE_CMD%" run
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo APPLICATION EXITED WITH ERROR CODE %EXIT_CODE%.
    pause
)

exit /b %EXIT_CODE%
