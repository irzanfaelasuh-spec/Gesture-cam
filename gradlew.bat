@echo off
setlocal
set VERSION=8.13
set BASE=%USERPROFILE%\.gradle\gestra-bootstrap
set INSTALL=%BASE%\gradle-%VERSION%
if not exist "%INSTALL%\bin\gradle.bat" (
  if not exist "%BASE%" mkdir "%BASE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%BASE%\gradle-%VERSION%-bin.zip'; Expand-Archive -Force '%BASE%\gradle-%VERSION%-bin.zip' '%BASE%'"
)
call "%INSTALL%\bin\gradle.bat" %*
endlocal
