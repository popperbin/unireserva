package com.unireserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;

class ControlAccesoTests {

	private final Map<Long, Usuario> datos = Map.of(
			1L, new Usuario(1, "Rosa", "rosa@u.edu", Rol.RESPONSABLE, true),
			2L, new Usuario(2, "Sol", "sol@u.edu", Rol.SOLICITANTE, true),
			3L, new Usuario(3, "Iris", "iris@u.edu", Rol.RESPONSABLE, false));

	// Solo lectura: el repositorio en memoria real es parte de la etapa 2.
	private final ControlAcceso acceso = new ControlAcceso(new UsuarioRepository() {
		public Usuario guardar(Usuario usuario) { throw new UnsupportedOperationException(); }
		public Optional<Usuario> buscarPorId(long id) { return Optional.ofNullable(datos.get(id)); }
		public List<Usuario> listar() { return List.copyOf(datos.values()); }
	});

	@Test
	void devuelveAlActorConRolPermitido() {
		assertEquals("Rosa", acceso.exigir(1, Rol.RESPONSABLE).nombre());
	}

	@Test
	void unSolicitanteNoPuedeAprobar() { // P08
		assertThrows(AccesoDenegadoException.class, () -> acceso.exigir(2, Rol.RESPONSABLE));
	}

	@Test
	void rechazaUsuariosInexistentesOInactivos() {
		assertThrows(AccesoDenegadoException.class, () -> acceso.exigir(99, Rol.RESPONSABLE));
		assertThrows(AccesoDenegadoException.class, () -> acceso.exigir(3, Rol.RESPONSABLE));
	}
}
