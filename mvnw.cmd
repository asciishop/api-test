@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Apache Maven Wrapper startup batch script, version 3.3.2

@IF "%MAVEN_BATCH_ECHO%"=="on" echo %MAVEN_BATCH_ECHO%
@IF "%HOME%"=="" (SET "HOME=%HOMEDRIVE%%HOMEPATH%")

@setlocal

SET errorCode=0

@REM ==== FIND JAVA ====
IF NOT "%JAVA_HOME%"=="" GOTO OkJHome
FOR %%i IN (java.exe) DO SET "JAVA_EXE=%%~$PATH:i"
IF NOT "%JAVA_EXE%"=="" GOTO OkJHome

echo.
echo Error: JAVA_HOME not found. Please set JAVA_HOME to your JDK installation. >&2
echo.
SET errorCode=1
GOTO error

:OkJHome
IF "%JAVA_HOME%"=="" SET "JAVA_HOME=%JAVA_EXE:~0,-9%"
IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
    echo Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
    SET errorCode=1
    GOTO error
)

@REM ==== FIND PROJECT BASE DIR ====
SET "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO endDetectBaseDir

SET "EXEC_DIR=%CD%"
SET "WDIR=%EXEC_DIR%"

:findBaseDir
IF EXIST "%WDIR%\.mvn" GOTO baseDirFound
cd ..
IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
SET "WDIR=%CD%"
GOTO findBaseDir

:baseDirFound
SET "MAVEN_PROJECTBASEDIR=%WDIR%"
cd "%EXEC_DIR%"
GOTO endDetectBaseDir

:baseDirNotFound
SET "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
cd "%EXEC_DIR%"

:endDetectBaseDir

@REM ==== DOWNLOAD WRAPPER JAR IF MISSING ====
SET "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_PROPS=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

IF EXIST "%WRAPPER_JAR%" GOTO runMaven

echo Downloading Maven Wrapper JAR...
FOR /F "tokens=2 delims==" %%a IN ('findstr /i "wrapperUrl" "%WRAPPER_PROPS%"') DO SET WRAPPER_URL=%%a
SET "WRAPPER_URL=%WRAPPER_URL: =%"

powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%') }"
IF %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to download Maven Wrapper JAR. Check your internet connection. >&2
    SET errorCode=1
    GOTO error
)

@REM ==== RUN MAVEN ====
:runMaven
"%JAVA_HOME%\bin\java.exe" ^
  %MAVEN_OPTS% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  -classpath "%WRAPPER_JAR%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*

SET MVN_ERROR_CODE=%ERRORLEVEL%

:error
SET EXIT_CODE=%errorCode%
IF %MVN_ERROR_CODE% NEQ 0 SET EXIT_CODE=%MVN_ERROR_CODE%

IF "%MAVEN_BATCH_PAUSE%"=="on" pause
EXIT /B %EXIT_CODE%
