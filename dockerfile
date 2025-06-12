FROM openjdk:17-jdk-slim

WORKDIR /app

# Copia os fontes e as bibliotecas externas
COPY src/ src/
COPY lib/ lib/

# Compila com o classpath apontando para os JARs em lib/
RUN mkdir out \
 && javac -cp "lib/*" -d out src/*.java \
 && jar --create --file app.jar --main-class=Main -C out .

# Executa com o classpath incluindo lib/ e app.jar
CMD ["java", "-cp", "lib/*:app.jar", "Main"]
