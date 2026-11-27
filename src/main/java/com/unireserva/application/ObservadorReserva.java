package com.unireserva.application;

/** Observador de Observer: reacciona a cada cambio de estado ya guardado. */
public interface ObservadorReserva {

	void alCambiar(CambioEstado cambio);
}
