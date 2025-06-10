@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  calculator-game startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and CALCULATOR_GAME_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\calculator-game.jar;%APP_HOME%\lib\jython-slim-2.7.4.jar;%APP_HOME%\lib\json-20231013.jar;%APP_HOME%\lib\antlr-3.5.3.jar;%APP_HOME%\lib\javax.servlet-api-3.1.0.jar;%APP_HOME%\lib\ST4-4.3.1.jar;%APP_HOME%\lib\antlr-runtime-3.5.3.jar;%APP_HOME%\lib\commons-compress-1.26.2.jar;%APP_HOME%\lib\commons-io-2.16.1.jar;%APP_HOME%\lib\bcpkix-jdk18on-1.78.1.jar;%APP_HOME%\lib\bcutil-jdk18on-1.78.1.jar;%APP_HOME%\lib\bcprov-jdk18on-1.78.1.jar;%APP_HOME%\lib\jnr-netdb-1.2.0.jar;%APP_HOME%\lib\jnr-posix-3.1.19.jar;%APP_HOME%\lib\jnr-ffi-2.2.16.jar;%APP_HOME%\lib\asm-commons-9.7.jar;%APP_HOME%\lib\asm-util-9.7.jar;%APP_HOME%\lib\asm-analysis-9.7.jar;%APP_HOME%\lib\asm-tree-9.7.jar;%APP_HOME%\lib\asm-9.7.jar;%APP_HOME%\lib\guava-33.2.1-jre.jar;%APP_HOME%\lib\icu4j-75.1.jar;%APP_HOME%\lib\java-sizeof-0.0.5.jar;%APP_HOME%\lib\jffi-1.3.13.jar;%APP_HOME%\lib\jffi-1.3.13-native.jar;%APP_HOME%\lib\jnr-constants-0.10.4.jar;%APP_HOME%\lib\jline-2.14.6.jar;%APP_HOME%\lib\netty-handler-4.1.73.Final.jar;%APP_HOME%\lib\netty-codec-4.1.73.Final.jar;%APP_HOME%\lib\netty-transport-4.1.73.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.73.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.73.Final.jar;%APP_HOME%\lib\netty-common-4.1.73.Final.jar;%APP_HOME%\lib\ant-1.10.14.jar;%APP_HOME%\lib\commons-codec-1.17.0.jar;%APP_HOME%\lib\commons-lang3-3.14.0.jar;%APP_HOME%\lib\failureaccess-1.0.2.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.42.0.jar;%APP_HOME%\lib\error_prone_annotations-2.26.1.jar;%APP_HOME%\lib\jnr-a64asm-1.0.0.jar;%APP_HOME%\lib\jnr-x86asm-1.0.2.jar;%APP_HOME%\lib\netty-tcnative-classes-2.0.46.Final.jar;%APP_HOME%\lib\ant-launcher-1.10.14.jar


@rem Execute calculator-game
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CALCULATOR_GAME_OPTS%  -classpath "%CLASSPATH%"  %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable CALCULATOR_GAME_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%CALCULATOR_GAME_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
