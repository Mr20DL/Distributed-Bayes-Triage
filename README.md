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

🚀 Guía de Ejecución (Docker)

**Requisitos previos:** Tener instalado **Docker**, **Docker Compose** y usar entorno de compilación **Java 17**.

### 1. Levantar la Infraestructura

Para compilar el código (Maven) y levantar los contenedores en segundo plano, ejecutar en la raíz del proyecto:

```bash
docker-compose up --build -d
```

### 2. Interactuar con el Sistema (Consola)

Para cconectar a la terminal del contenedor principal, ejecutar en la raíz del proyecto:

```bash
docker attach p1_main_container
```

(Nota: Una vez dentro, presiona Enter si ves la consola parpadeando para refrescar la vista. Para salir de esta vista sin apagar el contenedor, usa la combinación Ctrl + P, luego Ctrl + Q).

### 3. Apagar el Sistema

Para detener y eliminar los contenedores creados:

```bash
docker-compose down
```

(Nota: Una vez dentro, presiona Enter si ves la consola parpadeando para refrescar la vista. Para salir de esta vista sin apagar el contenedor, usa la combinación Ctrl + P, luego Ctrl + Q).

### Ejecución local sin Docker

```bash
mvn exec:java "-Dexec.mainClass=jade.Boot" "-Dexec.args=-gui -agents interfaz:com.sma.agents.AgenteInterfaz;tokenizer:com.sma.agents.AgenteTokenizer"
```

## 📊 Protocolo FIPA-ACL

Se utilizan protocolos de comunicación estándar para garantizar la interoperabilidad:

- **FIPA-Request:** Para la carga del modelo y solicitudes de clasificación.
- **FIPA-Inform:** Para la entrega de resultados y actualizaciones de feedback.
- **Directory Facilitator (DF):** Descubrimiento dinámico de servicios.
