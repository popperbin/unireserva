package com.unireserva.application;

import java.util.List;

import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;

/** Consultas de usuarios (§4.1, CQRS): solo lectura, para el administrador (D4). */
public class UsuarioConsultas {

	private final UsuarioRepository usuarios;
	private final ControlAcceso acceso;

	public UsuarioConsultas(UsuarioRepository usuarios, ControlAcceso acceso) {
		this.usuarios = usuarios;
		this.acceso = acceso;
	}

	public List<Usuario> consultarUsuarios(long actorId) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR);
		return usuarios.listar();
	}
}
