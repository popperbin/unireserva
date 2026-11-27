package com.unireserva.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Reserva;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.infrastructure.memoria.ReservaRepositoryEnMemoria;

class ValidadorDisponibilidadTests {

	private static final LocalDateTime OCHO = LocalDateTime.of(2026, 12, 1, 8, 0);
	private final ReservaRepository repo = new ReservaRepositoryEnMemoria();
	private final ValidadorDisponibilidad validador = new ValidadorDisponibilidad(repo);

	private static Reserva reserva(int desde, int hasta, EstadoReserva estado) {
		return new Reserva(0, 2, 1, OCHO.plusHours(desde), OCHO.plusHours(hasta), "Clase", estado, OCHO);
	}

	private static Recurso recurso(EstadoRecurso estado) {
		return new Recurso(1, "Sala 101", TipoRecurso.SALA, "Sala", "Bloque A", estado);
	}

	@Test
	void recursoEnMantenimientoOInactivoNoSeReserva() { // P07
		assertDoesNotThrow(() -> validador.exigirRecursoDisponible(recurso(EstadoRecurso.DISPONIBLE)));
		assertThrows(ReglaNegocioException.class, () -> validador.exigirRecursoDisponible(recurso(EstadoRecurso.MANTENIMIENTO)));
		assertThrows(ReglaNegocioException.class, () -> validador.exigirRecursoDisponible(recurso(EstadoRecurso.INACTIVO)));
	}

	@Test
	void solapamientoConUnaAprobadaEsConflicto() { // P06
		repo.guardar(reserva(0, 2, EstadoReserva.APROBADA));
		Reserva candidata = repo.guardar(reserva(1, 3, EstadoReserva.PENDIENTE));
		assertThrows(ReglaNegocioException.class, () -> validador.exigirSinConflicto(candidata));
	}

	@Test
	void reservasConsecutivasNoSonConflicto() { // EX05: 08-10 y 10-12
		repo.guardar(reserva(0, 2, EstadoReserva.APROBADA));
		Reserva candidata = repo.guardar(reserva(2, 4, EstadoReserva.PENDIENTE));
		assertDoesNotThrow(() -> validador.exigirSinConflicto(candidata));
	}

	@Test
	void pendientesCanceladasYOtroRecursoNoBloquean() { // D5
		repo.guardar(reserva(0, 2, EstadoReserva.PENDIENTE));
		repo.guardar(reserva(0, 2, EstadoReserva.CANCELADA));
		repo.guardar(new Reserva(0, 2, 9, OCHO, OCHO.plusHours(2), "Otro recurso", EstadoReserva.APROBADA, OCHO));
		Reserva candidata = repo.guardar(reserva(0, 2, EstadoReserva.PENDIENTE));
		assertDoesNotThrow(() -> validador.exigirSinConflicto(candidata));
	}
}
