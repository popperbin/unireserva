# Copias y recuperación de staging

Todo se guarda en `%LOCALAPPDATA%\UniReserva\unireserva-staging\`: credenciales, versiones y `copias\`. Comandos desde la raíz del repositorio.

## Copias

```powershell
.\scripts\staging.ps1 respaldar
```

- Formato comprimido de `pg_dump`, comprobado con `pg_restore --list`.
- Se hace además antes de cada despliegue y de cada restauración.
- Se conservan las 14 más recientes.
- Están en el mismo disco: copia alguna a otro medio. Si algún día hay datos reales, no subirlas sin cifrar.

Pérdida posible: lo cambiado desde la última copia.

### Copia diaria (requiere aprobación; no está creada)

```powershell
$script = (Resolve-Path .\scripts\staging.ps1).Path
$accion = New-ScheduledTaskAction -Execute powershell.exe -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$script`" respaldar"
$hora = New-ScheduledTaskTrigger -Daily -At 20:00
$ajustes = New-ScheduledTaskSettingsSet -StartWhenAvailable
Register-ScheduledTask -TaskName 'UniReserva copia' -Action $accion -Trigger $hora -Settings $ajustes
```

La ruta debe ser un clon estable de `main`.

## Restaurar

```powershell
.\scripts\staging.ps1 restaurar -Archivo <ruta de la copia> -Confirmar
```

Carga la copia en una base temporal; solo si termina bien detiene la aplicación y la sustituye. Antes guarda una copia del estado actual.

Después: `.\scripts\staging.ps1 estado`.

## Casos

| Situación | Qué hacer |
|---|---|
| Reinicio o apagado | Iniciar sesión, abrir Docker Desktop y ejecutar `estado` |
| Despliegue fallido | El script vuelve a la versión anterior. Si la nueva alcanzó a migrar y la anterior falla, restaurar la copia previa |
| Datos erróneos | `restaurar` |
| Volumen perdido | `desplegar` el commit y luego `restaurar` |
| Equipo nuevo | Instalar Git y Docker Desktop, clonar, `desplegar`, copiar el `.dump`, `restaurar`. El acceso requiere una clave nueva |
| Fallo entre eliminar y renombrar la base | Los datos están en `unireserva_nueva` y en la copia previa: repetir `restaurar` con esa copia |
| Se perdió `staging.env` pero existe el volumen | No borrar el volumen. Definir una clave nueva en el archivo y aplicarla en la base (ver abajo) |

### Restablecer la clave

Tras `desplegar` (que genera el archivo y fallará al autenticar), abrir la consola de la base:

```powershell
docker exec -it unireserva-staging-db-1 psql -U unireserva_staging -d unireserva
```

Dentro: `\password unireserva_staging`, escribir la clave del archivo y volver a desplegar.

## Ensayo

Probado en un proyecto desechable: copia, restauración, archivo dañado y migración rota. **No probados:** volumen perdido, equipo nuevo y restablecimiento de clave. Repetir un ensayo antes de la sustentación.
