# 🛡️ SMA Bug Triage - Sistema de Priorización con Naïve Bayes

Este proyecto implementa un **Sistema Multi-Agente (SMA)** distribuido utilizando el framework **JADE**. El objetivo es automatizar el triaje de reportes de errores (bugs) clasificándolos en **Alta** o **Baja** prioridad mediante una lógica probabilística de **Naïve Bayes**.

## 🏗️ Arquitectura Distribuida

El sistema está diseñado para operar en un entorno de red distribuido, simulado mediante dos contenedores lógicos de JADE:

1. **Main-Container (Nodo de Operaciones):** Aloja la Interfaz de usuario, el Tokenizador de texto y el Clasificador (Cerebro).
2. **Container-1 (Nodo de Datos):** Aloja el Repositorio de Modelos, simulando un servidor remoto de conocimiento.

## 🤖 Agentes del Sistema

- **AgenteInterfaz:** Gestiona la entrada del usuario y orquestra el flujo de mensajes.
- **AgenteTokenizer:** Normaliza y limpia el texto del reporte.
- **AgenteClasificador:** Realiza la inferencia bayesiana mediante sumas de log-probabilidades.
- **AgenteRepositorio:** Provee el modelo de conocimiento y gestiona el aprendizaje simulado.

## 🚀 Guía de Ejecución

El proyecto requiere **Maven** y un **JDK (17+)**.

1. **Compilar el proyecto:**
   ```bash
   mvn clean compile
   ```
2. **Iniciar el Main Container:**
   Ejecutar run_main_container.bat. Esto abrirá la GUI de JADE y preparará los agentes operativos.
3. **Iniciar el Nodo Remoto:**
   Ejecutar run_remote_container.bat. El Repositorio se registrará en el DF y el Clasificador descargará automáticamente el modelo.

## 📊 Protocolo FIPA-ACL

Se utilizan protocolos de comunicación estándar para garantizar la interoperabilidad:

- **FIPA-Request:** Para la carga del modelo y solicitudes de clasificación.
- **FIPA-Inform:** Para la entrega de resultados y actualizaciones de feedback.
- **Directory Facilitator (DF):** Descubrimiento dinámico de servicios.
