# UniReserva

Prototipo académico de gestión de recursos y reservas universitarias (Arquitectura de Software Empresarial).

Aplicación **Java 21 de consola**, sin frameworks ni base de datos. Los datos vivirán en memoria y se perderán al cerrar el programa.

**Estado: base técnica.** El menú solo permite salir. No hay funcionalidades de negocio.

| Integrante | Área |
|---|---|
| Mateo Avellaneda | Arquitectura, contratos, reservas, historial, reportes e integración |
| Nicolain de Castro | Datos (ER), usuarios, recursos, consola, notificaciones, patrones y TOGAF |

## Ejecutar

Requisitos: Git y JDK 21. Maven no se instala: `mvnw` lo descarga la primera vez (necesita internet).

Windows (PowerShell), desde la raíz del repositorio:

```powershell
.\mvnw.cmd verify                                  # compila, prueba y empaqueta
java -jar target\unireserva-0.1.0-SNAPSHOT.jar     # menú; escribe 0 para salir
```

Linux o macOS:

```bash
./mvnw verify
java -jar target/unireserva-0.1.0-SNAPSHOT.jar
```

Si `.\mvnw.cmd -v` no muestra Java 21, ver la [guía de incorporación](docs/incorporacion.md).

## Estructura

```text
.github/workflows/ci.yml        Integración continua
.mvn/, mvnw, mvnw.cmd           Maven Wrapper
src/main/java/com/unireserva/
  Main.java                     Arranque y composición de objetos
  domain/                       Entidades, estados y reglas propias
  application/                  Puertos de repositorio, Observer y control de acceso
  consola/                      Menú (adaptador de entrada)
src/test/java/                  Pruebas JUnit
docs/diseno-minimo.md           Propuesta de diseño, encargos y matriz de cumplimiento
docs/incorporacion.md           Preparar el equipo
scripts/usar-jdk21.ps1          Activar JDK 21 en una terminal
pom.xml                         Compilación; única dependencia: JUnit (pruebas)
```

Colaboración: [CONTRIBUTING.md](CONTRIBUTING.md) · Asistentes de IA: [AGENTS.md](AGENTS.md)
