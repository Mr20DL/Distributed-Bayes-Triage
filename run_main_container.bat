@echo off
set MVN_PATH="C:\Users\Carlos Andres\.vscode\extensions\oracle.oracle-java-25.1.0\nbcode\java\maven\bin\mvn.cmd"
echo Iniciando Main Container...
%MVN_PATH% exec:java -Dexec.mainClass="jade.Boot" -Dexec.args="-gui -agents Interfaz:com.sma.agents.AgenteInterfaz;Tokenizer:com.sma.agents.AgenteTokenizer;Clasificador:com.sma.agents.AgenteClasificador"
pause