# UniReserva

Prototipo académico para gestionar recursos y reservas universitarias (Arquitectura de Software Empresarial).

**Estado: Sprint 0 técnico.** Hay un esqueleto que compila, arranca y se prueba. No hay funcionalidades de negocio.

| Integrante | Área |
|---|---|
| Mateo Avellaneda | Arquitectura, reservas e integración |
| Michel Diaz | Arquitectura de datos, persistencia, usuarios y recursos |
| Nicolain de Castro | Interfaz, patrones y arquitectura empresarial |

## Requisitos

- Git.
- JDK 21 (`.\mvnw.cmd -v` debe mostrar Java 21; usa `JAVA_HOME`). En Windows: `winget install EclipseAdoptium.Temurin.21.JDK`.
- Docker Desktop en ejecución.
- Maven **no** hace falta: `mvnw` descarga la versión fijada (necesita internet la primera vez).

## Puesta en marcha

Todos los comandos se ejecutan desde la raíz del repositorio (PowerShell).

```powershell
Copy-Item .env.example .env      # luego escribe una contraseña en POSTGRES_PASSWORD
docker compose up -d --wait      # PostgreSQL en 127.0.0.1:5433
.\mvnw.cmd verify                # compila, prueba y empaqueta
.\mvnw.cmd spring-boot:run       # http://127.0.0.1:8080  (Ctrl+C para detener)
docker compose down              # detiene la base; conserva los datos
```

En Linux/macOS: `cp .env.example .env` y `./mvnw` en lugar de `.\mvnw.cmd`.

Abrir <http://127.0.0.1:8080> devuelve **404**: es lo esperado, todavía no hay páginas.

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Could not resolve placeholder 'POSTGRES_PASSWORD'` o fallo de autenticación | Falta `.env` o la contraseña está vacía |
| `Connection refused` en las pruebas | La base no está levantada: `docker compose up -d --wait` |
| `release version 21 not supported` | `JAVA_HOME` apunta a otro Java |
| Puerto 5433 ocupado | Cambia `POSTGRES_PORT` en `.env` |
| Cambiaste la contraseña y ya no conecta | PostgreSQL la fija al crear el volumen. `docker compose down -v` lo recrea y **borra los datos locales** |

## Estructura

```text
.github/workflows/ci.yml   Integración continua
.mvn/, mvnw, mvnw.cmd      Maven Wrapper
src/main/java/com/unireserva/
  usuarios/ recursos/ reservas/ notificaciones/ reportes/
src/main/resources/        Configuración y migraciones Flyway
src/test/java/             Pruebas
docs/                      arquitectura/ adr/ diagramas/ pruebas/ informe-sprint0.md
compose.yaml               PostgreSQL local
pom.xml                    Dependencias y compilación
```

Colaboración: [CONTRIBUTING.md](CONTRIBUTING.md) · Asistentes de IA: [AGENTS.md](AGENTS.md)

Gestión en Jira, documentación académica en Google Drive, código en GitHub.
