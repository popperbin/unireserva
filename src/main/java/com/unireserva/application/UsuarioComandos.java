package com.unireserva.application;

import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;

/** Comandos de usuarios (§4.1, CQRS): solo el administrador los ejecuta (D4). */
public class UsuarioComandos {

	private final UsuarioRepository usuarios;
	private final ControlAcceso acceso;

	public UsuarioComandos(UsuarioRepository usuarios, ControlAcceso acceso) {
		this.usuarios = usuarios;
		this.acceso = acceso;
	}

	public long registrarUsuario(long actorId, String nombre, String correo, Rol rol) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR);
		return usuarios.guardar(new Usuario(0, nombre, correo, rol, true)).id();
	}

	public void asignarRol(long actorId, long usuarioId, Rol rol) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR);
		Usuario u = usuarios.buscarPorId(usuarioId)
				.orElseThrow(() -> new ReglaNegocioException("El usuario " + usuarioId + " no existe"));
		usuarios.guardar(new Usuario(u.id(), u.nombre(), u.correo(), rol, u.activo()));
	}
}
