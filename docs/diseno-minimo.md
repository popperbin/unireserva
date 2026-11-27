# UniReserva: mínimo académico completo

**Equipo: Mateo Avellaneda y Nicolain de Castro. Diseño ratificado** (documento maestro v0.6). **Etapa 1: contratos y dominio en código; servicios, repositorios en memoria y menús pendientes.** Entrega: 2 de diciembre de 2026. Actualizado el 27 de noviembre de 2026. Preparado con asistencia de Claude Code.

Fuente que manda: el enunciado del profesor (§).

## 1. Qué es obligatorio, qué hay que decidir y qué sobra

| Obligatorio (enunciado) | Decisión imprescindible para cumplirlo | Fuera del alcance |
|---|---|---|
| Usuarios con roles y control de operaciones (§4.1) | Elegir un usuario existente al iniciar | Contraseñas, sesiones |
| Recursos: registrar, consultar, modificar, estado, tipo (§4.2) | Tipos como `enum` | Catálogo editable, borrado |
| Reservas y consultas por estado, usuario, recurso (§4.3) | Quién cancela y finaliza | Horarios, recordatorios, reglas de fechas pasadas |
| Sin solapamientos aprobados; consecutivas permitidas (§4.4) | Intervalo `[inicio, fin)` | Concurrencia: un solo usuario a la vez en un proceso |
| Avisos al aprobar, rechazar, cancelar (§4.5) | Guardarlos y mostrarlos por consola | Correo o servicios externos, aun simulados |
| Cuatro reportes con datos registrados (§4.6) | Definiciones de la sección 5 | Exportar archivos, periodos, gráficos |
| Persistencia relacional **o simulada**, justificada (§7) | Colecciones en memoria | Base de datos, archivos |
| Arquitectura, Repository, DI, CQRS, 2 GoF (§8–9) | Hexagonal, Observer, Facade | Bus de comandos, contenedor de inyección |
| Calidad, diagramas, ADR, empresa, TOGAF, pruebas, entregables (§10–19) | Reparto de la sección 7 | Documentos adicionales |

§7 acepta aplicaciones de consola y persistencia simulada. La preferencia del docente por la consola es una aclaración verbal comunicada por Mateo.

**Consecuencia que hay que aceptar:** al cerrar el programa los datos desaparecen. La demostración se hace en una sola ejecución, con datos iniciales ficticios.

## 2. Estado de la base técnica

