package com.unireserva.domain;

import java.time.LocalDateTime;

/** Aviso al dueno de una reserva aprobada, rechazada o cancelada (§4.5). Se muestra por consola. */
public record Notificacion(long id, long usuarioId, long reservaId, String mensaje,
		LocalDateTime fecha, EstadoReserva estadoReserva) {
}
