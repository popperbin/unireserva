package com.unireserva.application;

import java.util.List;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;

/**
 * Permisos comprobados en la aplicacion, no en el menu (§4.1.4): aunque se llame al servicio
 * directamente, un rol no autorizado no pasa. Cada comando y consulta lo llama primero.
 */
public class ControlAcceso {

	private final UsuarioRepository usuarios;

	public ControlAcceso(UsuarioRepository usuarios) {
		this.usuarios = usuarios;
	}

	/** Devuelve el actor si existe, esta activo y tiene uno de los roles permitidos. */
	public Usuario exigir(long actorId, Rol... permitidos) {
		Usuario actor = usuarios.buscarPorId(actorId)
				.orElseThrow(() -> new AccesoDenegadoException("El usuario " + actorId + " no existe"));
		if (!actor.activo())
			throw new AccesoDenegadoException("El usuario " + actorId + " esta inactivo");
		if (!List.of(permitidos).contains(actor.rol()))
			throw new AccesoDenegadoException("El rol " + actor.rol() + " no puede realizar esta operacion");
		return actor;
	}
}
