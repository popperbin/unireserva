package com.unireserva;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.unireserva.application.ControlAcceso;
import com.unireserva.application.NotificacionRepository;
import com.unireserva.application.NotificadorReserva;
import com.unireserva.application.PublicadorReserva;
import com.unireserva.application.RecursoComandos;
import com.unireserva.application.RecursoConsultas;
import com.unireserva.application.RecursoRepository;
import com.unireserva.application.RegistroHistorial;
import com.unireserva.application.ReporteConsultas;
import com.unireserva.application.ReservaConsultas;
import com.unireserva.application.ReservaFacade;
import com.unireserva.application.ReservaRepository;
import com.unireserva.application.UsuarioComandos;
import com.unireserva.application.UsuarioConsultas;
import com.unireserva.application.UsuarioRepository;
import com.unireserva.application.ValidadorDisponibilidad;
import com.unireserva.consola.DatosDemo;
import com.unireserva.consola.Entrada;
import com.unireserva.consola.MenuAdministracion;
import com.unireserva.consola.MenuConsola;
import com.unireserva.consola.MenuReservas;
import com.unireserva.infrastructure.memoria.NotificacionRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.RecursoRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.ReservaRepositoryEnMemoria;
import com.unireserva.infrastructure.memoria.UsuarioRepositoryEnMemoria;

/** Punto de entrada. */
public class Main {

	public static void main(String[] args) throws IOException {
		armar(new BufferedReader(new InputStreamReader(System.in)), System.out).ejecutar();
	}

	/**
	 * Raiz de composicion (DI): el unico lugar que crea los objetos y entrega a cada uno sus
	 * dependencias por constructor. Las pruebas de consola la reutilizan para probar el armado real.
	 */
	public static MenuConsola armar(BufferedReader teclado, PrintStream salida) {
		UsuarioRepository usuarios = new UsuarioRepositoryEnMemoria();
		RecursoRepository recursos = new RecursoRepositoryEnMemoria();
		ReservaRepository reservas = new ReservaRepositoryEnMemoria();
		NotificacionRepository notificaciones = new NotificacionRepositoryEnMemoria();
		String bienvenida = DatosDemo.cargar(usuarios, recursos);

		ControlAcceso acceso = new ControlAcceso(usuarios);
		PublicadorReserva publicador = new PublicadorReserva(new RegistroHistorial(reservas));
		publicador.suscribir(new NotificadorReserva(notificaciones));
		ReservaFacade facade = new ReservaFacade(acceso, recursos, reservas,
				new ValidadorDisponibilidad(reservas), publicador);

		Entrada entrada = new Entrada(teclado, salida);
		MenuAdministracion administracion = new MenuAdministracion(entrada,
				new UsuarioComandos(usuarios, acceso), new UsuarioConsultas(usuarios, acceso),
				new RecursoComandos(recursos, acceso), new RecursoConsultas(recursos, acceso));
		MenuReservas menuReservas = new MenuReservas(entrada, facade,
				new ReservaConsultas(reservas, notificaciones, acceso),
				new ReporteConsultas(reservas, usuarios, recursos, acceso));
		return new MenuConsola(entrada, administracion, menuReservas, bienvenida);
	}
}
