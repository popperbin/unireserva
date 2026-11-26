package com.unireserva.consola;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: lee opciones y muestra resultados. No contiene reglas de
 * negocio, validaciones de dominio, permisos ni acceso a datos.
 */
@Component
@Profile("consola")
class MenuConsola implements CommandLineRunner {

	@Override
	public void run(String... args) throws IOException {
		ejecutar(new BufferedReader(new InputStreamReader(System.in)), System.out);
	}

	/** Recibe la entrada y la salida para poder probar el menu sin teclado. */
	void ejecutar(BufferedReader entrada, PrintStream salida) throws IOException {
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
