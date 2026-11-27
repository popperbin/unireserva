package com.unireserva.application;

import java.time.LocalDateTime;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Reserva;
import com.unireserva.domain.Rol;

/**
 * Facade y comandos de reservas (§4.3, CQRS). Coordina permisos, recurso, disponibilidad,
 * guardado y publicacion en un orden fijo; las reglas viven en cada colaborador. Todo se valida
 * antes de guardar, porque la memoria no tiene transacciones.
 */
public class ReservaFacade {

	private final ControlAcceso acceso;
	private final RecursoRepository recursos;
	private final ReservaRepository reservas;
	private final ValidadorDisponibilidad validador;
	private final PublicadorReserva publicador;

	public ReservaFacade(ControlAcceso acceso, RecursoRepository recursos, ReservaRepository reservas,
			ValidadorDisponibilidad validador, PublicadorReserva publicador) {
		this.acceso = acceso;
		this.recursos = recursos;
		this.reservas = reservas;
		this.validador = validador;
		this.publicador = publicador;
	}

	/** Queda PENDIENTE; no se publica porque la creacion no va al historial (D7). */
	public long crearReserva(long actorId, long recursoId, LocalDateTime inicio, LocalDateTime fin, String motivo) {
		acceso.exigir(actorId, Rol.SOLICITANTE);
		validador.exigirRecursoDisponible(recurso(recursoId));
		Reserva nueva = new Reserva(0, actorId, recursoId, inicio, fin, motivo, EstadoReserva.PENDIENTE,
				LocalDateTime.now());
		return reservas.guardar(nueva).id();
	}

	public void aprobarReserva(long actorId, long reservaId) {
		acceso.exigir(actorId, Rol.RESPONSABLE);
		Reserva r = reserva(reservaId);
		validador.exigirRecursoDisponible(recurso(r.recursoId()));
		validador.exigirSinConflicto(r);
		cambiar(actorId, r, EstadoReserva.APROBADA);
	}

	public void rechazarReserva(long actorId, long reservaId) {
		acceso.exigir(actorId, Rol.RESPONSABLE);
		cambiar(actorId, reserva(reservaId), EstadoReserva.RECHAZADA);
	}

	/** Solo el dueno cancela lo suyo (D6). */
	public void cancelarReserva(long actorId, long reservaId) {
		acceso.exigir(actorId, Rol.SOLICITANTE);
		Reserva r = reserva(reservaId);
		if (r.usuarioId() != actorId)
			throw new AccesoDenegadoException("Solo el dueno puede cancelar la reserva " + reservaId);
		cambiar(actorId, r, EstadoReserva.CANCELADA);
	}

	public void finalizarReserva(long actorId, long reservaId) {
		acceso.exigir(actorId, Rol.RESPONSABLE);
		cambiar(actorId, reserva(reservaId), EstadoReserva.FINALIZADA);
	}

	/** cambiarA valida la transicion antes de guardar; despues se publica a los observadores. */
	private void cambiar(long actorId, Reserva r, EstadoReserva nuevo) {
		Reserva cambiada = reservas.guardar(r.cambiarA(nuevo));
		publicador.publicar(new CambioEstado(cambiada, r.estado(), actorId, LocalDateTime.now()));
	}

	private Recurso recurso(long recursoId) {
		return recursos.buscarPorId(recursoId)
				.orElseThrow(() -> new ReglaNegocioException("El recurso " + recursoId + " no existe"));
	}

	private Reserva reserva(long reservaId) {
		return reservas.buscarPorId(reservaId)
				.orElseThrow(() -> new ReglaNegocioException("La reserva " + reservaId + " no existe"));
	}
}
