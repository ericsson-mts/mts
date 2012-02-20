@echo off
REM script to automate delivery correctly
REM this script use command supplied by cygwin, so it is a linux like script, and not a classical dos batch file

REM copy the repository to deliver in flux_profil_delivery
cp -r flux_profil flux_profil_delivery
REM allow user to modify all file
chmod -R u+w flux_profil_delivery

REM copy prod configuration instead of hour dev configuration
cp -r flux_profil_delivery/config_prod/* flux_profil_delivery/config_exemple
REM remove prod configuration repository
rm -rf flux_profil_delivery/config_prod

REM pack all data in a zip file
"c:\Program Files\7-Zip\7z" a flux_profil.zip flux_profil_delivery/

REM erase temp repository flux_profil_delivery
rm -rf flux_profil_delivery

@echo on
