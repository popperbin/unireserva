package com.unireserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.HistorialReserva;
import com.unireserva.domain.Notificacion;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.domain.Usuario;
import com.unireserva.infrastructure.memoria.NotificacionRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.RecursoRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.ReservaRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.UsuarioRepositoryEnMemoria;

/** Arma el sistema igual que Main: prueba la fachada con sus colaboradores reales. */
class ReservaFacadeTests {

	private static final LocalDateTime OCHO = LocalDateTime.of(2026, 12, 1, 8, 0);

	private final UsuarioRepository usuarios = new UsuarioRepositoryEnMemoria();
	private final RecursoRepository recursos = new RecursoRepositoryEnMemoria();
	private final ReservaRepository reservas = new ReservaRepositoryEnMemoria();
	private final NotificacionRepository notificaciones = new NotificacionRepositoryEnMemoria();
	private final ReservaFacade facade;

	private final long responsable = usuarios.guardar(new Usuario(0, "Rosa", "r@u.edu", Rol.RESPONSABLE, true)).id();
	private final long sol = usuarios.guardar(new Usuario(0, "Sol", "s@u.edu", Rol.SOLICITANTE, true)).id();
	private final long luis = usuarios.guardar(new Usuario(0, "Luis", "l@u.edu", Rol.SOLICITANTE, true)).id();
	private final long sala = recursos.guardar(new Recurso(0, "Sala 101", TipoRecurso.SALA, "", "", EstadoRecurso.DISPONIBLE)).id();

	ReservaFacadeTests() {
		PublicadorReserva publicador = new PublicadorReserva(new RegistroHistorial(reservas));
		publicador.suscribir(new NotificadorReserva(notificaciones));
		facade = new ReservaFacade(new ControlAcceso(usuarios), recursos, reservas,
				new ValidadorDisponibilidad(reservas), publicador);
	}

	private long crear(long usuario, int desde, int hasta) {
		return facade.crearReserva(usuario, sala, OCHO.plusHours(desde), OCHO.plusHours(hasta), "Clase");
	}

	private EstadoReserva estado(long id) {
		return reservas.buscarPorId(id).orElseThrow().estado();
	}

	@Test
	void reservaValidaQuedaPendienteSinHistorial() { // P03, D7
		long id = crear(sol, 0, 2);
		assertEquals(EstadoReserva.PENDIENTE, estado(id));
		assertEquals(sol, reservas.buscarPorId(id).orElseThrow().usuarioId());
		assertTrue(reservas.historialDe(id).isEmpty());
	}

	@Test
	void finAnteriorAlInicioSeRechaza() { // P04
		assertThrows(ReglaNegocioException.class, () -> crear(sol, 2, 1));
		assertTrue(reservas.listar().isEmpty());
	}

	@Test
	void apruebaUnaReservaDisponible() { // P05
		long id = crear(sol, 0, 2);
		facade.aprobarReserva(responsable, id);
		assertEquals(EstadoReserva.APROBADA, estado(id));
	}

	@Test
	void noApruebaUnaReservaEnConflictoPeroSiLaConsecutiva() { // P06, EX05
		long primera = crear(sol, 0, 2);
		long solapada = crear(luis, 1, 3);
		long consecutiva = crear(luis, 2, 4);
		facade.aprobarReserva(responsable, primera);

		assertThrows(ReglaNegocioException.class, () -> facade.aprobarReserva(responsable, solapada));
		assertEquals(EstadoReserva.PENDIENTE, estado(solapada));
		facade.aprobarReserva(responsable, consecutiva);
		assertEquals(EstadoReserva.APROBADA, estado(consecutiva));
	}

	@Test
	void noSeReservaNiApruebaUnRecursoEnMantenimiento() { // P07
		long id = crear(sol, 0, 2);
		recursos.guardar(new Recurso(sala, "Sala 101", TipoRecurso.SALA, "", "", EstadoRecurso.MANTENIMIENTO));
		assertThrows(ReglaNegocioException.class, () -> crear(sol, 3, 4));
		assertThrows(ReglaNegocioException.class, () -> facade.aprobarReserva(responsable, id));
	}

	@Test
	void unSolicitanteNoPuedeAprobar() { // P08
		long id = crear(sol, 0, 2);
		assertThrows(AccesoDenegadoException.class, () -> facade.aprobarReserva(sol, id));
		assertEquals(EstadoReserva.PENDIENTE, estado(id));
	}

	@Test
	void cadaCambioDeEstadoQuedaEnElHistorial() { // P09
		long aprobada = crear(sol, 0, 2);
		long rechazada = crear(sol, 2, 4);
		long cancelada = crear(sol, 4, 6);
		facade.aprobarReserva(responsable, aprobada);
		facade.finalizarReserva(responsable, aprobada);
		facade.rechazarReserva(responsable, rechazada);
		facade.cancelarReserva(sol, cancelada);

		List<HistorialReserva> h = reservas.historialDe(aprobada);
		assertEquals(List.of(EstadoReserva.APROBADA, EstadoReserva.FINALIZADA),
				h.stream().map(HistorialReserva::estadoNuevo).toList());
		assertEquals(EstadoReserva.PENDIENTE, h.get(0).estadoAnterior());
		assertEquals(responsable, h.get(0).usuarioResponsableId());
		assertEquals(EstadoReserva.RECHAZADA, reservas.historialDe(rechazada).get(0).estadoNuevo());
		assertEquals(sol, reservas.historialDe(cancelada).get(0).usuarioResponsableId());
	}

	@Test
	void soloElDuenoCancelaYUnaCanceladaNoSeReaprueba() { // D6, EX04 (R7)
		long id = crear(sol, 0, 2);
		assertThrows(AccesoDenegadoException.class, () -> facade.cancelarReserva(luis, id));
		facade.cancelarReserva(sol, id);
		assertThrows(ReglaNegocioException.class, () -> facade.aprobarReserva(responsable, id));
		assertEquals(1, reservas.historialDe(id).size());
	}

	@Test
	void avisaAlDuenoAlAprobarRechazarYCancelarPeroNoAlFinalizar() { // EX07
		long a = crear(sol, 0, 2);
		facade.aprobarReserva(responsable, a);
		facade.rechazarReserva(responsable, crear(sol, 2, 4));
		facade.cancelarReserva(sol, crear(sol, 4, 6));
		facade.finalizarReserva(responsable, a);

		assertEquals(List.of(EstadoReserva.APROBADA, EstadoReserva.RECHAZADA, EstadoReserva.CANCELADA),
				notificaciones.listarPorUsuario(sol).stream().map(Notificacion::estadoReserva).toList());
		assertTrue(notificaciones.listarPorUsuario(luis).isEmpty());
	}

	@Test
	void referenciasInexistentesFallan() { // EX08
		assertThrows(ReglaNegocioException.class, () -> facade.crearReserva(sol, 99, OCHO, OCHO.plusHours(1), "X"));
		assertThrows(ReglaNegocioException.class, () -> facade.aprobarReserva(responsable, 99));
		assertThrows(AccesoDenegadoException.class, () -> crear(99, 0, 1));
	}
}
