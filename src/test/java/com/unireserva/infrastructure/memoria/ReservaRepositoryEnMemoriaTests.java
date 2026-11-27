package com.unireserva.infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.HistorialReserva;
import com.unireserva.domain.Reserva;

class ReservaRepositoryEnMemoriaTests {

	private static final LocalDateTime HOY = LocalDateTime.of(2026, 12, 1, 8, 0);
	private final ReservaRepositoryEnMemoria repo = new ReservaRepositoryEnMemoria();

	private static Reserva nueva() {
		return new Reserva(0, 2, 1, HOY, HOY.plusHours(2), "Clase", EstadoReserva.PENDIENTE, HOY);
	}

	@Test
	void conIdCeroAsignaIdsConsecutivos() {
		assertEquals(1, repo.guardar(nueva()).id());
		assertEquals(2, repo.guardar(nueva()).id());
	}

	@Test
	void conIdExistenteReemplazaSinDuplicar() {
		Reserva guardada = repo.guardar(nueva());
		repo.guardar(guardada.cambiarA(EstadoReserva.APROBADA));
		assertEquals(1, repo.listar().size());
		assertEquals(EstadoReserva.APROBADA, repo.buscarPorId(guardada.id()).orElseThrow().estado());
	}

	@Test
	void buscarIdInexistenteDevuelveVacio() {
		assertTrue(repo.buscarPorId(99).isEmpty());
	}

	@Test
	void listarDevuelveUnaCopiaQueNoSeModifica() {
		repo.guardar(nueva());
		assertThrows(UnsupportedOperationException.class, () -> repo.listar().clear());
	}

	@Test
	void historialSeFiltraPorReservaYConservaElOrden() {
		repo.agregarHistorial(new HistorialReserva(0, 1, EstadoReserva.PENDIENTE, EstadoReserva.APROBADA, HOY, 7));
		repo.agregarHistorial(new HistorialReserva(0, 2, EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA, HOY, 7));
		repo.agregarHistorial(new HistorialReserva(0, 1, EstadoReserva.APROBADA, EstadoReserva.FINALIZADA, HOY, 7));

		var deLaUno = repo.historialDe(1);
		assertEquals(2, deLaUno.size());
		assertEquals(EstadoReserva.APROBADA, deLaUno.get(0).estadoNuevo());
		assertEquals(EstadoReserva.FINALIZADA, deLaUno.get(1).estadoNuevo());
		assertEquals(3, deLaUno.get(1).id());
	}
}
