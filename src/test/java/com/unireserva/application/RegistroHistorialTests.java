package com.unireserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.HistorialReserva;
import com.unireserva.domain.Reserva;
import com.unireserva.infrastructure.memoria.ReservaRepositoryEnMemoria;

class RegistroHistorialTests {

	@Test
	void guardaQuienCambioQueEstadoYCuando() { // P09, parte del historial
		ReservaRepository repo = new ReservaRepositoryEnMemoria();
		LocalDateTime ahora = LocalDateTime.of(2026, 12, 1, 9, 0);
		Reserva aprobada = repo.guardar(new Reserva(0, 2, 1, ahora, ahora.plusHours(2), "Clase",
				EstadoReserva.PENDIENTE, ahora)).cambiarA(EstadoReserva.APROBADA);

		new RegistroHistorial(repo).alCambiar(new CambioEstado(aprobada, EstadoReserva.PENDIENTE, 7, ahora));

		HistorialReserva h = repo.historialDe(aprobada.id()).get(0);
		assertEquals(EstadoReserva.PENDIENTE, h.estadoAnterior());
		assertEquals(EstadoReserva.APROBADA, h.estadoNuevo());
		assertEquals(7, h.usuarioResponsableId());
		assertEquals(ahora, h.fechaCambio());
	}
}
