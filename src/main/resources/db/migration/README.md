# Migraciones Flyway

Aún no hay migraciones: el modelo de datos debe aprobarlo el equipo.

- Nombre: `V<numero>__descripcion.sql` (dos guiones bajos). Ejemplo: `V1__crear_usuario.sql`.
- Una migración ya integrada en `main` **no se edita**: Flyway guarda su huella y fallaría. Se crea otra.
