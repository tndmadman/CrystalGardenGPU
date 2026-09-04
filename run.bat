@echo off
setlocal EnableExtensions EnableDelayedExpansion
title CrystalGardenGPU - Compile and Run
cd /d "%~dp0"

echo ========================================
echo   CrystalGardenGPU - Compile and Run
echo ========================================
echo.

set "JAVA_OPTS="
set "GRADLE_OPTS="
set "JAVA_TOOL_OPTIONS="
set "_JAVA_OPTIONS="
set "JDK_JAVA_OPTIONS="
set "CLASSPATH="

rem ---------------------------------------------------------------------------
rem Java 20
rem ---------------------------------------------------------------------------
set "JAVA_HOME="
set "JAVA_EXE="
set "JAVAC_EXE="
set "JAVAC_VERSION="

rem Prefer an already-installed JDK 20 if javac 20 is on PATH.
for /f "delims=" %%J in ('where javac 2^>nul') do if not defined JAVAC_EXE set "JAVAC_EXE=%%J"
if defined JAVAC_EXE (
    for /f "tokens=2" %%V in ('"!JAVAC_EXE!" -version 2^>^&1') do if not defined JAVAC_VERSION set "JAVAC_VERSION=%%V"
    if "!JAVAC_VERSION:~0,2!"=="20" (
        rem Do not derive JAVA_HOME from the javac.exe path because Oracle may
        rem expose javac through a Common Files shim. Ask the JVM for its real
        rem home instead; this resolves to the actual JDK installation.
        for /f "tokens=1,* delims==" %%A in ('"!JAVAC_EXE!" -J-XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home ="') do (
            set "JAVA_HOME=%%B"
        )
        if defined JAVA_HOME for /f "tokens=*" %%H in ("!JAVA_HOME!") do set "JAVA_HOME=%%H"
        if defined JAVA_HOME set "JAVA_EXE=!JAVA_HOME!\bin\java.exe"

        if not exist "!JAVA_EXE!" (
            set "JAVA_HOME="
            set "JAVA_EXE="
            set "JAVAC_EXE="
            set "JAVAC_VERSION="
        ) else if not exist "!JAVA_HOME!\bin\javac.exe" (
            set "JAVA_HOME="
            set "JAVA_EXE="
            set "JAVAC_EXE="
            set "JAVAC_VERSION="
        ) else (
            set "JAVAC_EXE=!JAVA_HOME!\bin\javac.exe"
        )
    ) else (
        set "JAVAC_EXE="
        set "JAVAC_VERSION="
    )
)

rem Otherwise use/cache a project-local Temurin JDK 20.
if not defined JAVA_HOME (
    set "JDK_ROOT=%~dp0.jdk\portable20"
    set "JDK_ZIP=!JDK_ROOT!\temurin-jdk20-windows-x64.zip"
    set "JDK_URL=https://api.adoptium.net/v3/binary/latest/20/ga/windows/x64/jdk/hotspot/normal/eclipse"

    if exist "!JDK_ROOT!" (
        for /d %%D in ("!JDK_ROOT!\jdk-*") do (
            if not defined JAVA_HOME if exist "%%~fD\bin\javac.exe" (
                set "CANDIDATE_JAVAC=%%~fD\bin\javac.exe"
                set "CANDIDATE_VERSION="
                for /f "tokens=2" %%V in ('"!CANDIDATE_JAVAC!" -version 2^>^&1') do if not defined CANDIDATE_VERSION set "CANDIDATE_VERSION=%%V"
                if "!CANDIDATE_VERSION:~0,2!"=="20" set "JAVA_HOME=%%~fD"
            )
        )
    )

    if not defined JAVA_HOME (
        echo JDK 20 is not installed/cached yet.
        echo Downloading Eclipse Temurin JDK 20 for this project...
        echo This only happens on the first run.
        echo.

        if not exist "!JDK_ROOT!" mkdir "!JDK_ROOT!"
        if exist "!JDK_ZIP!" del /q "!JDK_ZIP!" >nul 2>&1

        where curl.exe >nul 2>&1
        if not errorlevel 1 (
            curl.exe -L --fail --retry 3 --connect-timeout 20 -o "!JDK_ZIP!" "!JDK_URL!"
            if errorlevel 1 goto :jdk_download_failed
        ) else (
            powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri $env:JDK_URL -OutFile $env:JDK_ZIP"
            if errorlevel 1 goto :jdk_download_failed
        )

        echo Extracting JDK 20...
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -LiteralPath $env:JDK_ZIP -DestinationPath $env:JDK_ROOT -Force"
        if errorlevel 1 (
            echo ERROR: Failed to extract JDK 20.
            pause
            exit /b 1
        )
        del /q "!JDK_ZIP!" >nul 2>&1

        for /d %%D in ("!JDK_ROOT!\jdk-*") do (
            if not defined JAVA_HOME if exist "%%~fD\bin\javac.exe" set "JAVA_HOME=%%~fD"
        )
    )

    if not defined JAVA_HOME (
        echo ERROR: JDK 20 could not be located after download/extraction.
        pause
        exit /b 1
    )

    set "JAVA_EXE=!JAVA_HOME!\bin\java.exe"
    set "JAVAC_EXE=!JAVA_HOME!\bin\javac.exe"
    set "JAVAC_VERSION="
    for /f "tokens=2" %%V in ('"!JAVAC_EXE!" -version 2^>^&1') do if not defined JAVAC_VERSION set "JAVAC_VERSION=%%V"
)

