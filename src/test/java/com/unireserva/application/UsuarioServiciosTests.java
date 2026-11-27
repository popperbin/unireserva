package com.unireserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;
import com.unireserva.infrastructure.memoria.UsuarioRepositoryEnMemoria;

class UsuarioServiciosTests {

	private final UsuarioRepository repo = new UsuarioRepositoryEnMemoria();
	private final ControlAcceso acceso = new ControlAcceso(repo);
	private final UsuarioComandos comandos = new UsuarioComandos(repo, acceso);
	private final UsuarioConsultas consultas = new UsuarioConsultas(repo, acceso);
	private final long admin = repo.guardar(new Usuario(0, "Ana", "ana@u.edu", Rol.ADMINISTRADOR, true)).id();

	@Test
	void elAdministradorRegistraUnUsuarioActivo() { // P01
		long id = comandos.registrarUsuario(admin, "Sol", "sol@u.edu", Rol.SOLICITANTE);
		Usuario sol = repo.buscarPorId(id).orElseThrow();
		assertEquals(Rol.SOLICITANTE, sol.rol());
		assertEquals(true, sol.activo());
	}

	@Test
	void asignaRolYConsultaUsuarios() { // EX01
		long id = comandos.registrarUsuario(admin, "Sol", "sol@u.edu", Rol.SOLICITANTE);
		comandos.asignarRol(admin, id, Rol.RESPONSABLE);
		assertEquals(Rol.RESPONSABLE, repo.buscarPorId(id).orElseThrow().rol());
		assertEquals(2, consultas.consultarUsuarios(admin).size());
	}

	@Test
	void soloElAdministradorGestionaUsuarios() { // EX01
		long sol = comandos.registrarUsuario(admin, "Sol", "sol@u.edu", Rol.SOLICITANTE);
		assertThrows(AccesoDenegadoException.class, () -> comandos.registrarUsuario(sol, "X", "x@u.edu", Rol.ADMINISTRADOR));
		assertThrows(AccesoDenegadoException.class, () -> consultas.consultarUsuarios(sol));
	}

	@Test
	void asignarRolAUnUsuarioInexistenteFalla() { // EX08
		assertThrows(ReglaNegocioException.class, () -> comandos.asignarRol(admin, 99, Rol.RESPONSABLE));
	}

	@Test
	void faltaDeDatosObligatoriosFalla() {
		assertThrows(ReglaNegocioException.class, () -> comandos.registrarUsuario(admin, " ", "a@u.edu", Rol.SOLICITANTE));
	}
}
