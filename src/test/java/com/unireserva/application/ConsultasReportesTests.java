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
import com.unireserva.domain.Reserva;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.domain.Usuario;
import com.unireserva.infrastructure.memoria.NotificacionRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.RecursoRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.ReservaRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.UsuarioRepositoryEnMemoria;

class ConsultasReportesTests {

	private static final LocalDateTime OCHO = LocalDateTime.of(2026, 12, 1, 8, 0);

	private final UsuarioRepository usuarios = new UsuarioRepositoryEnMemoria();
	private final RecursoRepository recursos = new RecursoRepositoryEnMemoria();
	private final ReservaRepository reservas = new ReservaRepositoryEnMemoria();
	private final NotificacionRepository notificaciones = new NotificacionRepositoryEnMemoria();
	private final ControlAcceso acceso = new ControlAcceso(usuarios);
	private final ReservaConsultas consultas = new ReservaConsultas(reservas, notificaciones, acceso);
	private final ReporteConsultas reportes = new ReporteConsultas(reservas, usuarios, recursos, acceso);

	private final Usuario admin = usuarios.guardar(new Usuario(0, "Ana", "a@u.edu", Rol.ADMINISTRADOR, true));
	private final Usuario sol = usuarios.guardar(new Usuario(0, "Sol", "s@u.edu", Rol.SOLICITANTE, true));
	private final Usuario luis = usuarios.guardar(new Usuario(0, "Luis", "l@u.edu", Rol.SOLICITANTE, true));
	private final Recurso sala = recursos.guardar(new Recurso(0, "Sala", TipoRecurso.SALA, "", "", EstadoRecurso.DISPONIBLE));
	private final Recurso bus = recursos.guardar(new Recurso(0, "Bus", TipoRecurso.VEHICULO, "", "", EstadoRecurso.DISPONIBLE));

	private Reserva reserva(Usuario u, Recurso rec, int horas, EstadoReserva estado) {
		return reservas.guardar(new Reserva(0, u.id(), rec.id(), OCHO, OCHO.plusMinutes(horas * 60L), "Clase", estado, OCHO));
	}

	private void cargarDatos() {
		reserva(sol, sala, 2, EstadoReserva.FINALIZADA);
		reserva(sol, sala, 1, EstadoReserva.FINALIZADA);
		reserva(sol, bus, 3, EstadoReserva.APROBADA);
		reserva(luis, bus, 4, EstadoReserva.PENDIENTE);
		reserva(luis, bus, 1, EstadoReserva.CANCELADA);
	}

	@Test
	void unSolicitanteSoloVeSusReservas() { // EX03, R6
		cargarDatos();
		assertEquals(5, consultas.consultarReservas(admin.id()).size());
		assertEquals(3, consultas.consultarReservas(sol.id()).size());
		assertEquals(1, consultas.consultarReservasPorRecurso(sol.id(), bus.id()).size());
		assertEquals(List.of(), consultas.consultarReservasPorEstado(sol.id(), EstadoReserva.PENDIENTE));
		assertThrows(AccesoDenegadoException.class, () -> consultas.consultarReservasPorUsuario(sol.id(), luis.id()));
	}

	@Test
	void filtrosPorEstadoUsuarioYRecurso() { // EX03
		cargarDatos();
		assertEquals(2, consultas.consultarReservasPorEstado(admin.id(), EstadoReserva.FINALIZADA).size());
		assertEquals(2, consultas.consultarReservasPorUsuario(admin.id(), luis.id()).size());
		assertEquals(3, consultas.consultarReservasPorRecurso(admin.id(), bus.id()).size());
	}

	@Test
	void historialYAvisosSoloDelPropio() { // R6, EX07
		Reserva deLuis = reserva(luis, bus, 1, EstadoReserva.APROBADA);
		reservas.agregarHistorial(new HistorialReserva(0, deLuis.id(), EstadoReserva.PENDIENTE, EstadoReserva.APROBADA, OCHO, admin.id()));
		notificaciones.guardar(new Notificacion(0, luis.id(), deLuis.id(), "Aprobada", OCHO, EstadoReserva.APROBADA));

		assertEquals(1, consultas.consultarHistorial(luis.id(), deLuis.id()).size());
		assertThrows(AccesoDenegadoException.class, () -> consultas.consultarHistorial(sol.id(), deLuis.id()));
		assertEquals(1, consultas.consultarNotificaciones(luis.id()).size());
		assertTrue(consultas.consultarNotificaciones(sol.id()).isEmpty());
	}

	@Test
	void reportesConDatosRegistrados() { // P10
		cargarDatos();
		var porEstado = reportes.generarReporteReservasPorEstado(admin.id());
		assertEquals(2L, porEstado.get(EstadoReserva.FINALIZADA));
		assertEquals(0L, porEstado.get(EstadoReserva.RECHAZADA));

		var porUsuario = reportes.generarReporteReservasPorUsuario(admin.id());
		assertEquals(3L, porUsuario.get(sol));
		assertEquals(2L, porUsuario.get(luis));

		var utilizacion = reportes.generarReporteUtilizacion(admin.id());
		assertEquals(3.0, utilizacion.get(sala));
		assertEquals(0.0, utilizacion.get(bus)); // sus reservas no estan FINALIZADAS (D11)

		var masSolicitados = reportes.generarReporteRecursosMasSolicitados(admin.id());
		assertEquals(bus, masSolicitados.get(0).getKey());
		assertEquals(3L, masSolicitados.get(0).getValue());
	}

	@Test
	void reportesSinDatosDevuelvenCeros() { // EX10
		assertTrue(reportes.generarReporteReservasPorEstado(admin.id()).values().stream().allMatch(n -> n == 0));
		assertEquals(0.0, reportes.generarReporteUtilizacion(admin.id()).get(sala));
	}

	@Test
	void unSolicitanteNoVeReportes() { // D4
		assertThrows(AccesoDenegadoException.class, () -> reportes.generarReporteReservasPorEstado(sol.id()));
	}
}