`main` contiene `Main`, un menú que solo permite salir, un `pom.xml` cuya única dependencia es JUnit y una CI sin servicios (PR #4, fusión `1143bf8`; #1–#3 cerrados sin fusionar). La etapa 1 añade los tipos de `domain`, los puertos de `application` y `ControlAcceso`, con sus pruebas.

## 3. Estructura mínima

Se organiza por capa: con unas 40 clases, pocas carpetas muestran la hexagonal con claridad. El documento maestro admite módulos "si son útiles"; el equipo prescinde de ellos (D2). Cada carpeta se crea con su primera clase.

```text
com.unireserva
  Main                      arma los objetos (inyección por constructor)
  domain/                   Usuario, Rol, Recurso, TipoRecurso, EstadoRecurso,
                            Reserva, EstadoReserva, HistorialReserva, Notificacion,
                            ReglaNegocioException, AccesoDenegadoException
  application/
    repositorios            UsuarioRepository, RecursoRepository,
                            ReservaRepository, NotificacionRepository
    comandos                UsuarioComandos, RecursoComandos, ReservaFacade
    consultas               UsuarioConsultas, RecursoConsultas, ReservaConsultas, ReporteConsultas
    apoyo                   ControlAcceso, ValidadorDisponibilidad
    observer                ObservadorReserva, CambioEstado, PublicadorReserva,
                            RegistroHistorial, NotificadorReserva
  infrastructure/memoria/   los 4 repositorios en memoria
  consola/                  MenuConsola (ya existe), MenuAdministracion, MenuReservas,
                            Entrada, DatosDemo
```

Las filas bajo `application` son grupos para leer, no subpaquetes. Dependencias: `consola` e `infrastructure → application → domain`. La consola no llama a repositorios; solo `Main` y `DatosDemo` los usan para armar el programa y cargar datos.

### Contratos

```java
// paquete: domain
public enum Rol { ADMINISTRADOR, SOLICITANTE, RESPONSABLE }
public enum TipoRecurso { SALA, LABORATORIO, EQUIPO_COMPUTO, PROYECTOR, AUDITORIO, VEHICULO }
public enum EstadoRecurso { DISPONIBLE, INACTIVO, MANTENIMIENTO }
public enum EstadoReserva {
    PENDIENTE, APROBADA, RECHAZADA, CANCELADA, FINALIZADA;
    public boolean puedePasarA(EstadoReserva nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == APROBADA || nuevo == RECHAZADA || nuevo == CANCELADA;
            case APROBADA -> nuevo == CANCELADA || nuevo == FINALIZADA;
            case RECHAZADA, CANCELADA, FINALIZADA -> false;
        };
    }
}
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) { super(mensaje); }
}
public record Usuario(long id, String nombre, String correo, Rol rol, boolean activo) {}
public record Recurso(long id, String nombre, TipoRecurso tipo, String descripcion,
                      String ubicacion, EstadoRecurso estado) {}
public record Reserva(long id, long usuarioId, long recursoId, LocalDateTime fechaInicio,
        LocalDateTime fechaFin, String motivo, EstadoReserva estado, LocalDateTime fechaCreacion) {
    public Reserva {
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio))
            throw new ReglaNegocioException("La fecha final debe ser posterior a la inicial");
        if (motivo == null || motivo.isBlank())
            throw new ReglaNegocioException("El motivo es obligatorio");
    }
    /** Intervalo [inicio, fin): 08-10 y 10-12 no se solapan. */
    public boolean seSolapaCon(Reserva otra) {
        return recursoId == otra.recursoId
                && fechaInicio.isBefore(otra.fechaFin) && fechaFin.isAfter(otra.fechaInicio);
    }
    public Reserva cambiarA(EstadoReserva nuevo) {
        if (!estado.puedePasarA(nuevo))
            throw new ReglaNegocioException("No se puede pasar de " + estado + " a " + nuevo);
        return new Reserva(id, usuarioId, recursoId, fechaInicio, fechaFin, motivo, nuevo, fechaCreacion);
    }
}
public record HistorialReserva(long id, long reservaId, EstadoReserva estadoAnterior,
        EstadoReserva estadoNuevo, LocalDateTime fechaCambio, long usuarioResponsableId) {}
public record Notificacion(long id, long usuarioId, long reservaId, String mensaje,
        LocalDateTime fecha, EstadoReserva estadoReserva) {}
```

```java
// paquete: application
public interface UsuarioRepository {
    Usuario guardar(Usuario usuario);            // id 0: crea; otro id: reemplaza
    Optional<Usuario> buscarPorId(long id);
    List<Usuario> listar();
}
public interface RecursoRepository {
    Recurso guardar(Recurso recurso);
    Optional<Recurso> buscarPorId(long id);
    List<Recurso> listar();
}
public interface ReservaRepository {
    Reserva guardar(Reserva reserva);
    Optional<Reserva> buscarPorId(long id);
    List<Reserva> listar();
    HistorialReserva agregarHistorial(HistorialReserva cambio);
    List<HistorialReserva> historialDe(long reservaId);
}
public interface NotificacionRepository {
    Notificacion guardar(Notificacion notificacion);
    List<Notificacion> listarPorUsuario(long usuarioId);
}
public record CambioEstado(Reserva reserva, EstadoReserva anterior, long actorId, LocalDateTime fecha) {}
public interface ObservadorReserva { void alCambiar(CambioEstado cambio); }
```

Firmas de los servicios. Cada constructor recibe sus dependencias; `actorId` identifica a quien actúa.

```java
// servicio: application
public class ControlAcceso {
    public Usuario exigir(long actorId, Rol... permitidos);          // usuario existente, activo y con el rol
}
public class UsuarioComandos {
    public long registrarUsuario(long actorId, String nombre, String correo, Rol rol);
    public void asignarRol(long actorId, long usuarioId, Rol rol);
}
public class UsuarioConsultas {
    public List<Usuario> consultarUsuarios(long actorId);
}
public class RecursoComandos {
    public long registrarRecurso(long actorId, String nombre, TipoRecurso tipo, String descripcion, String ubicacion);
    public void modificarRecurso(long actorId, long recursoId, String nombre, TipoRecurso tipo,
                                 String descripcion, String ubicacion);
    public void cambiarEstado(long actorId, long recursoId, EstadoRecurso estado);
}
public class RecursoConsultas {
    public List<Recurso> consultarRecursos(long actorId);
    public List<Recurso> consultarRecursosDisponibles(long actorId);
}
public class ValidadorDisponibilidad {
    public void exigirRecursoDisponible(Recurso recurso);
    public void exigirSinConflicto(Reserva candidata);               // contra las APROBADAS
}
public class ReservaFacade {
    public long crearReserva(long actorId, long recursoId, LocalDateTime inicio, LocalDateTime fin, String motivo);
    public void aprobarReserva(long actorId, long reservaId);
    public void rechazarReserva(long actorId, long reservaId);
    public void cancelarReserva(long actorId, long reservaId);
    public void finalizarReserva(long actorId, long reservaId);
}
public class ReservaConsultas {
    public List<Reserva> consultarReservas(long actorId);
    public List<Reserva> consultarReservasPorEstado(long actorId, EstadoReserva estado);
    public List<Reserva> consultarReservasPorUsuario(long actorId, long usuarioId);
    public List<Reserva> consultarReservasPorRecurso(long actorId, long recursoId);
    public List<HistorialReserva> consultarHistorial(long actorId, long reservaId);
    public List<Notificacion> consultarNotificaciones(long actorId);
}
public class ReporteConsultas {
    public Map<EstadoReserva, Long> generarReporteReservasPorEstado(long actorId);
    public Map<Usuario, Long> generarReporteReservasPorUsuario(long actorId);
    public Map<Recurso, Double> generarReporteUtilizacion(long actorId);             // horas
    public List<Map.Entry<Recurso, Long>> generarReporteRecursosMasSolicitados(long actorId);
}
public class PublicadorReserva {
    public PublicadorReserva(RegistroHistorial historial);           // obligatorio: siempre se notifica primero
    public void suscribir(ObservadorReserva observador);             // reacciones adicionales, como los avisos
    public void publicar(CambioEstado cambio);
}
public class RegistroHistorial implements ObservadorReserva {
    public void alCambiar(CambioEstado cambio);
}
public class NotificadorReserva implements ObservadorReserva {
    public void alCambiar(CambioEstado cambio);                      // APROBADA, RECHAZADA, CANCELADA
}
```

## 4. Persistencia simulada

Cada repositorio en memoria tiene un `Map<Long, Entidad>` (`LinkedHashMap`) y un contador para los identificadores. El historial es una lista dentro del repositorio de reservas.

- Las entidades son `record` inmutables: para cambiar algo hay que crear otra y llamar a `guardar`.
- `listar` devuelve una copia (`List.copyOf`).
- Los filtros y reportes recorren las listas en los servicios de consulta.
- Integridad que antes daría una base: los servicios comprueban que usuario y recurso existen; nada se borra.
- Sin transacciones: se valida **todo** antes de guardar. Después, guardar en memoria y avisar a los observadores no tiene fallos externos.
- Sin concurrencia: un proceso, un hilo, una persona en la consola. Si hubiera varios usuarios simultáneos habría que revisar esta decisión.
- `DatosDemo` crea un administrador, un responsable, dos solicitantes y algunos recursos ficticios.

| Justificación (§7) | Límite que se declara |
|---|---|
| Permite demostrar todas las funciones | No conserva datos entre ejecuciones |
| Sin instalación: ambos ejecutan igual | No demuestra consultas SQL ni control de concurrencia |
| El puerto `*Repository` permite añadir una base después sin cambiar el dominio | Esa migración es futura, no implementada |

## 5. Decisiones y reportes

Criterio acordado: la alternativa más sencilla que cumpla íntegramente el enunciado.

| ID | Decisión | Estado |
|---|---|---|
| D1 | Fusionar el PR de simplificación y cerrar #1–#3 | Hecha (`1143bf8`) |
| D2 | Estructura por capas de la sección 3, en lugar de módulos | Aceptada |
| D3 | Identidad: elegir un usuario existente y activo. No es autenticación | Adoptada |
| D4 | Administrador gestiona usuarios y recursos; solicitante crea, cancela y consulta **solo lo suyo**; responsable aprueba, rechaza y finaliza; administrador y responsable consultan reservas y reportes | Adoptada |
| D5 | No se crean ni aprueban reservas para recursos inactivos o en mantenimiento (§6, P07); se admiten pendientes solapadas y el conflicto se comprueba al aprobar (§4.4) | Adoptada |
| D6 | El dueño cancela una PENDIENTE o APROBADA; el responsable finaliza una APROBADA. Sin restricciones de hora: el enunciado no las pide | Adoptada |
| D7 | Solo los cambios de estado van al historial (§6); la creación no | Adoptada |
| D8 | Observer para historial y avisos; Facade limitada a reservas (sección 6) | Aceptada |
| D9 | Entrega el 2 de diciembre de 2026; el docente admite grupos de dos y estudió Observer y Facade en clase | Pendiente: disponibilidad y, si procede, numeración §14–15 |
| D10 | Sin consulta de disponibilidad por intervalo: §4.2 solo pide listar recursos disponibles | Adoptada |
| D11 | Utilización: horas de reservas FINALIZADAS | Adoptada |
| D12 | Reparto de la sección 7 | Ratificada |

Fuera del alcance por no exigirlo el enunciado: correo único, horarios, reservas en fechas pasadas, duración máxima y registro de intentos fallidos.

| Reporte | Definición |
|---|---|
| Por estado | Cantidad de reservas en cada estado |
| Por usuario | Cantidad de solicitudes de cada usuario |
| Utilización | Suma de horas de reservas FINALIZADAS por recurso |
| Más solicitados | Recursos ordenados por cantidad de solicitudes, en cualquier estado |

Indicadores propuestos (§12): tiempo promedio de aprobación (creación hasta el cambio a APROBADA); solicitudes atendidas (aprobadas más rechazadas); porcentaje de recursos disponibles.

## 6. Patrones

| Requisito | Evidencia | Sin él |
|---|---|---|
| Repository | 4 interfaces y 4 implementaciones | Los servicios dependerían de las colecciones |
| DI | Constructores; `Main` arma todo | No se podría sustituir una pieza en pruebas |
| CQRS básico | Comandos y consultas en clases distintas | Lecturas y cambios mezclados |

### Observer (comportamiento)

**Problema:** cada cambio de estado provoca dos reacciones, historial y aviso, y podrían aparecer más.
**Participantes:** sujeto `PublicadorReserva`; observador `ObservadorReserva`; concretos `RegistroHistorial` y `NotificadorReserva`.
**Sin él:** cada comando repetiría las llamadas y habría que modificarlos para añadir una reacción.
**Ventaja:** las reacciones se añaden suscribiendo una clase.
**Historial garantizado:** el historial es obligatorio (§6), así que no depende de una suscripción. `PublicadorReserva` lo recibe en su constructor y lo notifica siempre, antes que a los observadores suscritos; sin él, el programa no se puede armar. Los avisos sí se suscriben.
**Riesgo restante:** que un comando cambie el estado sin publicar. Lo cubre P09, que arma el sistema como `Main` y comprueba el historial tras aprobar, rechazar, cancelar y finalizar.

### Facade (estructural)

**Problema:** aprobar exige coordinar permisos, reserva, recurso, disponibilidad, guardado y publicación, en un orden concreto.
**Participantes:** fachada `ReservaFacade`; subsistema `ControlAcceso`, `ValidadorDisponibilidad`, `ReservaRepository`, `RecursoRepository`, `PublicadorReserva`; cliente `MenuReservas`.
**Sin ella:** el menú coordinaría esas piezas y contendría el flujo de negocio.
**Ventaja:** una llamada por operación; el orden está en un solo lugar.

Aprobación: exigir RESPONSABLE → buscar reserva → recurso disponible → sin conflicto → `cambiarA(APROBADA)` → guardar → publicar.

**Evaluación de la otra forma:** el documento maestro planteaba como candidata una fachada para que la consola use servicios de usuarios, recursos, reservas y reportes. Sobre estos servicios solo reenviaría llamadas, es decir, sería nominal. Por eso la fachada se limita a las reservas, donde sí hay coordinación.

**Defensa y debilidad:** la fachada es a la vez el servicio de comandos de reservas. Se sostiene porque oculta cinco colaboradores y las reglas de cada uno viven en ellos; sería nominal si solo reenviara llamadas. No debe acumular reglas propias ni consultas. Si el docente no la acepta, la alternativa es Factory Method (creacional) para construir las notificaciones.

Ambos se estudiaron en el curso (§9.4), según el docente.

Se descarta Adapter: sin servicio externo no hay interfaz incompatible que adaptar.

## 7. Encargos y plan para dos

Distribución del documento maestro, aceptada por Nicolain (D12). Incluye lo que aquel no asignaba: `ControlAcceso`, publicador, ADR de persistencia, decisiones y calidad.

| Área | Mateo | h | Nicolain | h |
|---|---|---|---|---|
| Código y pruebas | Contratos, dominio y `ControlAcceso` | 5 | Repositorios de usuarios, recursos y notificaciones | 1,5 |
| | Repositorio de reservas e historial | 1,5 | Usuarios y recursos: comandos y consultas | 5 |
| | `ValidadorDisponibilidad` y `ReservaFacade` | 7 | `PublicadorReserva` y `NotificadorReserva` | 2,5 |
| | `RegistroHistorial` | 0,5 | Menús y datos de demostración | 6 |
| | `ReservaConsultas` y `ReporteConsultas` | 5 | | |
| Documentos | C4 y 4+1 | 5 | ER conceptual | 3 |
| | ADR de arquitectura, decisiones y calidad | 4 | Casos de uso | 2,5 |
| | Diagrama de clases | 2 | Secuencia de crear y despliegue | 2,5 |
| | Secuencia de aprobar | 1 | TOGAF, brechas, fases e indicadores | 6 |
| | | | ADR de persistencia y de patrones | 2 |
| **Propio** | | **31** | | **31** |

Compartido, 24 horas-persona: acuerdos (2), revisión e integración (4), informe y evidencias (8), estudio y ensayo (10).

**Total: 86 horas-persona, unas 43 por persona; rango 70–100 (35–50 cada uno).** El documento maestro estima 45–60 (22,5–30 cada uno) sin desglosar estudio, revisión ni informe. Ambas cifras son orientativas; con la entrega el 2 de diciembre equivalen a unas 8 horas diarias por persona.

### Trabajo en paralelo

El único bloqueo común es un primer PR pequeño con tipos de dominio, las cuatro interfaces de repositorio, `ObservadorReserva`, `CambioEstado` y `ControlAcceso`.

| Etapa | Mateo | Nicolain | Terminada cuando | Fecha objetivo |
|---|---|---|---|---|
| 0 Acuerdo | Decide el PR de simplificación; consulta al docente | Revisa ese PR, ejecuta el proyecto, confirma reparto | D1, D9 y D12 resueltas | Hecha |
| 1 Contratos | PR de contratos y dominio | Borrador de ER y casos de uso; revisa el PR | PR fusionado | 27 nov |
| 2 Piezas | Repositorio de reservas, validador, `RegistroHistorial` | Sus repositorios, usuarios, recursos, publicador y avisos | Cada PR en verde y revisado | 28 nov |
| 3 Primera entrega | Fachada: crear y aprobar | Menús de usuarios y recursos; después, crear y aprobar | Registrar, crear, aprobar, rechazar el solapamiento y ver historial por consola | 29 nov |
| 4 Completar | Demás estados, consultas y reportes | Resto de menús, avisos, datos de demostración | P01–P10 con resultado real | 30 nov |
| 5 Cierre | Sus documentos | Sus documentos | Informe revisado y ensayo | 1 dic |

Reglas para no estorbarse:

- Cada uno implementa sus repositorios y prueba lo suyo; las firmas de la sección 3 solo cambian si ambos acuerdan.
- `Main` lo integra solo Mateo, para evitar conflictos.
- La fachada y el publicador se encuentran en la etapa 3: el publicador debe estar fusionado antes.
- Nicolain justifica los dos patrones, aunque Mateo programa `ReservaFacade`; ambos los defienden.
- Los borradores de documentos pueden avanzar mientras se espera una revisión; los diagramas finales se ajustan al código.
- Revisión mutua: nadie fusiona su propio PR. Ambos explican el sistema completo.

## 8. Matriz de cumplimiento

Todo está **pendiente**: la matriz dice cómo se cumplirá, no que se cumpla.

| § | Requisito | Cómo y evidencia prevista | Dueño |
|---|---|---|---|
| 1–3 | Contexto, nombre, objetivo | Informe; UniReserva, universidad ficticia | Ambos |
| 4.1 | Registrar, rol, consultar usuarios | `UsuarioComandos`, `UsuarioConsultas`; P01, EX01 | Nicolain |
| 4.1.4 | Control por rol | `ControlAcceso`; P08 | Mateo |
| 4.2 | Recursos: registrar, disponibles, modificar, estado, tipo | `RecursoComandos`, `RecursoConsultas`; P02, P07, EX02 | Nicolain |
| 4.3.1–5 | Crear con recurso, inicio, fin, motivo | `ReservaFacade.crearReserva`; P03, P04 | Mateo |
| 4.3.6, 4.3.9 | Consultar y filtrar | `ReservaConsultas`; EX03 | Mateo |
| 4.3.7–8 | Aprobar, rechazar, cancelar | `ReservaFacade`; P05, EX04 | Mateo |
| 4.4 | Conflictos y consecutivas | `ValidadorDisponibilidad`; P06, EX05 | Mateo |
| 4.5 | Notificaciones | `NotificadorReserva`, mostradas en consola; EX07 | Nicolain |
| 4.6 | Cuatro reportes | `ReporteConsultas`; P10 | Mateo |
| 5 | Cinco entidades y estados | `domain`; FINALIZADA con `finalizarReserva` | Mateo, Nicolain |
| 6 R1–R2 | Usuario y recurso existentes; fechas | Fachada y `Reserva`; P03, P04, EX08 | Mateo |
| 6 R3–R5 | Recurso no disponible, solapamiento, autorización | Validador y acceso; P06–P08 | Mateo |
| 6 R6 | Solicitante consulta las suyas | `ReservaConsultas`; EX03 | Mateo |
| 6 R7 | Cancelada no se reaprueba | `EstadoReserva`; EX04 | Mateo |
| 6 R8–R9 | Historial y notificaciones | Observadores; P09, EX07 | Mateo (historial), Nicolain (avisos) |
| 6 R10 | Reportes con datos registrados | Leen repositorios; P10, EX10 | Mateo |
| 7 | POO, capas, interfaces, repositorios, servicios, DI, validación, persistencia, errores, patrones | Código; errores de dominio mostrados por consola | Ambos |
| 8 | Arquitectura y sus siete preguntas | Sección 9; ADR 001 | Mateo |
| 9.1–9.3 | Repository, DI, CQRS | Sección 6 | Mateo |
| 9.4 | Dos GoF y sus siete preguntas, con diagrama de relación | Sección 6; ADR 003 | Nicolain |
| 10 | Cuatro atributos, dos escenarios | Sección 9 | Mateo |
| 11.1 | Tres decisiones de diseño y tres arquitectónicas | Sección 9 | Mateo |
| 11.2 | C4: contexto, contenedores, componentes | Contenedor único: aplicación de consola con almacenamiento en memoria | Mateo |
| 11.3 | 4+1 | Descripción de cinco vistas | Mateo |
| 11.4 | Casos de uso; secuencias crear y aprobar | Mermaid o PlantUML según el código | Nicolain (casos y crear), Mateo (aprobar) |
| 11.4 | Clases; entidad–relación | ER **lógico** de las colecciones, declarado como tal | Mateo (clases), Nicolain (ER) |
| 11.4 | Componentes; despliegue | Componentes reutiliza C4; despliegue: una JVM en un equipo | Mateo, Nicolain |
| 11.5 | Tres ADR completos | Hexagonal; persistencia y Repository; GoF | Mateo, Nicolain |
| 12 | Siete preguntas empresariales y tres indicadores | Informe | Nicolain |
| 13 | Actual, objetivo, cuatro brechas, cuatro fases | Separar lo hecho de lo futuro | Nicolain |
| 16 | P01–P10 con entrada, esperado, obtenido, estado | JUnit y tabla de evidencias | Cada dueño |
| 17.1 | Documento final con todas sus secciones, incluidas conclusiones | Informe en Drive | Ambos |
| 17.2 | Código y README | Repositorio | Ambos |
| 17.3 | Capturas, funcionalidades, resultados, explicación | Evidencias; script de base de datos: no aplica | Ambos |
| 17.4 | Sustentación con participación de todos | Simulacro | Ambos |
| 18 | Etapas progresivas | Orden de la sección 7 | Ambos |
| 19, 21 | Criterios y producto esperado | Lista de comprobación final | Ambos |
| 20 | Diez restricciones | Coherencia código-documentos, sin patrones nominales, la consola no accede a datos, trabajo nuevo y comprendido, conflictos validados | Ambos |

El enunciado no tiene §14 ni §15: aclararlo con el docente si procede (D9).

### Pruebas

Obligatorias: P01 registrar usuario; P02 registrar recurso; P03 reserva válida; P04 fin anterior al inicio; P05 aprobar disponible; P06 rechazar conflicto; P07 reservar en mantenimiento; P08 aprobar sin permiso; P09 cambio de estado e historial; P10 reporte con datos registrados.

Comprobaciones de cobertura (documento maestro), no requisitos nuevos: EX01 roles y consulta de usuarios; EX02 modificar, clasificar y cambiar estado; EX03 filtros y privacidad; EX04 transiciones; EX05 consecutivas; EX07 historial y avisos; EX08 referencias inexistentes; EX10 reportes y datos vacíos. EX06 y EX09 quedan fuera por no haber concurrencia ni base de datos.

Cada prueba se registra con identificador, funcionalidad, entrada, resultado esperado, obtenido, estado y commit.

## 9. Base para los documentos

**Arquitectura (§8):** hexagonal simplificada. Adecuada porque las reglas son el centro y la consola y la persistencia son reemplazables. Ventajas: pruebas sin infraestructura y trabajo paralelo. Desventajas: más interfaces que un programa en un solo bloque. Favorece modificabilidad y testeabilidad. Se evidencia en las tres carpetas y la dirección de dependencias. Un nuevo adaptador no cambia el dominio. Evolución: eventos publicando desde un observador a un broker; servicios separando capacidades, con APIs y datos propios, lo que obligaría a resolver red y consistencia.

| Atributo | Riesgo si falta | Decisión | Comprobación |
|---|---|---|---|
| Seguridad (autorización) | Aprobaciones indebidas | `ControlAcceso` en cada servicio | **Escenario:** P08 |
| Trazabilidad | No saber quién cambió qué | Observador de historial | **Escenario:** P09 |
| Modificabilidad | Un cambio rompe todo | Puertos y observadores | Añadir un observador sin tocar la fachada |
| Testeabilidad | Reglas sin verificar | Dominio sin consola; DI | Pruebas sin infraestructura |

Decisiones arquitectónicas: hexagonal; persistencia en memoria tras Repository; separar comandos y consultas. Decisiones de diseño: `record` inmutables; transiciones dentro de `EstadoReserva`; intervalo semiabierto.

## 10. Verificación de los contratos

Los tipos de la sección 3, salvo los servicios, están en `src/main/java` desde la etapa 1.

| Comprobación | Resultado |
|---|---|
| `.\mvnw.cmd verify` con JDK 21 | `BUILD SUCCESS`; 13 pruebas, 0 fallos |
| `DominioTests` | Fechas (P04), motivo, solapamiento (P06) y consecutivas (EX05), transiciones (EX04), datos básicos |
| `ControlAccesoTests` | Rol permitido, solicitante que aprueba (P08), usuario inexistente o inactivo |
| Dependencias entre paquetes (`jdeps`) | Solo `application → domain` |

Las pruebas P04, P06 y P08 cubren aquí la regla de dominio; su versión completa, a través de la fachada, llega con los servicios. Ninguna otra prueba P o EX se ha ejecutado.
