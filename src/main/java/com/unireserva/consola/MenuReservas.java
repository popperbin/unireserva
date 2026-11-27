package com.unireserva.consola;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.unireserva.application.ReporteConsultas;
import com.unireserva.application.ReservaConsultas;
import com.unireserva.application.ReservaFacade;
import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Reserva;

/** Submenu de reservas (§4.3), avisos (§4.5) y reportes (§4.6). Cliente de la fachada: una llamada por operacion. */
public class MenuReservas {

	private static final String OPCIONES = """
			-- Reservas y reportes --
			1. Crear reserva
			2. Aprobar reserva
			3. Rechazar reserva
			4. Cancelar reserva
			5. Finalizar reserva
			6. Consultar reservas
			7. Consultar por estado
			8. Consultar por usuario
			9. Consultar por recurso
			10. Ver historial de una reserva
			11. Ver mis notificaciones
			12. Ver reportes
			0. Volver""";

	private final Entrada entrada;
	private final ReservaFacade facade;
	private final ReservaConsultas consultas;
	private final ReporteConsultas reportes;

	public MenuReservas(Entrada entrada, ReservaFacade facade, ReservaConsultas consultas, ReporteConsultas reportes) {
		this.entrada = entrada;
		this.facade = facade;
		this.consultas = consultas;
		this.reportes = reportes;
	}

	public void ejecutar(long actor) throws IOException {
		while (true) {
			entrada.mostrar(OPCIONES);
			String opcion = entrada.texto(">");
			if (opcion.equals("0"))
				return;
			try {
				atender(actor, opcion);
			} catch (ReglaNegocioException | AccesoDenegadoException e) {
				entrada.mostrar("Error: " + e.getMessage());
			}
		}
	}

	private void atender(long actor, String opcion) throws IOException {
		switch (opcion) {
			case "1" -> entrada.mostrar("Reserva creada con id " + facade.crearReserva(actor,
					entrada.numero("Id del recurso:"), entrada.fecha("Inicio"), entrada.fecha("Fin"),
					entrada.texto("Motivo:")) + " (PENDIENTE)");
			case "2" -> {
				facade.aprobarReserva(actor, idReserva());
				entrada.mostrar("Reserva aprobada.");
			}
			case "3" -> {
				facade.rechazarReserva(actor, idReserva());
				entrada.mostrar("Reserva rechazada.");
			}
			case "4" -> {
				facade.cancelarReserva(actor, idReserva());
				entrada.mostrar("Reserva cancelada.");
			}
			case "5" -> {
				facade.finalizarReserva(actor, idReserva());
				entrada.mostrar("Reserva finalizada.");
			}
			case "6" -> listar(consultas.consultarReservas(actor));
			case "7" -> listar(consultas.consultarReservasPorEstado(actor, entrada.opcion("Estado", EstadoReserva.values())));
			case "8" -> listar(consultas.consultarReservasPorUsuario(actor, entrada.numero("Id del usuario:")));
			case "9" -> listar(consultas.consultarReservasPorRecurso(actor, entrada.numero("Id del recurso:")));
			case "10" -> consultas.consultarHistorial(actor, idReserva()).forEach(h -> entrada.mostrar(
					h.estadoAnterior() + " -> " + h.estadoNuevo() + ", " + fecha(h.fechaCambio())
							+ ", por el usuario " + h.usuarioResponsableId()));
			case "11" -> consultas.consultarNotificaciones(actor).forEach(n -> entrada.mostrar(
					fecha(n.fecha()) + " " + n.mensaje()));
			case "12" -> mostrarReportes(actor);
			default -> entrada.mostrar("Opcion no valida.");
		}
	}

	private long idReserva() throws IOException {
		return entrada.numero("Id de la reserva:");
	}

	private void listar(List<Reserva> reservas) {
		if (reservas.isEmpty())
			entrada.mostrar("No hay reservas.");
		reservas.forEach(r -> entrada.mostrar("#" + r.id() + " usuario " + r.usuarioId() + ", recurso " + r.recursoId()
				+ " | " + fecha(r.fechaInicio()) + " a " + fecha(r.fechaFin()) + " | " + r.estado() + " | " + r.motivo()));
	}

	private void mostrarReportes(long actor) {
		entrada.mostrar("-- Reservas por estado --");
		reportes.generarReporteReservasPorEstado(actor).forEach((e, n) -> entrada.mostrar(e + ": " + n));
		entrada.mostrar("-- Reservas por usuario --");
		reportes.generarReporteReservasPorUsuario(actor).forEach((u, n) -> entrada.mostrar(u.nombre() + ": " + n));
		entrada.mostrar("-- Utilizacion (horas de reservas finalizadas) --");
		reportes.generarReporteUtilizacion(actor).forEach((r, h) -> entrada.mostrar(r.nombre() + ": " + h + " h"));
		entrada.mostrar("-- Recursos mas solicitados --");
		reportes.generarReporteRecursosMasSolicitados(actor)
				.forEach(e -> entrada.mostrar(e.getKey().nombre() + ": " + e.getValue()));
	}

	private static String fecha(LocalDateTime f) {
		return f.toString().replace('T', ' ');
	}
}
