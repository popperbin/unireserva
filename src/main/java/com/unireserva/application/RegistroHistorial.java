package com.unireserva.application;

import com.unireserva.domain.HistorialReserva;

/**
 * Observador concreto obligatorio (R8, §6): guarda cada cambio de estado. PublicadorReserva lo
 * recibe en su constructor, asi que no puede quedar sin suscribir.
 */
public class RegistroHistorial implements ObservadorReserva {

	private final ReservaRepository reservas;

	public RegistroHistorial(ReservaRepository reservas) {
		this.reservas = reservas;
	}

	@Override
	public void alCambiar(CambioEstado cambio) {
		reservas.agregarHistorial(new HistorialReserva(0, cambio.reserva().id(), cambio.anterior(),
				cambio.reserva().estado(), cambio.fecha(), cambio.actorId()));
	}
}
