@echo off
set MVN_PATH="C:\Users\Carlos Andres\.vscode\extensions\oracle.oracle-java-25.1.0\nbcode\java\maven\bin\mvn.cmd"
echo Iniciando Container-1...
%MVN_PATH% exec:java -Dexec.mainClass="jade.Boot" -Dexec.args="-container -host localhost -agents Repositorio:com.sma.agents.AgenteRepositorio"
pause