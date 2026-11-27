package com.unireserva.infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Notificacion;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.domain.Usuario;

class RepositoriosEnMemoriaTests {

	@Test
	void usuariosYRecursosAsignanIdsYReemplazan() {
		var usuarios = new UsuarioRepositoryEnMemoria();
		Usuario ana = usuarios.guardar(new Usuario(0, "Ana", "a@u.edu", Rol.ADMINISTRADOR, true));
		usuarios.guardar(new Usuario(ana.id(), "Ana", "a@u.edu", Rol.RESPONSABLE, true));
		assertEquals(1, usuarios.listar().size());
		assertEquals(Rol.RESPONSABLE, usuarios.buscarPorId(1).orElseThrow().rol());

		var recursos = new RecursoRepositoryEnMemoria();
		recursos.guardar(new Recurso(0, "Sala", TipoRecurso.SALA, "", "", EstadoRecurso.DISPONIBLE));
		assertEquals(2, recursos.guardar(new Recurso(0, "Bus", TipoRecurso.VEHICULO, "", "", EstadoRecurso.DISPONIBLE)).id());
	}

	@Test
	void notificacionesSeFiltranPorUsuario() {
		var repo = new NotificacionRepositoryEnMemoria();
		LocalDateTime ahora = LocalDateTime.of(2026, 12, 1, 8, 0);
		repo.guardar(new Notificacion(0, 2, 1, "Aprobada", ahora, EstadoReserva.APROBADA));
		repo.guardar(new Notificacion(0, 3, 2, "Rechazada", ahora, EstadoReserva.RECHAZADA));
		assertEquals(1, repo.listarPorUsuario(2).size());
		assertEquals(2, repo.listarPorUsuario(3).get(0).id());
	}
}
