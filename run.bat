@echo off
echo Starting CineBook Cinema Ticket Booking System...
set JRE_BIN=C:\Users\Navin Pradeepan\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin
if exist "%JRE_BIN%\java.exe" (
    "%JRE_BIN%\java.exe" -cp "target/classes;lib/*" com.cinebook.app.CineBookApp
) else (
    java -cp "target/classes;lib/*" com.cinebook.app.CineBookApp
)
pause

