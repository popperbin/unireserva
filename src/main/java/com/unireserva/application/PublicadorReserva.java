package com.unireserva.application;

import java.util.ArrayList;
import java.util.List;

/**
 * Sujeto de Observer. El historial es obligatorio (R8): llega por constructor y se notifica
 * siempre primero. Las demas reacciones, como los avisos, se suscriben.
 */
public class PublicadorReserva {

	private final RegistroHistorial historial;
	private final List<ObservadorReserva> observadores = new ArrayList<>();

	public PublicadorReserva(RegistroHistorial historial) {
		this.historial = historial;
	}

	public void suscribir(ObservadorReserva observador) {
		observadores.add(observador);
	}

	public void publicar(CambioEstado cambio) {
		historial.alCambiar(cambio);
		observadores.forEach(o -> o.alCambiar(cambio));
	}
}
