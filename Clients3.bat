@echo off
set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot\bin\javaw.exe"

if not exist "%JAVA_EXE%" (
    echo ❌ Java не найдена: %JAVA_EXE%
    pause
    exit /b 1
)

start "" "%JAVA_EXE%" -jar "LLM-Ontology.jar"
exit


