package com.unireserva.domain;

/** El actor no existe, esta inactivo o su rol no permite la operacion (§4.1.4). */
public class AccesoDenegadoException extends RuntimeException {

	public AccesoDenegadoException(String mensaje) {
		super(mensaje);
	}
}
