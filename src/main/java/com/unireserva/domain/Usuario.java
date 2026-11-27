package com.unireserva.domain;

/** Usuario del sistema (§5). Inmutable: cambiar el rol es guardar otro registro con el mismo id. */
public record Usuario(long id, String nombre, String correo, Rol rol, boolean activo) {

	public Usuario {
		if (nombre == null || nombre.isBlank())
			throw new ReglaNegocioException("El nombre es obligatorio");
		if (correo == null || correo.isBlank())
			throw new ReglaNegocioException("El correo es obligatorio");
		if (rol == null)
			throw new ReglaNegocioException("El rol es obligatorio");
	}
}
