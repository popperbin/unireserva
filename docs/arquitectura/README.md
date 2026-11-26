# Arquitectura (propuesta preliminar, sin aprobar)

Un solo desplegable (**monolito modular**) dividido por capacidad de negocio. Cada módulo seguiría la **arquitectura hexagonal**:

```text
com.unireserva.<modulo>/
  domain/              reglas de negocio en Java puro
  application/         casos de uso (comandos y consultas) y puertos
  adapter/in/web/      controladores y vistas
  adapter/out/jdbc/    implementación de repositorios
```

Dependencias hacia adentro: `adapter → application → domain`. Todavía ninguna herramienta lo comprueba automáticamente.

Las subcarpetas se crean cuando exista la primera clase que las necesite. Hoy cada módulo solo tiene `package-info.java`, porque Git no guarda carpetas vacías.

Pendiente aquí: contratos entre módulos, atributos de calidad, vistas 4+1, arquitectura empresarial y TOGAF (el texto académico puede vivir en Drive).
