# Informe: simplificación a aplicación de consola

Fecha: 2026-11-26. Preparado con asistencia de Claude Code. Decisión propuesta: [ADR 0002](adr/0002-aplicacion-de-consola.md).

## Cambios

| Antes | Ahora |
|---|---|
| `spring-boot-starter-webmvc` y `spring-boot-starter-thymeleaf` | Retirados |
| Servidor en `127.0.0.1:8080` | `spring.main.web-application-type=none` |
| Sin interfaz | `consola/MenuConsola` (solo salir) |
| 1 prueba | 5 pruebas |
| CI: compilar y probar | Además arranca el JAR y elige salir |

No hay lógica de negocio, entidades ni migraciones.

## Verificaciones locales (Windows, JDK 21.0.13.1, PostgreSQL 17 en base nueva y aislada)

| Prueba | Resultado |
|---|---|
| `.\mvnw.cmd -B clean verify` | `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` · `BUILD SUCCESS` |
| Dependencias web, Thymeleaf o Tomcat | 0 entre las resueltas |
| JAR con opción inválida y `0` | Rechaza la primera y sale con código 0 |
| JAR con la entrada cerrada | Sale con código 0 |
| `spring-boot:run` enviando `0` | Sale con código 0 |
| JAR esperando teclado | Sigue activo a los 8 s, 0 puertos en escucha; termina al escribir `0` |
| Flyway | Existe `flyway_schema_history` |
| Base detenida | Código 1 con `Connection ... refused` |
| Pruebas automatizadas | El menú no se activa en el contexto de prueba |

Las filas de opción inválida, entrada cerrada, Flyway y base detenida se ejecutaron antes de añadir el manejo de la marca BOM; el resto se repitió después.

## Defecto encontrado y corregido

Una terminal de PowerShell antepuso una marca invisible (BOM) al texto enviado por tubería y el menú no reconocía el primer `0`. El menú la ignora y hay una prueba.

## Pull Requests anteriores

| PR | Tratamiento propuesto |
|---|---|
| #1 | Se incorporan aquí la guía, el script de JDK 21, la regla de repositorio público y el informe recortado. Se descarta la propuesta de staging en servidor. Cerrarlo sin fusionar |
| #2 | Se mantiene abierto, sin fusionar, como referencia opcional. Está diseñado para una aplicación web: no sirve tal cual para la consola |

## Sin verificar

- Escritura de tildes y eñes en la consola de Windows.
- Ejecución en los equipos de Michel y Nicolain y en macOS.
- Opciones escritas a mano en una terminal real (las pruebas envían la entrada por programa).

## Siguiente paso

Diseño funcional y arquitectónico conjunto. Incluye decidir cómo se identifica el usuario en consola, contratos, modelo de datos y patrones.
