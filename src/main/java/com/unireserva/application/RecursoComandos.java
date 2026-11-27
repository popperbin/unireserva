package com.unireserva.application;

import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;

/** Comandos de recursos (§4.2, CQRS): solo el administrador los ejecuta (D4). */
public class RecursoComandos {

	private final RecursoRepository recursos;
	private final ControlAcceso acceso;

	public RecursoComandos(RecursoRepository recursos, ControlAcceso acceso) {
		this.recursos = recursos;
		this.acceso = acceso;
	}

	/** Un recurso nuevo nace DISPONIBLE. */
	public long registrarRecurso(long actorId, String nombre, TipoRecurso tipo, String descripcion, String ubicacion) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR);
		return recursos.guardar(new Recurso(0, nombre, tipo, descripcion, ubicacion, EstadoRecurso.DISPONIBLE)).id();
	}

	/** Cambia los datos descriptivos; el estado se cambia con cambiarEstado. */
	public void modificarRecurso(long actorId, long recursoId, String nombre, TipoRecurso tipo,
			String descripcion, String ubicacion) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR);
		Recurso r = buscar(recursoId);
		recursos.guardar(new Recurso(r.id(), nombre, tipo, descripcion, ubicacion, r.estado()));
	}

	public void cambiarEstado(long actorId, long recursoId, EstadoRecurso estado) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR);
		Recurso r = buscar(recursoId);
		recursos.guardar(new Recurso(r.id(), r.nombre(), r.tipo(), r.descripcion(), r.ubicacion(), estado));
	}

	private Recurso buscar(long recursoId) {
		return recursos.buscarPorId(recursoId)
				.orElseThrow(() -> new ReglaNegocioException("El recurso " + recursoId + " no existe"));
	}
}
