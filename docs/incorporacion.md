# Guía de incorporación (Michel y Nicolain)

Guía para Windows con PowerShell. Tiempo principal: las descargas de la primera vez.

## 1. Acceso

1. Envía tu usuario de GitHub a Mateo y acepta la invitación como colaborador. Leer el repositorio es público; subir ramas requiere la invitación.
2. Pide acceso a Jira y a la carpeta de Drive con tu propia cuenta. Nadie comparte contraseñas ni tokens.

## 2. Instalar (una sola vez)

```powershell
winget install Git.Git
winget install EclipseAdoptium.Temurin.21.JDK
winget install Docker.DockerDesktop
```

Maven no se instala. Reinicia la terminal y abre Docker Desktop.

El instalador de Java puede cambiar tu `PATH`. Si otro proyecto tuyo usa una versión distinta, comprueba `java -version` allí después de instalar.

## 3. Identidad en Git

El repositorio es **público**: el correo de tus commits queda visible. Para ocultarlo, activa en GitHub *Settings → Emails → Keep my email addresses private* y usa la dirección `noreply` que te muestra:

```powershell
git config --global user.name "Tu Nombre"
git config --global user.email "<id>+<usuario>@users.noreply.github.com"
```

(Sin `--global` y dentro del repositorio, aplica solo a este proyecto.)

## 4. Clonar y ejecutar

```powershell
git clone https://github.com/popperbin/unireserva.git
cd unireserva
.\scripts\usar-jdk21.ps1          # en cada terminal nueva
.\mvnw.cmd -v                     # debe decir Java version: 21
Copy-Item .env.example .env       # escribe en .env una contraseña inventada por ti
docker compose up -d --wait
.\mvnw.cmd verify                 # esperado: Tests run: 1, Failures: 0 ... BUILD SUCCESS
.\mvnw.cmd spring-boot:run        # http://127.0.0.1:8080 devuelve 404 (esperado). Ctrl+C para salir
docker compose down
```

Si PowerShell bloquea el script: `Set-ExecutionPolicy -Scope Process Bypass` (solo afecta a esa terminal).

En IntelliJ: *File → Project Structure → SDK → 21*.

## 5. Ten en cuenta

- Todos los clones de tu equipo comparten la misma base local (proyecto Compose `unireserva`). Con un segundo clon, usa la misma contraseña en su `.env`.
- `docker compose down -v` **borra** tus datos locales.
- Problemas frecuentes: ver [README](../README.md).

## 6. Antes de programar

Lee [CONTRIBUTING.md](../CONTRIBUTING.md) y [la propuesta de arquitectura](arquitectura/README.md). La implementación funcional empieza cuando el equipo apruebe decisiones, contratos y modelo de datos.

## Comprobación

Envía al equipo la salida de `.\mvnw.cmd -v` y el resumen final de `.\mvnw.cmd verify`.
