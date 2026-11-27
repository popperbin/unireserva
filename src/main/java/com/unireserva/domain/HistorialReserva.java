package com.unireserva.domain;

import java.time.LocalDateTime;

/** Un cambio de estado de una reserva (§5, §6): de que estado a cual, cuando y quien lo hizo. */
public record HistorialReserva(long id, long reservaId, EstadoReserva estadoAnterior,
		EstadoReserva estadoNuevo, LocalDateTime fechaCambio, long usuarioResponsableId) {
}
