package com.unireserva.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DominioTests {

	private static final LocalDateTime DIA = LocalDateTime.of(2026, 12, 1, 0, 0);

	private static Reserva reserva(long recursoId, int desde, int hasta) {
		return new Reserva(0, 1, recursoId, DIA.plusHours(desde), DIA.plusHours(hasta),
				"Clase", EstadoReserva.PENDIENTE, DIA);
	}

	@Test
	void rechazaFinAnteriorOIgualAlInicio() { // P04
		assertThrows(ReglaNegocioException.class, () -> reserva(1, 10, 8));
		assertThrows(ReglaNegocioException.class, () -> reserva(1, 10, 10));
	}

	@Test
	void exigeMotivo() {
		assertThrows(ReglaNegocioException.class, () -> new Reserva(0, 1, 1,
				DIA.plusHours(8), DIA.plusHours(10), " ", EstadoReserva.PENDIENTE, DIA));
	}

	@Test
	void detectaSolapamientoYPermiteConsecutivas() { // P06, EX05
		Reserva ochoADiez = reserva(1, 8, 10);

		assertTrue(ochoADiez.seSolapaCon(reserva(1, 9, 11)));
		assertTrue(ochoADiez.seSolapaCon(reserva(1, 7, 12)));
		assertFalse(ochoADiez.seSolapaCon(reserva(1, 10, 12)));
		assertFalse(ochoADiez.seSolapaCon(reserva(1, 6, 8)));
		assertFalse(ochoADiez.seSolapaCon(reserva(2, 9, 11)));
	}

	@Test
	void cambiaDeEstadoSinModificarElOriginal() {
		Reserva pendiente = reserva(1, 8, 10);
		Reserva aprobada = pendiente.cambiarA(EstadoReserva.APROBADA);

		assertEquals(EstadoReserva.PENDIENTE, pendiente.estado());
		assertEquals(EstadoReserva.APROBADA, aprobada.estado());
		assertEquals(EstadoReserva.FINALIZADA, aprobada.cambiarA(EstadoReserva.FINALIZADA).estado());
	}

	@Test
	void unaCanceladaNoSeReaprueba() { // EX04, §6
		Reserva cancelada = reserva(1, 8, 10).cambiarA(EstadoReserva.CANCELADA);

		assertThrows(ReglaNegocioException.class, () -> cancelada.cambiarA(EstadoReserva.APROBADA));
	}

	@Test
	void soloSePermitenLasTransicionesDelDiseno() { // EX04
		int permitidas = 0;
		for (EstadoReserva desde : EstadoReserva.values())
			for (EstadoReserva hacia : EstadoReserva.values())
				if (desde.puedePasarA(hacia))
					permitidas++;

		assertEquals(5, permitidas);
		assertTrue(EstadoReserva.APROBADA.puedePasarA(EstadoReserva.CANCELADA));
		assertFalse(EstadoReserva.PENDIENTE.puedePasarA(EstadoReserva.FINALIZADA));
		assertFalse(EstadoReserva.RECHAZADA.puedePasarA(EstadoReserva.APROBADA));
	}

	@Test
	void usuarioYRecursoExigenSusDatosBasicos() {
		assertThrows(ReglaNegocioException.class, () -> new Usuario(0, "", "a@u.edu", Rol.SOLICITANTE, true));
		assertThrows(ReglaNegocioException.class, () -> new Usuario(0, "Ana", "a@u.edu", null, true));
		assertThrows(ReglaNegocioException.class,
				() -> new Recurso(0, "Lab 1", null, "", "Bloque A", EstadoRecurso.DISPONIBLE));
	}
}
