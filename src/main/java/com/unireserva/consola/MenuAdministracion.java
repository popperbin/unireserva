package com.unireserva.consola;

import java.io.IOException;

import com.unireserva.application.RecursoComandos;
import com.unireserva.application.RecursoConsultas;
import com.unireserva.application.UsuarioComandos;
import com.unireserva.application.UsuarioConsultas;
import com.unireserva.domain.AccesoDenegadoException;
import com.unireserva.domain.EstadoRecurso;
import com.unireserva.domain.Recurso;
import com.unireserva.domain.ReglaNegocioException;
import com.unireserva.domain.Rol;
import com.unireserva.domain.TipoRecurso;
import com.unireserva.domain.Usuario;

/** Submenu de usuarios (§4.1) y recursos (§4.2). Pide datos, llama al servicio y muestra el resultado o el error. */
public class MenuAdministracion {

	private static final String OPCIONES = """
			-- Usuarios y recursos --
			1. Registrar usuario
			2. Asignar rol
			3. Consultar usuarios
			4. Registrar recurso
			5. Modificar recurso
			6. Cambiar estado de recurso
			7. Consultar recursos
			8. Consultar recursos disponibles
			0. Volver""";

	private final Entrada entrada;
	private final UsuarioComandos usuarioComandos;
	private final UsuarioConsultas usuarioConsultas;
	private final RecursoComandos recursoComandos;
	private final RecursoConsultas recursoConsultas;

	public MenuAdministracion(Entrada entrada, UsuarioComandos usuarioComandos, UsuarioConsultas usuarioConsultas,
			RecursoComandos recursoComandos, RecursoConsultas recursoConsultas) {
		this.entrada = entrada;
		this.usuarioComandos = usuarioComandos;
		this.usuarioConsultas = usuarioConsultas;
		this.recursoComandos = recursoComandos;
		this.recursoConsultas = recursoConsultas;
	}

	public void ejecutar(long actor) throws IOException {
		while (true) {
			entrada.mostrar(OPCIONES);
			String opcion = entrada.texto(">");
			if (opcion.equals("0"))
				return;
			try {
				atender(actor, opcion);
			} catch (ReglaNegocioException | AccesoDenegadoException e) {
				entrada.mostrar("Error: " + e.getMessage());
			}
		}
	}

	private void atender(long actor, String opcion) throws IOException {
		switch (opcion) {
			case "1" -> entrada.mostrar("Usuario registrado con id " + usuarioComandos.registrarUsuario(actor,
					entrada.texto("Nombre:"), entrada.texto("Correo:"), entrada.opcion("Rol", Rol.values())));
			case "2" -> {
				usuarioComandos.asignarRol(actor, entrada.numero("Id del usuario:"), entrada.opcion("Rol", Rol.values()));
				entrada.mostrar("Rol asignado.");
			}
			case "3" -> usuarioConsultas.consultarUsuarios(actor).forEach(u -> entrada.mostrar(texto(u)));
			case "4" -> entrada.mostrar("Recurso registrado con id " + recursoComandos.registrarRecurso(actor,
					entrada.texto("Nombre:"), entrada.opcion("Tipo", TipoRecurso.values()),
					entrada.texto("Descripcion:"), entrada.texto("Ubicacion:")));
			case "5" -> {
				recursoComandos.modificarRecurso(actor, entrada.numero("Id del recurso:"), entrada.texto("Nombre:"),
						entrada.opcion("Tipo", TipoRecurso.values()), entrada.texto("Descripcion:"),
						entrada.texto("Ubicacion:"));
				entrada.mostrar("Recurso modificado.");
			}
			case "6" -> {
				recursoComandos.cambiarEstado(actor, entrada.numero("Id del recurso:"),
						entrada.opcion("Estado", EstadoRecurso.values()));
				entrada.mostrar("Estado cambiado.");
			}
			case "7" -> recursoConsultas.consultarRecursos(actor).forEach(r -> entrada.mostrar(texto(r)));
			case "8" -> recursoConsultas.consultarRecursosDisponibles(actor).forEach(r -> entrada.mostrar(texto(r)));
			default -> entrada.mostrar("Opcion no valida.");
		}
	}

	private static String texto(Usuario u) {
		return u.id() + ". " + u.nombre() + " <" + u.correo() + "> " + u.rol() + (u.activo() ? "" : " (inactivo)");
	}

	private static String texto(Recurso r) {
		return r.id() + ". " + r.nombre() + " [" + r.tipo() + "] " + r.ubicacion() + " - " + r.descripcion()
				+ " (" + r.estado() + ")";
	}
}
