@echo off
setlocal
set DIR=%~dp0
start "" "%DIR%runtime\bin\javaw.exe" --enable-native-access=ALL-UNNAMED -jar "%DIR%sunrise-launcher-jp.jar"
endlocal
exit
