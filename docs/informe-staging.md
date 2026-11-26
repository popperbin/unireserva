# Informe de capacidad, riesgos y verificaciones del staging local

Fecha: 2026-11-26. Preparado con asistencia de Claude Code. El detalle del equipo se conserva en privado.

## Conclusión

Hay capacidad suficiente para UniReserva, pero **el portátil hoy no cumple como servidor 24/7**: su historial muestra 17 apagados inesperados en los últimos 30 días, de causa no determinada. Tratarlo como staging disponible cuando esté encendido, no como servicio garantizado.

## Capacidad

| Recurso | Disponible (aprox.) | Presupuesto de staging |
|---|---|---|
| CPU | 12 hilos | Límites: 1,75 |
| Memoria | 24 GB; unos 4 a 5 libres al medir | Límites: 896 MiB (app 512, base 256, acceso 128) |
| Memoria medida en prueba | | app 128 MiB, base 63 MiB |
| Disco | Unos 50 GB libres, cerca del 10 % | Imagen de la app 344 MB, más imágenes base, caché, datos y registros |

## Riesgos de un portátil como servidor

| Riesgo | Efecto | Mitigación | Estado |
|---|---|---|---|
| Apagados inesperados | Sin servicio | Diagnosticar antes de depender de él | Abierto |
| Docker Desktop requiere iniciar sesión | Tras reiniciar no hay servicio | Iniciar sesión; no activar inicio automático de sesión | Aceptar |
| Reinicios por actualizaciones | Interrupciones | Ajustar horas activas | Requiere aprobación |
| Calor y desgaste de batería | Daño del equipo | Superficie ventilada, límite de carga del fabricante | Requiere aprobación |
| Disco con poco margen | Fallo de base o compilación | `estado` avisa bajo 20 GB | Vigilar |
| Equipo y motor Docker compartidos con otros usos | Un fallo afecta a todos | Redes internas y límites; no es aislamiento de máquina | Residual |
| Contenedor de acceso con salida a Internet | Si se compromete, podría alcanzar servicios del anfitrión | Imagen fijada, política que solo permite 443 | Residual |
| Robo o daño | Pérdida de datos y copias | Solo datos ficticios; copiar respaldos fuera | Abierto |
| Corte de luz o Internet | Sin acceso | Demostrar en local el día de la sustentación | Aceptar |
| `main` sin protección | Código sin revisar podría considerarse aprobado | Activar protección; el propietario elige el commit | Abierto |

## Verificaciones efectuadas

Ejecutadas en el portátil, en un proyecto desechable (`unireserva-staging-prueba`) que luego se eliminó. **No se desplegó el staging definitivo ni se inició Tailscale.**

### Controles de aprobación (script real)

| Entrada | Resultado |
|---|---|
| `-Commit abc` | Rechazado: exige SHA completo |
| Commit de una rama no integrada | Rechazado: no pertenece a `main` |
| Commit de `main` anterior a esta propuesta | Pasó `main` y CI; rechazado por no tener configuración de staging |
| `restaurar` sin `-Confirmar` | Rechazado |

### Ciclo completo

Para desplegar antes de integrar se usó una copia del script que clona un repositorio local y **omite la consulta de CI**. El resto es idéntico. Commit probado: `8fdf5ee`.

| Prueba | Resultado |
|---|---|
| Construcción de la imagen y `up --wait` | Base y aplicación `healthy` |
| Puertos publicados | Ninguno |
| Usuario de la aplicación | `uid=10001`, sistema de archivos de solo lectura, `/tmp` escribible |
| HTTP dentro del contenedor | `GET /` → 404 esperado |
| Límites aplicados | app 512 MiB, 1 CPU, 200 procesos; base 256 MiB, 0,5 CPU, 100 procesos |
| `estado` sin copias | Advierte y sale con código 1 |
| `respaldar` | Copia verificada con `pg_restore --list` |
| `restaurar` tras alterar un dato | Dato original recuperado |
| `restaurar` un archivo dañado | Falla con código 1; datos intactos y aplicación sana |
| Despliegue de una migración rota | Copia previa, fallo detectado, vuelta automática a la versión anterior (59 s); dato intacto |
| `detener` | Código 0; sin contenedores en ejecución del proyecto |

### Aislamiento de red

Conexiones TCP con `nc` (4 s de espera).

| Destino | Contenedor normal | app | base | Red `web` |
|---|---|---|---|---|
| Servicios del equipo anfitrión (tres puertos) | Alcanzables | No | No | No |
| Internet | Alcanzable | No | No | No |
| Base de datos | No | Sí | Sí | No |
| Aplicación | No | Sí | Sí | Sí |

## Defecto encontrado y corregido

El primer intento falló con `Filename too long`: las rutas largas de Windows impedían clonar. El script ahora usa una carpeta corta por versión y `core.longpaths`.

## Sin verificar

- Tailscale: arranque del nodo, `serve.json` hacia `app:8080`, certificado, política e invitaciones.
- Consulta de CI sobre un commit que sí contenga staging (posible tras integrar).
- Recuperación tras reiniciar el equipo y comportamiento durante la espera.
- Pérdida completa del volumen y restauración en otro equipo.
- Reglas del cortafuegos de Windows.
