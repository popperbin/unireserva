# Informe técnico: cierre de infraestructura del Sprint 0

Fecha: 2026-11-26. Preparado con asistencia de Claude Code, para revisión del equipo.

> Resultados del momento en que se midieron, antes de convertir el proyecto en aplicación de consola ([ADR 0002](adr/0002-aplicacion-de-consola.md)). Las propuestas de alojamiento quedaron fuera del alcance y se retiraron.

## 1. Resumen

| Encargo | Estado |
|---|---|
| JDK 21 sin afectar Java 8 | Hecho en el equipo de Mateo |
| Clon limpio | Verificado en Windows |
| Guía de incorporación | [incorporacion.md](incorporacion.md); sin probar por sus destinatarios |
| Protección de `main` | Revisada; disponible, **no activada** |
| Repositorio público | Hecho por solicitud de Mateo |

## 2. Java

- JDK Temurin 21.0.13.1 (suma SHA-256 verificada contra la API de Adoptium) en `%USERPROFILE%\.jdks\temurin-21`.
- No se modificaron `JAVA_HOME` ni `PATH` del sistema: siguen apuntando a Java 8.
- `scripts\usar-jdk21.ps1` activa Java 21 solo en la terminal actual.

## 3. Clon limpio

Commit `f960187`, clonado sin credenciales en una carpeta nueva, con caché de Maven vacía y base de datos independiente (`COMPOSE_PROJECT_NAME=unireserva-clon`, puerto 5434).

| Paso | Resultado |
|---|---|
| `git clone` | Código 0; sin `.env` |
| `.\scripts\usar-jdk21.ps1` | `JAVA_HOME` pasó de Java 8 a JDK 21 |
| `.\mvnw.cmd -v` | Maven 3.10.2, Java 21.0.13.1 |
| `docker compose up -d --wait` | Código 0 |
| `.\mvnw.cmd -B clean verify` | `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` · `BUILD SUCCESS` · 55 s |
| `.\mvnw.cmd spring-boot:run` | `GET /` → 404 esperado |
| `docker compose down -v` (solo la base de prueba) | Código 0; `git status` limpio |

Límites: mismo equipo; la imagen de PostgreSQL ya estaba descargada; la política de ejecución de PowerShell era `Bypass`, así que el posible bloqueo del script no se probó.

## 4. Repositorio público

Antes del cambio se revisó el historial (dos commits): no se encontraron secretos, la contraseña local no aparece y no había Pull Requests, issues ni secretos de Actions.

Consecuencias:

- Cualquiera puede leer y copiar el código, incluidos otros grupos del curso.
- Quedan visibles los nombres de los integrantes y el correo del autor de los commits.
- No hay licencia: se conservan los derechos, pero la copia técnica es posible.
- Volver a privado no recupera copias ya hechas.
- No se debe publicar información de servidores, credenciales ni datos personales.

## 5. Protección de `main`

Con GitHub Free, la API rechazaba la protección en el repositorio privado (HTTP 403). Ya público, la función está disponible sin costo y hoy **no hay reglas**.

| Opción | Costo | Nota |
|---|---|---|
| Reglas en repositorio público | Gratis | Recomendada |
| Repositorio privado con GitHub Pro mediante [Student Developer Pack](https://education.github.com/pack) | Gratis para estudiantes verificados | Alternativa si se vuelve a privado; confirmar condiciones vigentes |
| Hook `pre-push` local | Gratis | Se puede saltar; no es protección real |

Configuración propuesta: exigir Pull Request con una aprobación, exigir el check `verificar`, bloquear *force push* y borrado. Activarla **después** de que Michel y Nicolain acepten la invitación; antes, nadie podría aprobar los cambios de Mateo.

También aparecen desactivados *secret scanning* y *push protection*; se recomienda activarlos.

## 6. Riesgos

| Riesgo | Mitigación |
|---|---|
| Plagio al ser público | Decidir si se mantiene público; consultar al docente |
| `main` sin protección | Activar reglas tras las invitaciones |
| Secreto subido por error | *Push protection*; `.env` ignorado |
| Java 8 por defecto en el equipo de Mateo | Ejecutar el script en cada terminal |
| Dos clones comparten la base local | Documentado en la guía |
| Spring Boot 4.2.0 reciente | Decidir en el ADR de stack |

## 7. Pendiente antes del Sprint 1

Aprobación conjunta de decisiones arquitectónicas, contratos y modelo de datos (lista en el [informe del Sprint 0](informe-sprint0.md)). Su punto 14 queda reemplazado por la sección 5.
