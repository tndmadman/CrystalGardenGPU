@echo off
setlocal
title CrystalGardenGPU - Compile and Run
cd /d "%~dp0"

echo ========================================
echo   CrystalGardenGPU - Compile and Run
echo ========================================
echo.

rem Isolate this project from machine-wide Java/Gradle flags that can inject
rem malformed options into every Java process.
set "JAVA_OPTS="
set "GRADLE_OPTS="
set "JAVA_TOOL_OPTIONS="
set "_JAVA_OPTIONS="
set "JDK_JAVA_OPTIONS="

rem Gradle 8.14.3 has a Windows launcher bug where an undefined/empty
rem CLASSPATH can become: java -classpath "" ...
rem Java rejects that with "-classpath requires class path specification".
rem A harmless non-empty classpath works around the upstream launcher bug.
set "CLASSPATH=."

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java was not found in PATH.
    echo Install JDK 21 and make sure java.exe is available from Command Prompt.
    echo.
    pause
    exit /b 1
)

rem Verify Java itself can launch before involving Gradle.
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java exists, but Java itself failed to start.
    echo.
    echo Java executable:
    where java
    echo.
    echo Running java -version for diagnostics:
    java -version
    echo.
    pause
    exit /b 1
)

rem Prefer a committed Gradle wrapper when one exists.
if exist "%~dp0gradlew.bat" (
    set "GRADLE_CMD=%~dp0gradlew.bat"
    goto :gradle_ready
)

rem Otherwise use a project-local portable Gradle installation.
set "GRADLE_VERSION=8.14.3"
set "LOCAL_GRADLE_ROOT=%~dp0.gradle\portable"
set "LOCAL_GRADLE_HOME=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%"
set "LOCAL_GRADLE_ZIP=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_CMD=%LOCAL_GRADLE_HOME%\bin\gradle.bat"

if exist "%GRADLE_CMD%" goto :gradle_ready

echo Gradle %GRADLE_VERSION% is not cached yet.
echo Downloading a portable copy for this project...
echo This only happens on the first run.
echo.

where powershell >nul 2>&1
if errorlevel 1 (
    echo ERROR: PowerShell was not found.
    echo It is required once to download the portable Gradle package.
    echo.
    pause
    exit /b 1
)

if not exist "%LOCAL_GRADLE_ROOT%" mkdir "%LOCAL_GRADLE_ROOT%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri ('https://services.gradle.org/distributions/gradle-' + $env:GRADLE_VERSION + '-bin.zip') -OutFile $env:LOCAL_GRADLE_ZIP; Expand-Archive -LiteralPath $env:LOCAL_GRADLE_ZIP -DestinationPath $env:LOCAL_GRADLE_ROOT -Force; Remove-Item -LiteralPath $env:LOCAL_GRADLE_ZIP -Force"
if errorlevel 1 (
    echo.
    echo ERROR: Failed to download or extract Gradle.
    echo Check your internet connection and try again.
    echo.
    pause
    exit /b 1
)

if not exist "%GRADLE_CMD%" (
    echo.
    echo ERROR: Gradle downloaded, but gradle.bat was not found where expected:
    echo %GRADLE_CMD%
    echo.
    pause
    exit /b 1
)

:gradle_ready
echo Java check: OK
echo Gradle: %GRADLE_CMD%
echo Gradle 8.14.3 Windows classpath workaround: ON
echo.

echo [1/2] Compiling...
call "%GRADLE_CMD%" --no-daemon classes
if errorlevel 1 (
    echo.
    echo BUILD FAILED.
    echo.
    pause
    exit /b 1
)

echo.
echo [2/2] Launching CrystalGardenGPU...
echo.
call "%GRADLE_CMD%" --no-daemon run
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo APPLICATION EXITED WITH ERROR CODE %EXIT_CODE%.
    pause
)

exit /b %EXIT_CODE%
