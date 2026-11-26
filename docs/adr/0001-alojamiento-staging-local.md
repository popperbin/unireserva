# ADR 0001: Alojamiento de staging en un equipo local con acceso por Tailscale

## Estado
Propuesto (2026-11-26). Pendiente de aprobación del propietario del equipo y del grupo.

## Contexto
El equipo necesita mostrar el prototipo entre tres personas sin costo. Se descartó usar un servidor compartido con otros servicios. Hay un portátil Windows que suele estar encendido, con otros usos y con un historial de apagados inesperados. El curso exige un prototipo funcional, no un alojamiento.

## Decisión
Ejecutar staging en ese portátil con Docker Compose: aplicación y PostgreSQL en redes internas, sin puertos publicados. El acceso remoto lo da un contenedor Tailscale en una tailnet exclusiva del proyecto, compartiendo únicamente ese nodo con los compañeros. El despliegue es manual, desde un commit de `main` con CI aprobada.

## Alternativas consideradas

| Alternativa | Por qué no |
|---|---|
| Servidor existente compartido | Infraestructura ajena al proyecto |
| Solo desarrollo local de cada integrante | Más simple y suficiente para el curso; no permite revisar una misma instancia. Es la opción de respaldo |
| Tailscale instalado en Windows e invitar a la tailnet existente | Expondría el equipo y otras máquinas; depende de reglas más delicadas |
| Abrir puertos del router o Tailscale Funnel | Expone a Internet una aplicación sin control de acceso |
| Ejecutor propio de GitHub Actions | En un repositorio público podría ejecutar código ajeno en el equipo |
| Nube gratuita | Límites, posibles cobros y cuentas adicionales |

## Consecuencias positivas
- Costo adicional previsto: cero (plan Personal de Tailscale, de uso no comercial).
- Los compañeros ven una sola máquina y un puerto.
- Reproducible y reversible: todo está en archivos versionados.

## Consecuencias negativas
- Sin garantía de disponibilidad.
- Aislamiento de contenedores, no de máquina.
- El propietario opera, actualiza y respalda.
- Depende de un tercero (Tailscale) y sus condiciones.
- El nombre del equipo en la tailnet aparece en registros públicos de certificados.
- La aplicación no puede usar servicios externos sin cambiar las redes.

## Revisar si
La aplicación necesita salida a Internet, se usan datos reales, o la disponibilidad pasa a ser un requisito.