if not "!JAVAC_VERSION:~0,2!"=="20" (
    echo ERROR: JDK 20 is required, but found javac !JAVAC_VERSION!.
    pause
    exit /b 1
)

if not exist "!JAVA_EXE!" (
    echo ERROR: Could not find java.exe in resolved JDK home:
    echo !JAVA_HOME!
    pause
    exit /b 1
)

set "PATH=!JAVA_HOME!\bin;%PATH%"

rem ---------------------------------------------------------------------------
rem Portable Gradle
rem ---------------------------------------------------------------------------
set "GRADLE_VERSION=8.14.3"
set "LOCAL_GRADLE_ROOT=%~dp0.gradle\portable"
set "LOCAL_GRADLE_HOME=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%"
set "LOCAL_GRADLE_ZIP=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_CLI_JAR=!LOCAL_GRADLE_HOME!\lib\gradle-gradle-cli-main-%GRADLE_VERSION%.jar"
set "GRADLE_AGENT_JAR=!LOCAL_GRADLE_HOME!\lib\agents\gradle-instrumentation-agent-%GRADLE_VERSION%.jar"

if exist "!GRADLE_CLI_JAR!" if exist "!GRADLE_AGENT_JAR!" goto :gradle_ready

echo Gradle %GRADLE_VERSION% is not cached yet.
echo Downloading portable Gradle...
if not exist "!LOCAL_GRADLE_ROOT!" mkdir "!LOCAL_GRADLE_ROOT!"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri ('https://services.gradle.org/distributions/gradle-' + $env:GRADLE_VERSION + '-bin.zip') -OutFile $env:LOCAL_GRADLE_ZIP; Expand-Archive -LiteralPath $env:LOCAL_GRADLE_ZIP -DestinationPath $env:LOCAL_GRADLE_ROOT -Force; Remove-Item -LiteralPath $env:LOCAL_GRADLE_ZIP -Force"
if errorlevel 1 (
    echo ERROR: Failed to download or extract Gradle.
    pause
    exit /b 1
)

:gradle_ready
echo JDK check: OK ^(javac !JAVAC_VERSION!^)
echo JDK home: !JAVA_HOME!
echo Java: !JAVA_EXE!
echo Gradle home: !LOCAL_GRADLE_HOME!
echo Checking Gradle...
call :run_gradle --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Gradle cannot start with JDK 20.
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
set "EXIT_CODE=!ERRORLEVEL!"

if not "!EXIT_CODE!"=="0" (
    echo.
    echo APPLICATION EXITED WITH ERROR CODE !EXIT_CODE!.
    pause
)
exit /b !EXIT_CODE!

:jdk_download_failed
echo.
echo ERROR: Failed to download JDK 20.
echo Check your internet connection and try again.
pause
exit /b 1

:run_gradle
"!JAVA_EXE!" -Xmx64m -Xms64m "-javaagent:!GRADLE_AGENT_JAR!" "-Dorg.gradle.appname=gradle" "-Dorg.gradle.java.installations.paths=!JAVA_HOME!" -jar "!GRADLE_CLI_JAR!" %*
exit /b !ERRORLEVEL!
