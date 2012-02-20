@echo off
REM script to automate delivery correctly
REM this script use command supplied by cygwin, so it is a linux like script, and not a classical dos batch file

mkdir testPolicyManager

REM copy the repository to deliver in flux_profil_delivery
cp -r scripts testPolicyManager
REM copy the configuration file
cp -r config testPolicyManager

REM allow user to modify all file
chmod -R u+w testPolicyManager

REM pack all data in a zip file
"c:\Program Files\7-Zip\7z" a testPolicyManager.zip testPolicyManager/

REM erase temp repository testPolicyManager
rm -rf testPolicyManager

@echo on
