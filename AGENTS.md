# Instrucciones para asistentes de IA

Proyecto académico de tres estudiantes: deben poder entender y defender cada línea.

## Estado

Sprint 0 completado. **No implementar funcionalidad de negocio** hasta que el equipo apruebe contratos y diseño.

## Stack (preliminar)

Java 21, Spring Boot, Maven Wrapper, PostgreSQL, Spring JDBC (no JPA), Flyway, Thymeleaf, Bootstrap (aún sin incorporar), JUnit y Mockito. Monolito modular con arquitectura hexagonal.

## Verificación

`docker compose up -d --wait` y luego `.\mvnw.cmd verify`.

## Reglas

- El dominio no depende de Spring, JDBC ni web. Los controladores no acceden a datos.
- Un módulo no accede a tablas ni clases internas de otro.
- No definir el modelo de datos ni añadir dependencias sin aprobación del equipo.
- No editar migraciones ya integradas en `main`.
- No inventar resultados: informar comandos ejecutados y su salida real.
- Sin secretos en el repositorio. Sin despliegues ni servicios de pago.
- No usar infraestructura ni credenciales ajenas a este proyecto.
- Explicar al equipo cada decisión.
