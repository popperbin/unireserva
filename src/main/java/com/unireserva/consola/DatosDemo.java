package com.unireserva.consola;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.unireserva.application.RecursoRepository;
import com.unireserva.application.UsuarioRepository;
import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.domain.Usuario;

/**
 * Datos ficticios para la demostracion (§7): se cargan al iniciar y se pierden al salir. Junto con
 * Main, es lo unico de la consola que usa repositorios, porque arma el programa y no atiende al usuario.
 */
public class DatosDemo {

	/** Devuelve la lista de usuarios creados, para que se sepa con que id entrar. */
	public static String cargar(UsuarioRepository usuarios, RecursoRepository recursos) {
		List<Usuario> creados = Stream.of(
				new Usuario(0, "Ana Admin", "admin@unireserva.edu", Rol.ADMINISTRADOR, true),
				new Usuario(0, "Raul Responsable", "responsable@unireserva.edu", Rol.RESPONSABLE, true),
				new Usuario(0, "Sofia Solicitante", "sofia@unireserva.edu", Rol.SOLICITANTE, true),
				new Usuario(0, "Lucas Solicitante", "lucas@unireserva.edu", Rol.SOLICITANTE, true))
				.map(usuarios::guardar).toList();
		Stream.of(
				new Recurso(0, "Sala 101", TipoRecurso.SALA, "30 puestos", "Bloque A", EstadoRecurso.DISPONIBLE),
				new Recurso(0, "Laboratorio de redes", TipoRecurso.LABORATORIO, "20 equipos", "Bloque B", EstadoRecurso.DISPONIBLE),
				new Recurso(0, "Proyector 3", TipoRecurso.PROYECTOR, "HDMI", "Bodega", EstadoRecurso.DISPONIBLE),
				new Recurso(0, "Auditorio principal", TipoRecurso.AUDITORIO, "200 puestos", "Edificio central", EstadoRecurso.DISPONIBLE),
				new Recurso(0, "Portatil 12", TipoRecurso.EQUIPO_COMPUTO, "En revision", "Soporte", EstadoRecurso.MANTENIMIENTO))
				.forEach(recursos::guardar);
		return "Usuarios de demostracion: " + creados.stream()
				.map(u -> u.id() + " " + u.nombre() + " (" + u.rol() + ")").collect(Collectors.joining(", "));
	}
}
