@echo off
echo Iniciando JADE Main Container en el puerto 1099...
mvn clean compile exec:java -Dexec.mainClass="jade.Boot" -Dexec.args="-gui -port 1099"
pause