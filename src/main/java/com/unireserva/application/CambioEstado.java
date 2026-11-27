package com.unireserva.application;

import java.time.LocalDateTime;

import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Reserva;

/** Evento de Observer: la reserva ya en su nuevo estado, el estado anterior, quien actuo y cuando. */
public record CambioEstado(Reserva reserva, EstadoReserva anterior, long actorId, LocalDateTime fecha) {
}
