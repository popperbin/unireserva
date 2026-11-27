package com.unireserva.application;

import java.util.List;

import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.Rol;

/** Consultas de recursos (§4.2, CQRS): cualquier usuario activo las usa para elegir que reservar. */
public class RecursoConsultas {

	private final RecursoRepository recursos;
	private final ControlAcceso acceso;

	public RecursoConsultas(RecursoRepository recursos, ControlAcceso acceso) {
		this.recursos = recursos;
		this.acceso = acceso;
	}

	public List<Recurso> consultarRecursos(long actorId) {
		acceso.exigir(actorId, Rol.values());
		return recursos.listar();
	}

	public List<Recurso> consultarRecursosDisponibles(long actorId) {
		return consultarRecursos(actorId).stream().filter(r -> r.estado() == EstadoRecurso.DISPONIBLE).toList();
	}
}
