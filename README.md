# UniReserva

Prototipo académico para gestionar recursos y reservas universitarias (Arquitectura de Software Empresarial). Es una **aplicación Java de consola**: sin interfaz web ni API REST.

**Estado: preparación técnica.** Hay un esqueleto que compila, arranca y se prueba. No hay funcionalidades de negocio.

| Integrante | Área |
|---|---|
| Mateo Avellaneda | Arquitectura, reservas e integración |
| Michel Diaz | Arquitectura de datos, persistencia, usuarios y recursos |
| Nicolain de Castro | Interfaz, patrones y arquitectura empresarial |

¿Primera vez? Sigue la [guía de incorporación](docs/incorporacion.md).

## Requisitos

- Git.
- JDK 21 (`.\mvnw.cmd -v` debe mostrar Java 21; usa `JAVA_HOME`). En Windows: `winget install EclipseAdoptium.Temurin.21.JDK`.
- Docker Desktop en ejecución.
- Maven **no** hace falta: `mvnw` descarga la versión fijada (necesita internet la primera vez).

## Puesta en marcha

Todos los comandos se ejecutan desde la raíz del repositorio (PowerShell).

```powershell
.\scripts\usar-jdk21.ps1         # activa Java 21 en esta terminal
Copy-Item .env.example .env      # luego escribe una contraseña en POSTGRES_PASSWORD
docker compose up -d --wait      # PostgreSQL en 127.0.0.1:5433
.\mvnw.cmd verify                # compila, prueba y empaqueta
java -jar target\unireserva-0.0.1-SNAPSHOT.jar   # abre el menú; 0 para salir
docker compose down              # detiene la base; conserva los datos
```

En Linux/macOS no hace falta el script: usa un JDK 21, `cp .env.example .env`, `./mvnw` y `/` en la ruta del JAR.

Salida esperada (antes puede aparecer el aviso de Flyway de que no hay migraciones; es normal por ahora):

```text
UniReserva - aun no hay operaciones disponibles.
0. Salir
>
```

Ejecuta el JAR desde la raíz del repositorio: ahí busca el archivo `.env`. También funciona `.\mvnw.cmd spring-boot:run`. Para ver más registros, añade al final `--logging.level.root=info`.

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Could not resolve placeholder 'POSTGRES_PASSWORD'` o fallo de autenticación | Falta `.env` o la contraseña está vacía |
| `Connection refused` | La base no está levantada: `docker compose up -d --wait` |
| `release version 21 not supported` o `UnsupportedClassVersionError` | Estás usando otro Java: `.\scripts\usar-jdk21.ps1` |
| Puerto 5433 ocupado | Cambia `POSTGRES_PORT` en `.env` |
| Cambiaste la contraseña y ya no conecta | PostgreSQL la fija al crear el volumen. `docker compose down -v` lo recrea y **borra los datos locales** |

## Estructura

```text
.github/workflows/ci.yml   Integración continua
.mvn/, mvnw, mvnw.cmd      Maven Wrapper
src/main/java/com/unireserva/
  consola/                 Menú principal (adaptador de entrada)
  usuarios/ recursos/ reservas/ notificaciones/ reportes/
src/main/resources/        Configuración y migraciones Flyway
src/test/java/             Pruebas
docs/                      arquitectura/ adr/ diagramas/ pruebas/ e informes
scripts/                   Activar JDK 21
compose.yaml               PostgreSQL local
pom.xml                    Dependencias y compilación
```

Colaboración: [CONTRIBUTING.md](CONTRIBUTING.md) · Asistentes de IA: [AGENTS.md](AGENTS.md)

Gestión en Jira, documentación académica en Google Drive, código en GitHub.
