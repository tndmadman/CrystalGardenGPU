@echo off
setlocal EnableExtensions
title CrystalGardenGPU - Compile and Run
cd /d "%~dp0"

echo ========================================
echo   CrystalGardenGPU - Compile and Run
echo ========================================
echo.

rem Keep machine-wide Java/Gradle flags from interfering with this project.
set "JAVA_OPTS="
set "GRADLE_OPTS="
set "JAVA_TOOL_OPTIONS="
set "_JAVA_OPTIONS="
set "JDK_JAVA_OPTIONS="
set "CLASSPATH="

set "JAVA_EXE="
for /f "delims=" %%J in ('where java 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%J"
if not defined JAVA_EXE (
    echo ERROR: Java was not found in PATH.
    echo Install JDK 21 and make sure java.exe is available from Command Prompt.
    echo.
    pause
    exit /b 1
)

"%JAVA_EXE%" -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java exists, but failed to start.
    echo Java: %JAVA_EXE%
    "%JAVA_EXE%" -version
    echo.
    pause
    exit /b 1
)

set "GRADLE_VERSION=8.14.3"
set "LOCAL_GRADLE_ROOT=%~dp0.gradle\portable"
set "LOCAL_GRADLE_HOME=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%"
set "LOCAL_GRADLE_ZIP=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_CLI_JAR=%LOCAL_GRADLE_HOME%\lib\gradle-gradle-cli-main-%GRADLE_VERSION%.jar"
set "GRADLE_AGENT_JAR=%LOCAL_GRADLE_HOME%\lib\agents\gradle-instrumentation-agent-%GRADLE_VERSION%.jar"

if exist "%GRADLE_CLI_JAR%" if exist "%GRADLE_AGENT_JAR%" goto :gradle_ready

echo Gradle %GRADLE_VERSION% is not cached yet.
echo Downloading a portable copy for this project...
echo This only happens on the first run.
echo.

where powershell >nul 2>&1
if errorlevel 1 (
    echo ERROR: PowerShell was not found.
    pause
    exit /b 1
)

if not exist "%LOCAL_GRADLE_ROOT%" mkdir "%LOCAL_GRADLE_ROOT%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri ('https://services.gradle.org/distributions/gradle-' + $env:GRADLE_VERSION + '-bin.zip') -OutFile $env:LOCAL_GRADLE_ZIP; Expand-Archive -LiteralPath $env:LOCAL_GRADLE_ZIP -DestinationPath $env:LOCAL_GRADLE_ROOT -Force; Remove-Item -LiteralPath $env:LOCAL_GRADLE_ZIP -Force"
if errorlevel 1 (
    echo.
    echo ERROR: Failed to download or extract Gradle.
    pause
    exit /b 1
)

if not exist "%GRADLE_CLI_JAR%" (
    echo ERROR: Gradle downloaded, but its CLI JAR was not found:
    echo %GRADLE_CLI_JAR%
    pause
    exit /b 1
)
if not exist "%GRADLE_AGENT_JAR%" (
    echo ERROR: Gradle downloaded, but its instrumentation agent was not found:
    echo %GRADLE_AGENT_JAR%
    pause
    exit /b 1
)

:gradle_ready
echo Java check: OK
echo Java: %JAVA_EXE%
echo Gradle home: %LOCAL_GRADLE_HOME%
echo Launcher mode: direct Java/JAR ^(bypasses broken gradle.bat^)
echo Checking Gradle...
call :run_gradle --version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Gradle still cannot start even with gradle.bat bypassed.
    echo Running the Gradle version check visibly for diagnostics:
    echo.
    call :run_gradle --version
    echo.
    pause
    exit /b 1
)
echo Gradle check: OK
echo.

echo [1/2] Compiling...
call :run_gradle --no-daemon classes
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
call :run_gradle --no-daemon run
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo APPLICATION EXITED WITH ERROR CODE %EXIT_CODE%.
    pause
)

exit /b %EXIT_CODE%

:run_gradle
"%JAVA_EXE%" -Xmx64m -Xms64m "-javaagent:%GRADLE_AGENT_JAR%" "-Dorg.gradle.appname=gradle" -jar "%GRADLE_CLI_JAR%" %*
exit /b %ERRORLEVEL%
