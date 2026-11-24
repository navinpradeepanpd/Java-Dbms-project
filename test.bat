@echo off
echo Running CineBook System Test Suite...
set JRE_BIN=C:\Users\Navin Pradeepan\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin
if exist "%JRE_BIN%\java.exe" (
    "%JRE_BIN%\java.exe" -cp "target/classes;lib/*" com.cinebook.test.CineBookSystemTest
) else (
    java -cp "target/classes;lib/*" com.cinebook.test.CineBookSystemTest
)
pause

