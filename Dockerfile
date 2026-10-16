# Etapa 1: compilar e empacotar a aplicação com Java 17
FROM eclipse-temurin:17-jdk AS build

WORKDIR /projeto

# Copia os arquivos necessários para utilizar o Gradle Wrapper
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./

# Copia o código da aplicação e dos testes
COPY src ./src

# Ajusta as quebras de linha do Windows e gera a distribuição
RUN sed -i 's/\r$//' gradlew \
    && bash ./gradlew clean test installDist --no-daemon

# Etapa 2: executar a aplicação com Java 17
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia somente a aplicação empacotada e suas dependências
COPY --from=build /projeto/build/install/credito-api/lib/ ./lib/

# Permite receber requisições externas ao contêiner
ENV HOST=0.0.0.0

# Porta padrão; o Render poderá fornecer outro valor por PORT
ENV PORT=8080

# Executa sem privilégios de administrador
USER 10001:10001

EXPOSE 8080

# Inicializa a API
ENTRYPOINT ["java", "-cp", "/app/lib/*", "credito.CreditoApi"]