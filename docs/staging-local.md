# Propuesta técnica: staging local

Estado: **propuesto, sin desplegar.** Sustituye la idea de alojar UniReserva en un servidor.

Relacionados: [ADR 0001](adr/0001-alojamiento-staging-local.md) · [diagrama](diagramas/despliegue-staging.md) · [acceso](acceso-staging.md) · [recuperación](recuperacion.md) · [informe](informe-staging.md)

## Archivos

| Archivo | Función | Motivo |
|---|---|---|
| `Dockerfile` | Compila con JDK 21 y ejecuta con un JRE Alpine | Imagen proporcionada al proyecto, sin compilador y con usuario `10001` |
| `.dockerignore` | Lista de lo único que entra a la construcción | `.env` nunca llega a la imagen |
| `compose.staging.yaml` | Base, aplicación y acceso | Separado del `compose.yaml` de desarrollo |
| `deploy/tailscale/serve.json` | Publica la web por HTTPS dentro de la tailnet | |
| `deploy/tailscale/politica-ejemplo.hujson` | Solo puerto 443 | |
| `scripts/staging.ps1` | Desplegar, respaldar, restaurar, estado y detener | Un solo punto de operación |
| `.github/workflows/ci.yml` | Además valida el Compose y construye la imagen | Detectar roturas antes de integrar. No publica ni despliega |

Las imágenes base se fijan por resumen (digest). Actualizarlas es un cambio revisado.

## Separación entre desarrollo y staging

| | Desarrollo | Staging |
|---|---|---|
| Proyecto Compose | `unireserva` | `unireserva-staging` |
| Archivo | `compose.yaml` | `compose.staging.yaml` |
| Base y volumen | Propios, puerto 5433 local | Propios, sin puertos publicados |
| Credenciales | `.env` en el clon | `%LOCALAPPDATA%\UniReserva\unireserva-staging\staging.env`, generadas |
| Código | Copia de trabajo | Clon limpio del commit aprobado |
| Ejecución | Maven | Contenedor |

Es separación lógica: comparten equipo y motor de Docker.

## Límites

| Servicio | Memoria | CPU | Redes |
|---|---|---|---|
| db | 256 MiB | 0,5 | `datos` (interna) |
| app | 512 MiB | 1,0 | `datos`, `web` (internas) |
| tailscale | 128 MiB | 0,25 | `web`, `salida` |

La aplicación y la base no tienen salida a Internet ni al equipo anfitrión. Si una función futura necesita llamar a un servicio externo, habrá que revisar esta decisión. Registros: 3 archivos de 10 MB por servicio.

## Despliegue

Desde un clon de `main` revisado por el propietario, con Docker Desktop abierto:

```powershell
git switch main; git pull
.\scripts\staging.ps1 desplegar -Commit <sha completo> -ConAcceso
.\scripts\staging.ps1 estado
```

El script exige que el commit pertenezca a `main` y tenga el check `verificar` aprobado, lo clona aparte, construye, hace copia previa, levanta y, si falla, vuelve a la versión anterior. Sin `-ConAcceso` no inicia Tailscale. No hay despliegue automático ni ejecutores propios: ningún Pull Request se ejecuta en el equipo.

"Aprobado" depende hoy de revisar antes de integrar: conviene proteger `main`.

## Salud y disponibilidad

- Docker comprueba la base (`pg_isready`) y que la aplicación abra su puerto.
- `staging.ps1 estado` avisa si un servicio no está sano, si la copia supera 26 horas o quedan menos de 20 GB.
- Limitación: el chequeo de la aplicación no detecta que la base falle después del arranque desde la propia aplicación. Spring Boot Actuator lo permitiría; añadirlo es una decisión pendiente.
- La prueba real de disponibilidad es abrir la web desde otro dispositivo.

## Reinicios

Los servicios usan `restart: unless-stopped`: vuelven cuando Docker Desktop arranca, lo que requiere iniciar sesión en Windows. Tras `detener`, hay que desplegar de nuevo el mismo commit.

## Cambios en el equipo que requieren aprobación

Ninguno se aplicó: crear la tailnet y la clave; programar la copia diaria; ajustar horas activas, energía o límite de carga; diagnosticar los apagados.

## Antes de activar

1. Aprobar este diseño e integrar el Pull Request.
2. Resolver o aceptar por escrito los apagados inesperados.
3. Seguir la [guía de acceso](acceso-staging.md) y completar sus comprobaciones.
