# ADR 0002: UniReserva como aplicación de consola

## Estado
Propuesto (2026-11-26). Pendiente de aprobación del equipo.

El número 0001 está reservado por la propuesta de alojamiento, que no se ha integrado.

## Contexto
El Sprint 0 preparó una base web. El profesor aclaró que no requiere ni evaluará interfaz web o gráfica: prioriza backend, arquitectura, patrones y comportamiento desde consola. El enunciado admite aplicaciones de consola.

## Decisión
- Aplicación Java de consola con Spring Boot en modo no web.
- Se retiran Spring MVC, Thymeleaf y el servidor embebido; Bootstrap no se incorpora.
- Entrada por un `CommandLineRunner` (`MenuConsola`) activo solo con el perfil `consola`, que `main()` habilita. Las pruebas no pasan por `main()` y por eso no se quedan esperando el teclado.
- Se conservan arquitectura hexagonal, monolito modular, Spring JDBC, PostgreSQL, Flyway e inyección de dependencias.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| Mantener la web | Trabajo y dependencias que no se evalúan |
| Java sin Spring | Habría que resolver a mano inyección, transacciones, conexiones y migraciones |
| Spring Shell u otra librería de línea de comandos | Dependencia nueva; un menú con `BufferedReader` basta |
| Una propiedad para apagar el menú en cada prueba | Una prueba que la olvide quedaría bloqueada |
| Persistencia en memoria | El curso la admite, pero la base ya está preparada y permite demostrar reglas e historial |

## Consecuencias positivas
- Menos dependencias y ningún puerto abierto por la aplicación.
- El esfuerzo se concentra en dominio, casos de uso y patrones.
- La arquitectura hexagonal permite añadir otro adaptador de entrada sin cambiar el núcleo.

## Consecuencias negativas
- Los menús de consola son menos cómodos y hay que cuidar la lectura de datos.
- Escribir tildes o eñes requiere comprobar la codificación en Windows; por ahora los textos no las usan.
- El control por roles debe demostrarse sin sesión web: falta decidir cómo se identifica el usuario.
- PostgreSQL sigue exigiendo Docker para la demostración.
