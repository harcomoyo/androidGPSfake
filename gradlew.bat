@if "%DEBUG%"=="" @echo off
setlocal
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

if defined JAVA_HOME goto findJava
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute
echo ERROR: Java was not found. Set JAVA_HOME to a JDK 17 installation. 1>&2
exit /b 1

:findJava
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto execute
echo ERROR: JAVA_HOME does not contain java.exe. 1>&2
exit /b 1

:execute
"%JAVA_EXE%" -Xmx64m "-Dorg.gradle.appname=gradlew" -jar "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" %*
endlocal
