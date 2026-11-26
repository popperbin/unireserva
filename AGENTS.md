# Instrucciones para asistentes de IA

Proyecto académico de tres estudiantes: deben poder entender y defender cada línea.

## Estado

Sprint 0 completado. **No implementar funcionalidad de negocio** hasta que el equipo apruebe contratos y diseño.

## Stack (preliminar)

Java 21, Spring Boot, Maven Wrapper, PostgreSQL, Spring JDBC (no JPA), Flyway, JUnit y Mockito. Aplicación de consola (sin web ni API REST). Monolito modular con arquitectura hexagonal.

## Verificación

`docker compose up -d --wait` y luego `.\mvnw.cmd verify`.

## Reglas

- El dominio no depende de Spring, JDBC ni consola. Los menús no acceden a datos ni contienen reglas, validaciones de negocio o permisos.
- Un módulo no accede a tablas ni clases internas de otro.
- No definir el modelo de datos ni añadir dependencias sin aprobación del equipo.
- No editar migraciones ya integradas en `main`.
- No inventar resultados: informar comandos ejecutados y su salida real.
- Sin secretos en el repositorio. Sin despliegues ni servicios de pago.
- El repositorio es público: no publicar datos de servidores, credenciales ni datos personales.
- No usar infraestructura ni credenciales ajenas a este proyecto.
- Explicar al equipo cada decisión.
