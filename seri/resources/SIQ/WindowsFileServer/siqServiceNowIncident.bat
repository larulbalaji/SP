@echo off
rem Additional logging
rem @echo UserAndFiles=%1 > C:\SailPoint\Tools\Snow.txt
PowerShell.exe -NoProfile -ExecutionPolicy Bypass -Command "& 'C:\SailPoint\Tools\siqServiceNowIncident.ps1' -UserAndFiles ""%1"" "