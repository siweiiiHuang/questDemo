@ECHO OFF
SETLOCAL

SET DIR=%~dp0
SET WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Gradle wrapper jar not found. Please generate it with "gradle wrapper".
  EXIT /B 1
)

"%DIR%gradle\wrapper\gradle-wrapper" %*


