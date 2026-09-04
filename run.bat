@echo off
setlocal EnableExtensions EnableDelayedExpansion
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
rem Portable JDK 21
rem ---------------------------------------------------------------------------
rem The project requires a real JDK 21 (javac + java), not just whatever Java
rem happens to be registered in Windows. Cache Temurin 21 inside the repo's
rem ignored .jdk folder so the launcher is self-contained after the first run.
set "JDK_ROOT=%~dp0.jdk\portable"
set "JDK_ZIP=%JDK_ROOT%\temurin-jdk21-windows-x64.zip"
set "JDK_URL=https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse"
set "JAVA_HOME="

if exist "%JDK_ROOT%" (
    for /d %%D in ("%JDK_ROOT%\jdk-*") do (
        if not defined JAVA_HOME if exist "%%~fD\bin\javac.exe" set "JAVA_HOME=%%~fD"
    )
)

if defined JAVA_HOME goto :jdk_ready

echo JDK 21 is not cached yet.
echo Downloading Eclipse Temurin JDK 21 for this project...
echo This only happens on the first run.
echo.

if not exist "%JDK_ROOT%" mkdir "%JDK_ROOT%"
if exist "%JDK_ZIP%" del /q "%JDK_ZIP%" >nul 2>&1

where curl.exe >nul 2>&1
if not errorlevel 1 (
    echo Using curl for the JDK download...
    curl.exe -L --fail --retry 3 --connect-timeout 20 -o "%JDK_ZIP%" "%JDK_URL%"
    if errorlevel 1 goto :jdk_download_failed
) else (
    where powershell >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Neither curl.exe nor PowerShell was found.
        echo One of them is required for the one-time JDK download.
        pause
        exit /b 1
    )
    echo Using PowerShell for the JDK download...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri $env:JDK_URL -OutFile $env:JDK_ZIP"
    if errorlevel 1 goto :jdk_download_failed
)

echo.
echo Extracting JDK 21...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -LiteralPath $env:JDK_ZIP -DestinationPath $env:JDK_ROOT -Force"
if errorlevel 1 (
    echo ERROR: Failed to extract JDK 21.
    pause
    exit /b 1
)
del /q "%JDK_ZIP%" >nul 2>&1

for /d %%D in ("%JDK_ROOT%\jdk-*") do (
    if not defined JAVA_HOME if exist "%%~fD\bin\javac.exe" set "JAVA_HOME=%%~fD"
)

if not defined JAVA_HOME (
    echo ERROR: JDK 21 downloaded, but its extracted folder could not be found.
    echo Expected a folder matching: %JDK_ROOT%\jdk-*
    pause
    exit /b 1
)
goto :jdk_ready

:jdk_download_failed
echo.
echo ERROR: Failed to download JDK 21.
echo Check your internet connection and run this file again.
echo.
pause
exit /b 1

:jdk_ready
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"

if not exist "%JAVA_EXE%" (
    echo ERROR: java.exe is missing from the cached JDK:
    echo %JAVA_EXE%
    pause
    exit /b 1
)
if not exist "%JAVAC_EXE%" (
    echo ERROR: javac.exe is missing from the cached JDK:
    echo %JAVAC_EXE%
    pause
    exit /b 1
)

set "JAVAC_VERSION="
for /f "tokens=2" %%V in ('"%JAVAC_EXE%" -version 2^>^&1') do if not defined JAVAC_VERSION set "JAVAC_VERSION=%%V"
if not "!JAVAC_VERSION:~0,2!"=="21" (
    echo ERROR: The cached compiler is not JDK 21.
    echo Found: javac !JAVAC_VERSION!
    echo Delete .jdk\portable and run this file again.
    pause
    exit /b 1
)

rem Make Gradle toolchain detection see the same JDK that launches Gradle.
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem ---------------------------------------------------------------------------
rem Portable Gradle
rem ---------------------------------------------------------------------------
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
echo JDK check: OK ^(javac !JAVAC_VERSION!^)
echo Java: %JAVA_EXE%
echo Gradle home: %LOCAL_GRADLE_HOME%
echo Launcher mode: direct Java/JAR ^(bypasses broken gradle.bat^)
echo Checking Gradle...
call :run_gradle --version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Gradle cannot start with the portable JDK.
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
"%JAVA_EXE%" -Xmx64m -Xms64m "-javaagent:%GRADLE_AGENT_JAR%" "-Dorg.gradle.appname=gradle" "-Dorg.gradle.java.installations.paths=%JAVA_HOME%" -jar "%GRADLE_CLI_JAR%" %*
exit /b %ERRORLEVEL%
