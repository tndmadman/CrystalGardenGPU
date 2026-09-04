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

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java was not found in PATH.
    echo Install JDK 21 and make sure java.exe is available from Command Prompt.
    echo.
    pause
    exit /b 1
)

java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java exists, but failed to start.
    where java
    java -version
    echo.
    pause
    exit /b 1
)

set "GRADLE_VERSION=8.14.3"
set "LOCAL_GRADLE_ROOT=%~dp0.gradle\portable"
set "LOCAL_GRADLE_HOME=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%"
set "LOCAL_GRADLE_ZIP=%~dp0.gradle\portable\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_CMD=%LOCAL_GRADLE_HOME%\bin\gradle.bat"

if not exist "%GRADLE_CMD%" goto :download_gradle
goto :patch_gradle

:download_gradle
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

if not exist "%GRADLE_CMD%" (
    echo ERROR: Gradle downloaded, but gradle.bat was not found:
    echo %GRADLE_CMD%
    pause
    exit /b 1
)

:patch_gradle
rem Gradle 8.14.x generated Windows launchers can contain both
rem -classpath "%%CLASSPATH%%" and -jar. Because the distribution launcher
rem clears CLASSPATH internally, Java receives an empty classpath and exits.
rem Patch only our project-local Gradle copy by removing that redundant switch.
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $p=$env:GRADLE_CMD; $text=[IO.File]::ReadAllText($p); $pct=[string][char]37; $needle='-classpath "' + $pct + 'CLASSPATH' + $pct + '" -jar'; if($text.Contains($needle)){ $text=$text.Replace($needle,'-jar'); [IO.File]::WriteAllText($p,$text,[Text.Encoding]::ASCII); Write-Host 'Patched Gradle 8.14.3 Windows launcher.' }"
if errorlevel 1 (
    echo ERROR: Could not patch the local Gradle launcher.
    pause
    exit /b 1
)

echo Java check: OK
echo Gradle: %GRADLE_CMD%
echo Checking Gradle launcher...
call "%GRADLE_CMD%" --version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: The Gradle launcher still cannot start.
    echo Relevant lines from gradle.bat:
    findstr /n /i "classpath jar" "%GRADLE_CMD%"
    echo.
    pause
    exit /b 1
)
echo Gradle check: OK
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
