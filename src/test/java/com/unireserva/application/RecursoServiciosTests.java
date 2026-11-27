package com.unireserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.domain.Usuario;
import com.unireserva.infrastructure.memoria.RecursoRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.UsuarioRepositoryEnMemoria;

class RecursoServiciosTests {

	private final UsuarioRepository usuarios = new UsuarioRepositoryEnMemoria();
	private final RecursoRepository repo = new RecursoRepositoryEnMemoria();
	private final ControlAcceso acceso = new ControlAcceso(usuarios);
	private final RecursoComandos comandos = new RecursoComandos(repo, acceso);
	private final RecursoConsultas consultas = new RecursoConsultas(repo, acceso);
	private final long admin = usuarios.guardar(new Usuario(0, "Ana", "ana@u.edu", Rol.ADMINISTRADOR, true)).id();
	private final long sol = usuarios.guardar(new Usuario(0, "Sol", "sol@u.edu", Rol.SOLICITANTE, true)).id();

	@Test
	void elAdministradorRegistraUnRecursoDisponible() { // P02
		long id = comandos.registrarRecurso(admin, "Sala 101", TipoRecurso.SALA, "30 puestos", "Bloque A");
		assertEquals(EstadoRecurso.DISPONIBLE, repo.buscarPorId(id).orElseThrow().estado());
	}

	@Test
	void modificarConservaElEstadoYCambiarEstadoLoFiltra() { // EX02
		long id = comandos.registrarRecurso(admin, "Sala 101", TipoRecurso.SALA, "30 puestos", "Bloque A");
		comandos.registrarRecurso(admin, "Proyector 1", TipoRecurso.PROYECTOR, "HDMI", "Bodega");
		comandos.cambiarEstado(admin, id, EstadoRecurso.MANTENIMIENTO);
		comandos.modificarRecurso(admin, id, "Laboratorio 1", TipoRecurso.LABORATORIO, "20 equipos", "Bloque B");

		Recurso r = repo.buscarPorId(id).orElseThrow();
		assertEquals(TipoRecurso.LABORATORIO, r.tipo());
		assertEquals(EstadoRecurso.MANTENIMIENTO, r.estado());
		assertEquals(2, consultas.consultarRecursos(sol).size());
		assertEquals("Proyector 1", consultas.consultarRecursosDisponibles(sol).get(0).nombre());
	}

	@Test
	void unSolicitanteNoGestionaRecursos() {
		assertThrows(AccesoDenegadoException.class,
				() -> comandos.registrarRecurso(sol, "Sala", TipoRecurso.SALA, "", ""));
	}

	@Test
	void modificarUnRecursoInexistenteFalla() { // EX08
		assertThrows(ReglaNegocioException.class, () -> comandos.cambiarEstado(admin, 99, EstadoRecurso.INACTIVO));
	}
}
