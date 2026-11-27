package com.unireserva.application;

import java.util.Set;

import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Notificacion;
import com.unireserva.domain.Reserva;

/** Observador concreto (R9, §4.5): avisa al dueno cuando su reserva se aprueba, rechaza o cancela. */
public class NotificadorReserva implements ObservadorReserva {

	private static final Set<EstadoReserva> AVISABLES =
			Set.of(EstadoReserva.APROBADA, EstadoReserva.RECHAZADA, EstadoReserva.CANCELADA);

	private final NotificacionRepository notificaciones;

	public NotificadorReserva(NotificacionRepository notificaciones) {
		this.notificaciones = notificaciones;
	}

	@Override
	public void alCambiar(CambioEstado cambio) {
		Reserva r = cambio.reserva();
		if (!AVISABLES.contains(r.estado()))
			return;
		notificaciones.guardar(new Notificacion(0, r.usuarioId(), r.id(),
				"Su reserva " + r.id() + " fue " + r.estado(), cambio.fecha(), r.estado()));
	}
}
