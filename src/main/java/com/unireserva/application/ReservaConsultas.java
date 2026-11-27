package com.unireserva.application;

import java.util.List;
import java.util.function.Predicate;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.HistorialReserva;
import com.unireserva.domain.Notificacion;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Reserva;
import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;

/**
 * Consultas de reservas (§4.3.6, §4.3.9, CQRS): solo leen. Un solicitante solo ve lo suyo (R6);
 * administrador y responsable ven todo (D4).
 */
public class ReservaConsultas {

	private final ReservaRepository reservas;
	private final NotificacionRepository notificaciones;
	private final ControlAcceso acceso;

	public ReservaConsultas(ReservaRepository reservas, NotificacionRepository notificaciones, ControlAcceso acceso) {
		this.reservas = reservas;
		this.notificaciones = notificaciones;
		this.acceso = acceso;
	}

	public List<Reserva> consultarReservas(long actorId) {
		return filtrar(actorId, r -> true);
	}

	public List<Reserva> consultarReservasPorEstado(long actorId, EstadoReserva estado) {
		return filtrar(actorId, r -> r.estado() == estado);
	}

	public List<Reserva> consultarReservasPorUsuario(long actorId, long usuarioId) {
		Usuario actor = acceso.exigir(actorId, Rol.values());
		exigirPropia(actor, usuarioId);
		return filtrar(actorId, r -> r.usuarioId() == usuarioId);
	}

	public List<Reserva> consultarReservasPorRecurso(long actorId, long recursoId) {
		return filtrar(actorId, r -> r.recursoId() == recursoId);
	}

	public List<HistorialReserva> consultarHistorial(long actorId, long reservaId) {
		Usuario actor = acceso.exigir(actorId, Rol.values());
		Reserva r = reservas.buscarPorId(reservaId)
				.orElseThrow(() -> new ReglaNegocioException("La reserva " + reservaId + " no existe"));
		exigirPropia(actor, r.usuarioId());
		return reservas.historialDe(reservaId);
	}

	/** Cada usuario ve solo sus avisos. */
	public List<Notificacion> consultarNotificaciones(long actorId) {
		acceso.exigir(actorId, Rol.values());
		return notificaciones.listarPorUsuario(actorId);
	}

	/** Unico punto donde se aplica la privacidad de las listas (R6). */
	private List<Reserva> filtrar(long actorId, Predicate<Reserva> criterio) {
		Usuario actor = acceso.exigir(actorId, Rol.values());
		return reservas.listar().stream()
				.filter(r -> actor.rol() != Rol.SOLICITANTE || r.usuarioId() == actorId)
				.filter(criterio)
				.toList();
	}

	private static void exigirPropia(Usuario actor, long duenoId) {
		if (actor.rol() == Rol.SOLICITANTE && duenoId != actor.id())
			throw new AccesoDenegadoException("Un solicitante solo consulta sus propias reservas");
	}
}
