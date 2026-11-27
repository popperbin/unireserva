package com.unireserva.domain;

/** Recurso reservable (§5). Inmutable, como Usuario. */
public record Recurso(long id, String nombre, TipoRecurso tipo, String descripcion,
		String ubicacion, EstadoRecurso estado) {

	public Recurso {
		if (nombre == null || nombre.isBlank())
			throw new ReglaNegocioException("El nombre del recurso es obligatorio");
		if (tipo == null || estado == null)
			throw new ReglaNegocioException("El tipo y el estado del recurso son obligatorios");
	}
}
