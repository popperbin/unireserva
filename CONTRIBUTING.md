# Cómo colaborar

Flujo propuesto, pendiente de aprobación del equipo.

## Flujo

1. Toma una tarea en Jira.
2. Crea una rama desde `main`: `tipo/CLAVE-descripcion`, por ejemplo `feat/UNI-12-crear-reserva` (la clave real depende del proyecto Jira).
3. Commits pequeños en formato `tipo: descripción` (`feat`, `fix`, `docs`, `test`, `chore`).
4. Antes de subir: `.\mvnw.cmd verify` en verde.
5. Abre un Pull Request. Otro integrante lo revisa; se integra con CI en verde. Sin push directo a `main`.

## Un cambio está terminado cuando

- Compila y las pruebas pasan.
- Tiene pruebas de la lógica nueva.
- Actualiza la documentación y diagramas afectados.
- Quien lo entrega puede explicarlo en la sustentación.

## Reglas

- Los contratos (interfaces entre módulos) y el modelo de datos se acuerdan antes de implementarlos.
- Una dependencia nueva se justifica en el Pull Request.
- Una decisión arquitectónica se registra como ADR en `docs/adr/`.
- Nunca subas `.env`, contraseñas ni tokens.
