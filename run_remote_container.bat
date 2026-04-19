@echo off
echo Iniciando Container 1 y conectando al Main Container...
mvn exec:java -Dexec.mainClass="jade.Boot" -Dexec.args="-container -host localhost -port 1099"
pause