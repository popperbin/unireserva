# Instrucciones para asistentes de IA

Proyecto académico de dos estudiantes: deben poder entender y defender cada línea. El enunciado del profesor prevalece.

## Estado

Base técnica. **No implementar funcionalidad de negocio** hasta que el equipo acuerde el diseño de detalle (`docs/diseno-minimo.md`).

## Stack acordado

Java 21 puro, Maven Wrapper, JUnit, consola, hexagonal simplificada, persistencia simulada con `Map` y `List`, inyección por constructor, CQRS básico, GitHub Actions.

Sin frameworks de aplicación, bases de datos, Docker, web, API REST, servicios externos ni despliegue.

## Verificación

`.\mvnw.cmd verify`

## Reglas

- `domain` no depende de `application` ni de adaptadores. `application` no depende de adaptadores.
- La consola no accede a repositorios ni contiene reglas, validaciones de negocio o permisos.
- No crear clases, interfaces ni paquetes vacíos o sin necesidad.
- No añadir requisitos ajenos al enunciado ni dependencias sin acuerdo del equipo.
- No inventar resultados: informar comandos ejecutados y su salida real.
- El repositorio es público: sin secretos, datos de servidores ni datos personales.
- No usar infraestructura ni credenciales ajenas a este proyecto.
