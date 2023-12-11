FROM eclipse-temurin:17 as builder

WORKDIR /app

COPY . .

RUN ./gradlew buildJAR --no-daemon

FROM kennarddh/mindustry-server-kotlin-runtime:1.0.5

WORKDIR /app

COPY --from=builder /app/build/libs/genesis-1.0.jar config/mods/genesis-1.0.jar

EXPOSE 6567