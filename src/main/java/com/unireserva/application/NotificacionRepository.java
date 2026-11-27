package com.unireserva.application;

import java.util.List;

import com.unireserva.domain.Notificacion;

/** Puerto de persistencia de avisos. Solo se consultan los de un usuario. */
public interface NotificacionRepository {

	/** Con id 0 asigna un id. Devuelve lo guardado. */
	Notificacion guardar(Notificacion notificacion);

	List<Notificacion> listarPorUsuario(long usuarioId);
}
