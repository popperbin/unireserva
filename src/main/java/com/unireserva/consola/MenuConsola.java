package com.unireserva.consola;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Adaptador de entrada: lee opciones y muestra resultados. No contiene reglas de
 * negocio, validaciones de dominio, permisos ni acceso a datos.
 */
public class MenuConsola {

	private final BufferedReader entrada;
	private final PrintStream salida;

	/** Recibe la entrada y la salida para poder probar el menu sin teclado. */
	public MenuConsola(BufferedReader entrada, PrintStream salida) {
		this.entrada = entrada;
		this.salida = salida;
	}

	public void ejecutar() throws IOException {
		salida.println("UniReserva - aun no hay operaciones disponibles.");
		while (true) {
			salida.println("0. Salir");
			salida.print("> ");
			String opcion = entrada.readLine();
			// null: la entrada se cerro (Ctrl+Z, Ctrl+D o fin de una tuberia).
			// ﻿: marca invisible que algunas terminales de Windows anteponen al texto enviado por tuberia.
			if (opcion == null || opcion.replace("﻿", "").strip().equals("0")) {
				salida.println("Hasta luego.");
				return;
			}
			salida.println("Opcion no valida.");
		}
	}

}
