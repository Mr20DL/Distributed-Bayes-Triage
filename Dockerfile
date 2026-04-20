# Se utiliza la imagen oficial de Maven con Java 17
FROM maven:3.8.5-eclipse-temurin-17

# Se establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el archivo pom.xml y el código fuente
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto al construir la imagen
RUN mvn clean compile