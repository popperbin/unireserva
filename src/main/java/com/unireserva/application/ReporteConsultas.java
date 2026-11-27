package com.unireserva.application;

import java.time.Duration;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.unireserva.domain.EstadoReserva;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.Reserva;
import com.unireserva.domain.Rol;
import com.unireserva.domain.Usuario;

/**
 * Los cuatro reportes (§4.6, R10), calculados en cada llamada con los datos registrados.
 * Solo administrador y responsable (D4). Incluyen los valores en cero para que un dato vacio se vea.
 */
public class ReporteConsultas {

	private final ReservaRepository reservas;
	private final UsuarioRepository usuarios;
	private final RecursoRepository recursos;
	private final ControlAcceso acceso;

	public ReporteConsultas(ReservaRepository reservas, UsuarioRepository usuarios, RecursoRepository recursos,
			ControlAcceso acceso) {
		this.reservas = reservas;
		this.usuarios = usuarios;
		this.recursos = recursos;
		this.acceso = acceso;
	}

	/** Cantidad de reservas en cada estado. */
	public Map<EstadoReserva, Long> generarReporteReservasPorEstado(long actorId) {
		List<Reserva> todas = leer(actorId);
		Map<EstadoReserva, Long> reporte = new EnumMap<>(EstadoReserva.class);
		for (EstadoReserva e : EstadoReserva.values())
			reporte.put(e, todas.stream().filter(r -> r.estado() == e).count());
		return reporte;
	}

	/** Cantidad de solicitudes de cada usuario, en cualquier estado. */
	public Map<Usuario, Long> generarReporteReservasPorUsuario(long actorId) {
		List<Reserva> todas = leer(actorId);
		Map<Usuario, Long> reporte = new LinkedHashMap<>();
		for (Usuario u : usuarios.listar())
			reporte.put(u, todas.stream().filter(r -> r.usuarioId() == u.id()).count());
		return reporte;
	}

	/** Horas de reservas FINALIZADAS por recurso (D11). */
	public Map<Recurso, Double> generarReporteUtilizacion(long actorId) {
		List<Reserva> finalizadas = leer(actorId).stream().filter(r -> r.estado() == EstadoReserva.FINALIZADA).toList();
		Map<Recurso, Double> reporte = new LinkedHashMap<>();
		for (Recurso rec : recursos.listar())
			reporte.put(rec, finalizadas.stream().filter(r -> r.recursoId() == rec.id())
					.mapToLong(r -> Duration.between(r.fechaInicio(), r.fechaFin()).toMinutes()).sum() / 60.0);
		return reporte;
	}

	/** Recursos de mas a menos solicitudes, en cualquier estado; empates en orden de registro. */
	public List<Map.Entry<Recurso, Long>> generarReporteRecursosMasSolicitados(long actorId) {
		List<Reserva> todas = leer(actorId);
		return recursos.listar().stream()
				.map(rec -> Map.entry(rec, todas.stream().filter(r -> r.recursoId() == rec.id()).count()))
				.sorted(Comparator.comparing((Map.Entry<Recurso, Long> e) -> e.getValue()).reversed())
				.toList();
	}

	private List<Reserva> leer(long actorId) {
		acceso.exigir(actorId, Rol.ADMINISTRADOR, Rol.RESPONSABLE);
		return reservas.listar();
	}
}
