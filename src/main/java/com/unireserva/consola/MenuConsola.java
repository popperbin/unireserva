package com.unireserva.consola;

import java.io.EOFException;
import java.io.IOException;

/**
 * Menu principal, adaptador de entrada: elige el usuario que actua (D3, no es autenticacion) y
 * delega en los submenus. No contiene reglas de negocio, permisos ni acceso a datos.
 */
public class MenuConsola {

	private final Entrada entrada;
	private final MenuAdministracion administracion;
	private final MenuReservas reservas;
	private final String bienvenida;

	public MenuConsola(Entrada entrada, MenuAdministracion administracion, MenuReservas reservas, String bienvenida) {
		this.entrada = entrada;
		this.administracion = administracion;
		this.reservas = reservas;
		this.bienvenida = bienvenida;
	}

	public void ejecutar() throws IOException {
		entrada.mostrar("UniReserva");
		entrada.mostrar(bienvenida);
		try {
			long actor = entrada.numero("Id de usuario:");
			while (true) {
				entrada.mostrar("""
						Usuario actual: %d
						1. Usuarios y recursos
						2. Reservas y reportes
						3. Cambiar de usuario
						0. Salir""".formatted(actor));
				switch (entrada.texto(">")) {
					case "1" -> administracion.ejecutar(actor);
					case "2" -> reservas.ejecutar(actor);
					case "3" -> actor = entrada.numero("Id de usuario:");
					case "0" -> {
						entrada.mostrar("Hasta luego.");
						return;
					}
					default -> entrada.mostrar("Opcion no valida.");
				}
			}
		} catch (EOFException e) {
			entrada.mostrar("Hasta luego.");
		}
	}
}
