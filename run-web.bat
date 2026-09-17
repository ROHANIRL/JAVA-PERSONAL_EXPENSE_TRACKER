@echo off
setlocal
if not exist "lib\mysql-connector-j-*.jar" (
  echo Missing MySQL JDBC driver.
  echo Download mysql-connector-j and put its .jar file in the lib folder, then run this file again.
  pause
  exit /b 1
)
if not exist out mkdir out
javac --add-modules jdk.httpserver -encoding UTF-8 -cp "lib\*" -d out src\*.java src\dao\*.java src\gui\*.java src\menu\*.java src\model\*.java src\service\*.java src\util\*.java src\web\*.java
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
java --add-modules jdk.httpserver -cp "out;lib\*" web.ApiServer %1
endlocal
