@echo off
cls

setlocal enableDelayedExpansion

set "config_file=%~dp0\start-config.properties"
set "jar_file=%~dp0\petpet-*.jar"

if not exist "%jar_file%" (
    echo Cannot find petpet jar file.
    pause
    exit /b 1
)

if exist "%config_file%" (
    set /p "user_option=" < %config_file% || set "user_option=0"
    echo Start Config: !user_option!
) else (
    set /p "user_option=Enter the option (0: WebServer, 1: GoCQ Plugin)[0]: " || set "user_option=0"
    set /p=!user_option!<nul >%config_file%
)

if "!user_option!" == "1" (
    set "args=-gocq"
) else (
    set "args="
)

for /f "delims=" %%a in ('dir /b /a-d %jar_file%') do (
    set "jar_file=%%a"
)

echo Running %jar_file%
if exist %SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe (
    powershell.exe -Command "cd '%~dp0'; java -jar -Xms16M "%jar_file%" %args%"
) else (
    cmd /c "cd /d %~dp0 && java -jar -Xms16M "%jar_file%" %args%"
)

title Petpet

endlocal
