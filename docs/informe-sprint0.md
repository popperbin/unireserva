# Informe de inicialización: Sprint 0

Fecha: 2026-11-26 · Para revisión de Mateo Avellaneda, Michel Diaz y Nicolain de Castro.
Preparado con asistencia de Claude Code.

## 1. Alcance

Solo inicialización técnica. No hay funcionalidades de negocio, tablas ni despliegue.

## 2. Versiones usadas

| Componente | Versión | Quién la fija |
|---|---|---|
| Java (Eclipse Temurin) | 21.0.13.1 local; "21" en CI | `pom.xml`, workflow |
| Maven | 3.10.2 | Maven Wrapper |
| Spring Boot | 4.2.0 | `pom.xml` |
| Spring Framework / Spring JDBC | 7.1.0 | Spring Boot |
| Tomcat embebido | 11.0.27 | Spring Boot |
| Thymeleaf | 3.1.7 | Spring Boot |
| Flyway | 12.11.0 | Spring Boot |
| Driver PostgreSQL | 42.7.14 | Spring Boot |
| PostgreSQL | 17.12 local; imagen `postgres:17-alpine` | `compose.yaml`, workflow |
| JUnit Jupiter / Mockito | 6.1.3 / 5.24.0 | Spring Boot |
| Docker / Compose | 29.4.3 / 5.1.3 | Equipo de desarrollo |
| Git / GitHub CLI | 2.46.0 / 2.102.0 | Equipo de desarrollo |

## 3. Decisiones y archivos explicados

| Archivo | Para qué sirve | Por qué así |
|---|---|---|
| `pom.xml` | Dependencias y compilación | Generado con Spring Initializr. Las versiones las hereda de Spring Boot para que sean compatibles entre sí |
| `mvnw`, `mvnw.cmd`, `.mvn/` | Ejecutar Maven sin instalarlo | Todos usan la misma versión |
| `UnireservaApplication.java` | Punto de arranque | `@SpringBootApplication` activa la configuración automática y la búsqueda de componentes |
| `*/package-info.java` | Declara cada módulo | Muestra la división sin inventar clases; Git no guarda carpetas vacías |
| `application.properties` | Conexión, Flyway, servidor | Lee `.env`; la contraseña no tiene valor por defecto; escucha solo en `127.0.0.1` |
| `db/migration/` | Futuras migraciones | Sin SQL: el modelo de datos no está aprobado |
| `UnireservaApplicationTests.java` | Prueba de arranque | Levanta la aplicación contra PostgreSQL real y comprueba que Flyway creó su tabla de historial |
| `compose.yaml` | PostgreSQL local | Puerto 5433 para no chocar con otro PostgreSQL en 5432; comprobación de salud; volumen persistente |
| `.env.example` / `.env` | Variables locales | Se versiona el ejemplo, nunca los valores |
| `.gitignore` | Excluye compilados, IDE y secretos | |
| `.gitattributes` | Fin de línea | `mvnw` necesita LF para ejecutarse en Linux |
| `.github/workflows/ci.yml` | CI en `main` y en cada Pull Request | Base de datos desechable y permisos de solo lectura |
| `README.md` | Cómo ejecutar | |
| `CONTRIBUTING.md` | Cómo colaborar | Propuesta |
| `AGENTS.md` | Límites para asistentes de IA | |
| `docs/` | Arquitectura, ADR, diagramas, pruebas | Estructura y pendientes; ningún ADR aprobado |

Dependencias: web (Spring MVC), Thymeleaf, JDBC, Flyway (con su módulo PostgreSQL), driver PostgreSQL y `spring-boot-starter-test`.

Los cuatro paquetes de prueba por módulo que propone Initializr se reemplazaron por `spring-boot-starter-test`.

No incluido: Bootstrap (se añade con la primera vista), Spring Security, JPA, Lombok, Testcontainers, ArchUnit.

## 4. Verificaciones (equipo local, Windows 11)

| Verificación | Comando | Resultado |
|---|---|---|
| Configuración Compose | `docker compose config -q` | Código 0 |
| Base de datos | `docker compose up -d --wait` | Contenedor `healthy`, PostgreSQL 17.12 |
| Compilación, prueba y empaquetado | `.\mvnw.cmd -B -V clean verify` | `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` · `BUILD SUCCESS` · 47,9 s |
| Flyway | (dentro de la prueba) | 0 migraciones; creó `flyway_schema_history` |
| Ejecución del JAR | `java -jar target\unireserva-0.0.1-SNAPSHOT.jar` | Arranca en `127.0.0.1:8080`; `GET /` → 404 esperado |
| Ejecución con Maven | `.\mvnw.cmd -B spring-boot:run` | Arranca; `GET /` → 404 esperado |

La CI se verifica en la pestaña *Actions* del repositorio.

Se usó un JDK 21 portátil temporal: **el equipo de Mateo no tiene JDK 21 instalado** y `JAVA_HOME` apunta a Java 8.

### Avisos observados (no impiden el resultado)

- Thymeleaf: no encuentra `classpath:/templates/`; desaparece con la primera plantilla.
- Flyway: `No migrations found`; esperado.
- Maven: error 401 al consultar dos repositorios externos declarados por dependencias de Flyway; los artefactos se descargaron de Maven Central.
- Mockito: carga un agente de forma dinámica; versiones futuras del JDK lo restringirán.

### No verificado

- Puesta en marcha en los equipos de Michel y Nicolain, ni en Linux/macOS (salvo la CI en Ubuntu).
- Instalación desde un clon limpio sin caché de Maven.

## 5. Decisiones pendientes del equipo

1. Aprobar el stack, incluida la versión Spring Boot 4.2.0 (rama reciente: muchos tutoriales son de 3.x).
2. Aprobar arquitectura hexagonal y monolito modular (ADR).
3. Contratos entre módulos y comunicación entre ellos.
4. Modelo de datos y primera migración.
5. Estrategia contra reservas superpuestas (aplicación, restricción en PostgreSQL o ambas) y zona horaria.
6. Cómo identificar al usuario y controlar roles (no se exige autenticación con contraseña).
7. Dónde viven `HistorialReserva` y los reportes sin romper límites entre módulos.
8. Dos patrones GoF de categorías diferentes.
9. Cuatro atributos de calidad.
10. Nombres: paquete base `com.unireserva` y módulos en español.
11. Bootstrap: CDN o WebJar.
12. Pruebas: Testcontainers y ArchUnit, sí o no.
13. Invitar a Michel y Nicolain al repositorio (faltan sus usuarios de GitHub).
14. Protección de `main`: en repositorios privados puede requerir un plan de pago de GitHub.
15. Proyecto Jira, clave de tareas y carpeta de Drive.
16. Confirmar que el docente acepta esta entrega por etapas y el uso de asistentes de IA.
