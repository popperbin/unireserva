package com.unireserva.domain;

/** Disponibilidad de un recurso (§5). Solo DISPONIBLE admite crear y aprobar reservas (D5). */
public enum EstadoRecurso {
	DISPONIBLE, INACTIVO, MANTENIMIENTO
}
