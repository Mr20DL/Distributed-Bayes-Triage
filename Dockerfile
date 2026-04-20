FROM maven:3.8-openjdk-17
WORKDIR /app
COPY . .
RUN mvn clean compile
# El comando de inicio se sobreescribirá en el compose