# 🛡️ SMA Bug Triage – Priorización con Naïve Bayes sobre JADE

Sistema Multi‑Agente distribuido que clasifica reportes de bugs en **Alta** o **Baja** prioridad utilizando un modelo probabilístico de **Naïve Bayes** (suma de log‑probabilidades).  
La arquitectura se despliega con **Docker Compose** en dos contenedores.

## 🏗️ Arquitectura

| Contenedor        | Rol                        | Agentes contenidos                      |
| ----------------- | -------------------------- | --------------------------------------- |
| `main-platform`   | Contenedor principal (GUI) | `Interfaz`, `Tokenizer`, `Clasificador` |
| `remote-platform` | Contenedor remoto          | `Repositorio` (modelo Bayesiano)        |

- El contenedor principal expone el puerto **1099** para que el remoto se conecte.
- El descubrimiento de servicios se realiza a través del **Directory Facilitator (DF)**.

## 🤖 Agentes del Sistema

- **AgenteInterfaz:** Gestiona la entrada del usuario y orquestra el flujo de mensajes.
- **AgenteTokenizer:** Normaliza y limpia el texto del reporte.
- **AgenteClasificador:** Realiza la inferencia bayesiana mediante sumas de log-probabilidades.
- **AgenteRepositorio:** Provee el modelo de conocimiento y gestiona el aprendizaje simulado.

## 🚀 Guía de Ejecución

El proyecto requiere **Docker** , **Maven** y un **JDK (17+)**.

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/Mr20DL/Distributed-Bayes-Triage
   cd Distributed-Bayes-Triage
   ```
2. **Construir y levantar los contenedores**
   ```bash
   docker-compose up -d
   ```
   Esto iniciará los dos contenedores en segundo plano. La GUI de JADE no se muestra, pero toda la interacción se hace por consola.
3. **Adjuntarse al contenedor principal para ingresar bugs**
   ```bash
   docker attach main-container-node
   ```
   Verás el prompt del agente Interfaz. Escribe un reporte de bug y presiona Enter.
4. **Ver logs del repositorio remoto**
   ```bash
   docker logs -f remote-container-node
   ```
5. **Detener los contenedores**
   ```bash
   docker-compose down
   ```

## 🧪 Ejemplo de uso

```text
INTERFAZ: Iniciada. Escriba el reporte del bug:
Error critico en servidor de login
TOKENIZER: [error critico servidor login] -> [error critico servidor login]
CLASIFICADOR: Conocimiento cargado. Listo para clasificar.
>>> RESULTADO DEL TRIAJE: ALTA PRIORIDAD
   Es correcta la clasificacion? (S/N):
```

Si responde N, se envía una corrección de aprendizaje al repositorio remoto.

## 📊 Protocolo FIPA-ACL

Se utilizan protocolos de comunicación estándar para garantizar la interoperabilidad:

- **FIPA-Request:** Para la carga del modelo y solicitudes de clasificación.
- **FIPA-Inform:** Para la entrega de resultados y actualizaciones de feedback.
- **FIPA-Confirm:** Para el acuse de recibo del feedback por parte del repositorio.
- **Directory Facilitator (DF):** Descubrimiento dinámico de servicios.
