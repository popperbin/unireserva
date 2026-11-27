# Guía de incorporación

Para Windows con PowerShell. Solo se necesitan Git y JDK 21.

## 1. Acceso

Envía tu usuario de GitHub a Mateo y acepta la invitación: leer es público, subir ramas requiere invitación. Nadie comparte contraseñas ni tokens.

## 2. Instalar (una vez)

```powershell
winget install Git.Git
winget install EclipseAdoptium.Temurin.21.JDK
```

Abre una terminal nueva. El instalador puede cambiar tu `PATH`: si otro proyecto usa otra versión de Java, compruébalo.

## 3. Identidad en Git

El repositorio es **público**. Para no exponer tu correo, usa la dirección `noreply` de GitHub (*Settings → Emails*), dentro del repositorio:

```powershell
git config user.name "Tu Nombre"
git config user.email "<id>+<usuario>@users.noreply.github.com"
```

## 4. Clonar y ejecutar

```powershell
git clone https://github.com/popperbin/unireserva.git
cd unireserva
.\mvnw.cmd -v                     # debe decir Java version: 21
.\mvnw.cmd verify                 # esperado: Failures: 0, Errors: 0 ... BUILD SUCCESS
java -jar target\unireserva-0.1.0-SNAPSHOT.jar
```

Si no aparece Java 21 (por ejemplo, tienes otro Java), actívalo en esa terminal:

```powershell
.\scripts\usar-jdk21.ps1
```

Si PowerShell bloquea el script: `Set-ExecutionPolicy -Scope Process Bypass` (solo esa terminal).

En IntelliJ: *File → Project Structure → SDK → 21*.

## 5. Antes de programar

Lee [CONTRIBUTING.md](../CONTRIBUTING.md) y la [propuesta de diseño](diseno-minimo.md). El código de negocio empieza cuando ambos acuerden el diseño de detalle.

## Comprobación

Envía al equipo la salida de `.\mvnw.cmd -v` y el resumen final de `.\mvnw.cmd verify`.
