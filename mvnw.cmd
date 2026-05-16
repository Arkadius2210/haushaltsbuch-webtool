@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto checkMavenWrapper
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto error

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto checkMavenWrapper
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto error

:checkMavenWrapper
if exist "%MAVEN_WRAPPER_JAR%" goto runMaven

echo Downloading Maven Wrapper...
for /f "usebackq tokens=1,2 delims==" %%a in ("%MAVEN_WRAPPER_PROPERTIES%") do (
    if "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
)
powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%MAVEN_WRAPPER_JAR%')"
if "%ERRORLEVEL%" NEQ "0" (
    echo ERROR: Failed to download Maven Wrapper.
    goto error
)

:runMaven
"%JAVA_EXE%" -jar "%MAVEN_WRAPPER_JAR%" %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
endlocal
exit /b %ERROR_CODE%
