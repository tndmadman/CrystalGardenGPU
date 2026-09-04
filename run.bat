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

rem ---------------------------------------------------------------------------
rem Use the Java 20 installation already configured in Windows PATH.
rem Do NOT resolve JAVA_HOME or executable paths; Oracle's javapath shim lives
rem under Program Files and caused CMD quoting/parsing failures in earlier runs.
rem ---------------------------------------------------------------------------
where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: java was not found in PATH.
    pause
    exit /b 1
)

where javac >nul 2>&1
if errorlevel 1 (
    echo ERROR: javac was not found in PATH.
    echo A full JDK 20 installation is required.
    pause
    exit /b 1
)

set "JAVA_VERSION="
set "JAVAC_VERSION="

for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /b /c:"java version"') do if not defined JAVA_VERSION set "JAVA_VERSION=%%~V"
for /f "tokens=2" %%V in ('javac -version 2^>^&1') do if not defined JAVAC_VERSION set "JAVAC_VERSION=%%V"

if not "%JAVA_VERSION:~0,2%"=="20" (
    echo ERROR: Java 20 is required.
    echo Found java %JAVA_VERSION%
    echo.
    java -version
    pause
    exit /b 1
)

if not "%JAVAC_VERSION:~0,2%"=="20" (
    echo ERROR: JDK 20 is required.
    echo Found javac %JAVAC_VERSION%
    echo.
    javac -version
    pause
    exit /b 1
)

echo Java check: OK ^(%JAVA_VERSION%^) 
echo Javac check: OK ^(%JAVAC_VERSION%^) 
echo.

rem ---------------------------------------------------------------------------
rem Portable Gradle. Java/Javac come from PATH.
rem ---------------------------------------------------------------------------
set "GRADLE_VERSION=8.14.3"
set "LOCAL_GRADLE_ROOT=%~dp0.gradle\portable"
set "LOCAL_GRADLE_HOME=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%"
set "LOCAL_GRADLE_ZIP=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_CLI_JAR=%LOCAL_GRADLE_HOME%\lib\gradle-gradle-cli-main-%GRADLE_VERSION%.jar"
set "GRADLE_AGENT_JAR=%LOCAL_GRADLE_HOME%\lib\agents\gradle-instrumentation-agent-%GRADLE_VERSION%.jar"

if exist "%GRADLE_CLI_JAR%" if exist "%GRADLE_AGENT_JAR%" goto :gradle_ready

echo Gradle %GRADLE_VERSION% is not cached yet.
echo Downloading portable Gradle...
if not exist "%LOCAL_GRADLE_ROOT%" mkdir "%LOCAL_GRADLE_ROOT%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri ('https://services.gradle.org/distributions/gradle-' + $env:GRADLE_VERSION + '-bin.zip') -OutFile $env:LOCAL_GRADLE_ZIP; Expand-Archive -LiteralPath $env:LOCAL_GRADLE_ZIP -DestinationPath $env:LOCAL_GRADLE_ROOT -Force; Remove-Item -LiteralPath $env:LOCAL_GRADLE_ZIP -Force"
if errorlevel 1 (
    echo ERROR: Failed to download or extract Gradle.
    pause
    exit /b 1
)

:gradle_ready
echo Gradle home: %LOCAL_GRADLE_HOME%
echo Checking Gradle...
call :run_gradle --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Gradle cannot start with the installed Java 20.
    echo Running Gradle version check visibly:
    call :run_gradle --version
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
java -Xmx64m -Xms64m "-javaagent:%GRADLE_AGENT_JAR%" "-Dorg.gradle.appname=gradle" -jar "%GRADLE_CLI_JAR%" %*
exit /b %ERRORLEVEL%
