@echo off
cd %~dp0
set CLASSPATH=lib\gson-2.10.1.jar;lib\mysql-connector-java-8.0.30.jar;lib\protobuf-java-3.19.4.jar

echo Compiling Java files...
javac -cp %CLASSPATH% src\Conexao.java src\Confirmados.java src\DadosApi.java src\InsertApi.java src\Main.java src\RetornoAdvogado.java src\RetornoAutor.java src\RetornoDocIniciais.java src\RetornoListDocument.java src\RetornoMovimento.java src\RetornoOutrosEnvil.java src\RetornoReu.java


if errorlevel 1 (
    echo Compilation failed. Exiting...
    pause
    exit /b 1
)

echo Running the application...
java -cp src;%CLASSPATH% Main

pause
