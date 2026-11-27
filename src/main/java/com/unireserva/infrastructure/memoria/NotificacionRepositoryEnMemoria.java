package com.unireserva.infrastructure.memoria;

import java.util.ArrayList;
import java.util.List;

import com.unireserva.application.NotificacionRepository;
import com.unireserva.domain.Notificacion;

/** Persistencia simulada (§7) de avisos: una lista, porque solo se agregan y se filtran por usuario. */
public class NotificacionRepositoryEnMemoria implements NotificacionRepository {

	private final List<Notificacion> notificaciones = new ArrayList<>();

	@Override
	public Notificacion guardar(Notificacion n) {
		Notificacion guardada = new Notificacion(notificaciones.size() + 1, n.usuarioId(), n.reservaId(),
				n.mensaje(), n.fecha(), n.estadoReserva());
		notificaciones.add(guardada);
		return guardada;
	}

	@Override
	public List<Notificacion> listarPorUsuario(long usuarioId) {
		return notificaciones.stream().filter(n -> n.usuarioId() == usuarioId).toList();
	}
}
