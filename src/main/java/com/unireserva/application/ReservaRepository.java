package com.unireserva.application;

import java.util.List;
import java.util.Optional;

import com.unireserva.domain.HistorialReserva;
import com.unireserva.domain.Reserva;

/** Puerto de persistencia de reservas y de su historial, que solo existe junto a una reserva. */
public interface ReservaRepository {

	Reserva guardar(Reserva reserva);

	Optional<Reserva> buscarPorId(long id);

	List<Reserva> listar();

	/** Con id 0 asigna un id. Devuelve lo guardado. */
	HistorialReserva agregarHistorial(HistorialReserva cambio);

	/** Cambios de una reserva, en el orden en que ocurrieron. */
	List<HistorialReserva> historialDe(long reservaId);
}
