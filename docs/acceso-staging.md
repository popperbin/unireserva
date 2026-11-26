# Acceso a staging

Estado: **no activado.** Nada de esto funciona hasta que el propietario lo apruebe y lo configure.

No publiques la dirección de staging, enlaces de invitación ni claves en GitHub, Jira o documentos compartidos del curso.

## Para Michel y Nicolain

1. Crea **tu propia** cuenta de Tailscale e instala la aplicación en tu equipo.
2. Pide a Mateo tu invitación; llega por un canal privado y sirve una sola vez.
3. Acéptala. Verás **una sola** máquina compartida: `unireserva-staging`.
4. Con Tailscale conectado abre la dirección `https://...ts.net` que te indique Mateo. Mientras no haya páginas, un 404 es correcto.

No tienes, ni necesitas, acceso a Windows, a la base de datos ni a la red del equipo. Si ves otras máquinas, avisa y no las uses.

Si no carga: comprueba que Tailscale esté conectado y pregunta si el equipo está encendido.

## Para Mateo (una vez, tras aprobar)

1. Crea una **tailnet nueva y exclusiva** para UniReserva, con otra identidad. No invites a nadie a tu tailnet actual. El Tailscale de Windows no se modifica.
2. Antes de añadir el nodo, reemplaza la política por [`deploy/tailscale/politica-ejemplo.hujson`](../deploy/tailscale/politica-ejemplo.hujson). Confirma que no queda ninguna regla con origen `*`: también alcanzaría a los invitados.
3. Activa MagicDNS y certificados HTTPS. El nombre de la máquina quedará en registros públicos de certificados.
4. Genera una clave de un solo uso, **sin etiquetas** (una máquina etiquetada no se puede compartir) y no efímera.
5. Ejecuta el primer despliegue sin `-ConAcceso` para crear `staging.env`; escribe la clave en `TS_AUTHKEY=`.
6. Despliega con `-ConAcceso`. Cuando el nodo aparezca, **borra la clave** de `staging.env`.
7. Comparte solo `unireserva-staging`, una invitación por persona. Para entrar tú desde tu cuenta habitual, compártela también contigo.
8. Decide si desactivas el vencimiento de la clave del nodo; si no, habrá que reautenticarlo.

## Comprobaciones obligatorias antes de dar acceso

| Desde el dispositivo de un invitado | Esperado |
|---|---|
| Web por HTTPS | Responde |
| Puertos 22, 3389, 5432 y 8080 de la máquina compartida | No conectan |
| Lista de máquinas | Solo la compartida |
| Direcciones del portátil o de su red local | No conectan |

Si la web responde con error de proxy: `serve.json` apunta a `http://app:8080`, algo **no probado** (la documentación de la línea de comandos solo menciona destinos locales). No abras puertos ni cambies redes sin revisar el diseño.

## Retirar el acceso

Revoca la invitación en la consola de Tailscale. Para cortar todo: `.\scripts\staging.ps1 detener`.

Fuentes: [compartir máquinas](https://tailscale.com/kb/1084/sharing) · [planes](https://tailscale.com/pricing) · [Docker](https://tailscale.com/docs/features/containers/docker/docker-params) · [HTTPS](https://tailscale.com/kb/1153/enabling-https)
