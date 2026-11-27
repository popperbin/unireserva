package com.unireserva.application;

import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Reserva;

/** Reglas R3 y R4 (§6): recurso disponible y sin solapamiento con reservas ya aprobadas (§4.4). */
public class ValidadorDisponibilidad {

	private final ReservaRepository reservas;

	public ValidadorDisponibilidad(ReservaRepository reservas) {
		this.reservas = reservas;
	}

	/** Se usa al crear y al aprobar: un recurso inactivo o en mantenimiento no se reserva (D5). */
	public void exigirRecursoDisponible(Recurso recurso) {
		if (recurso.estado() != EstadoRecurso.DISPONIBLE)
			throw new ReglaNegocioException("El recurso " + recurso.nombre() + " esta " + recurso.estado());
	}

	/** Se usa al aprobar: las pendientes pueden solaparse entre si, las aprobadas no (D5). */
	public void exigirSinConflicto(Reserva candidata) {
		boolean conflicto = reservas.listar().stream()
				.anyMatch(r -> r.estado() == EstadoReserva.APROBADA && r.id() != candidata.id()
						&& r.seSolapaCon(candidata));
		if (conflicto)
			throw new ReglaNegocioException("El recurso ya tiene una reserva aprobada en ese horario");
	}
}
