package com.unireserva.domain;

/** Se incumple una regla de negocio (§6). La consola muestra el mensaje y nada se guarda. */
public class ReglaNegocioException extends RuntimeException {

	public ReglaNegocioException(String mensaje) {
		super(mensaje);
	}
}
