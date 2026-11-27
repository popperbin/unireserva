package com.unireserva.consola;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Lectura y escritura de la consola. Solo convierte texto en numeros, fechas u opciones y vuelve
 * a preguntar si el formato es incorrecto; las reglas de negocio las validan los servicios.
 */
public class Entrada {

	private final BufferedReader teclado;
	private final PrintStream salida;

	public Entrada(BufferedReader teclado, PrintStream salida) {
		this.teclado = teclado;
		this.salida = salida;
	}

	public void mostrar(String texto) {
		salida.println(texto);
	}

	/** Lanza EOFException si la entrada se cerro (Ctrl+Z, Ctrl+D o fin de una tuberia). */
	public String texto(String etiqueta) throws IOException {
		salida.print(etiqueta + " ");
		String linea = teclado.readLine();
		if (linea == null)
			throw new EOFException();
		// ﻿: marca invisible que algunas terminales de Windows anteponen al texto enviado por tuberia.
		return linea.replace("﻿", "").strip();
	}

	public long numero(String etiqueta) throws IOException {
		while (true) {
			try {
				return Long.parseLong(texto(etiqueta));
			} catch (NumberFormatException e) {
				mostrar("Escriba un numero.");
			}
		}
	}

	public LocalDateTime fecha(String etiqueta) throws IOException {
		while (true) {
			try {
				return LocalDateTime.parse(texto(etiqueta + " (aaaa-mm-dd hh:mm):").replace(' ', 'T'));
			} catch (DateTimeParseException e) {
				mostrar("Fecha no valida. Ejemplo: 2026-12-01 08:00");
			}
		}
	}

	/** Muestra los valores de un enum numerados y devuelve el elegido. */
	public <E extends Enum<E>> E opcion(String etiqueta, E[] valores) throws IOException {
		for (int i = 0; i < valores.length; i++)
			mostrar((i + 1) + ". " + valores[i]);
		while (true) {
			long elegido = numero(etiqueta + ":");
			if (elegido >= 1 && elegido <= valores.length)
				return valores[(int) elegido - 1];
			mostrar("Opcion no valida.");
		}
	}
}
