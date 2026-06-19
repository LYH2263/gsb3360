@echo off
setlocal

set MVNW_DIR=%~dp0
set WRAPPER_DIR=%MVNW_DIR%\.mvn\wrapper
set JAR=%WRAPPER_DIR%\maven-wrapper.jar
set PROPS=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%PROPS%" (
  echo Missing %PROPS%
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in ('findstr /b wrapperUrl= "%PROPS%"') do set WRAPPER_URL=%%B

if not exist "%JAR%" (
  echo Downloading Maven Wrapper jar...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$p='%JAR%'; $u='%WRAPPER_URL%'; New-Item -ItemType Directory -Force -Path (Split-Path $p) | Out-Null; (New-Object Net.WebClient).DownloadFile($u,$p)"
)

if defined JAVA_HOME (
  "%JAVA_HOME%\bin\java" -jar "%JAR%" -Dmaven.multiModuleProjectDirectory="%MVNW_DIR%" %*
) else (
  java -jar "%JAR%" -Dmaven.multiModuleProjectDirectory="%MVNW_DIR%" %*
)

