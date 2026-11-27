package com.unireserva.domain;

/**
 * Estados de una reserva (§5) y las unicas transiciones permitidas.
 * RECHAZADA, CANCELADA y FINALIZADA son terminales: una cancelada no se reaprueba (§6).
 */
public enum EstadoReserva {
	PENDIENTE, APROBADA, RECHAZADA, CANCELADA, FINALIZADA;

	public boolean puedePasarA(EstadoReserva nuevo) {
		return switch (this) {
			case PENDIENTE -> nuevo == APROBADA || nuevo == RECHAZADA || nuevo == CANCELADA;
			case APROBADA -> nuevo == CANCELADA || nuevo == FINALIZADA;
			case RECHAZADA, CANCELADA, FINALIZADA -> false;
		};
	}
}
