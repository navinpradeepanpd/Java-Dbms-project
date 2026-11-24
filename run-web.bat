@echo off
echo ======================================================================
echo           Starting CineBook Web Application (Embedded Server)
echo ======================================================================
echo Open your browser at: http://localhost:8080
echo ======================================================================
call "%~dp0mvn.cmd" spring-boot:run
pause

