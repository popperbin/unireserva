package com.unireserva.infrastructure.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unireserva.application.ReservaRepository;
import com.unireserva.domain.HistorialReserva;
import com.unireserva.domain.Reserva;

/** Persistencia simulada (§7): las reservas en un mapa y su historial en una lista; se pierden al salir. */
public class ReservaRepositoryEnMemoria implements ReservaRepository {

	private final Map<Long, Reserva> reservas = new LinkedHashMap<>();
	private final List<HistorialReserva> historial = new ArrayList<>();
	private long ultimoId;
	private long ultimoHistorialId;

	@Override
	public Reserva guardar(Reserva r) {
		Reserva guardada = r.id() != 0 ? r
				: new Reserva(++ultimoId, r.usuarioId(), r.recursoId(), r.fechaInicio(), r.fechaFin(),
						r.motivo(), r.estado(), r.fechaCreacion());
		reservas.put(guardada.id(), guardada);
		return guardada;
	}

	@Override
	public Optional<Reserva> buscarPorId(long id) {
		return Optional.ofNullable(reservas.get(id));
	}

	@Override
	public List<Reserva> listar() {
		return List.copyOf(reservas.values());
	}

	@Override
	public HistorialReserva agregarHistorial(HistorialReserva c) {
		HistorialReserva guardado = new HistorialReserva(++ultimoHistorialId, c.reservaId(),
				c.estadoAnterior(), c.estadoNuevo(), c.fechaCambio(), c.usuarioResponsableId());
		historial.add(guardado);
		return guardado;
	}

	@Override
	public List<HistorialReserva> historialDe(long reservaId) {
		return historial.stream().filter(h -> h.reservaId() == reservaId).toList();
	}
}
