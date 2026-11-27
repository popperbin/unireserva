package com.unireserva.domain;

import java.time.LocalDateTime;

/**
 * Solicitud de uso de un recurso (§5). Aqui viven las reglas que solo dependen de la reserva:
 * fechas, motivo, solapamiento y transiciones. Las que necesitan otros datos (permisos,
 * recurso disponible, reservas aprobadas) viven en los servicios.
 */
public record Reserva(long id, long usuarioId, long recursoId, LocalDateTime fechaInicio,
		LocalDateTime fechaFin, String motivo, EstadoReserva estado, LocalDateTime fechaCreacion) {

	public Reserva {
		if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio))
			throw new ReglaNegocioException("La fecha final debe ser posterior a la inicial");
		if (motivo == null || motivo.isBlank())
			throw new ReglaNegocioException("El motivo es obligatorio");
		if (estado == null || fechaCreacion == null)
			throw new ReglaNegocioException("El estado y la fecha de creacion son obligatorios");
	}

	/** Intervalo [inicio, fin): 08-10 y 10-12 no se solapan; 08-10 y 09-11 si (§4.4). */
	public boolean seSolapaCon(Reserva otra) {
		return recursoId == otra.recursoId
				&& fechaInicio.isBefore(otra.fechaFin) && fechaFin.isAfter(otra.fechaInicio);
	}

	/** Devuelve una copia en el nuevo estado; el registro original no cambia. */
	public Reserva cambiarA(EstadoReserva nuevo) {
		if (!estado.puedePasarA(nuevo))
			throw new ReglaNegocioException("No se puede pasar de " + estado + " a " + nuevo);
		return new Reserva(id, usuarioId, recursoId, fechaInicio, fechaFin, motivo, nuevo, fechaCreacion);
	}
}
