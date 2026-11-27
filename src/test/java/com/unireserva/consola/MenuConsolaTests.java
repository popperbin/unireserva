package com.unireserva.consola;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.unireserva.Main;

/** Prueba la consola con el armado real de Main y los datos de demostracion. */
class MenuConsolaTests {

	// Ids de DatosDemo: 1 administradora, 2 responsable, 3 y 4 solicitantes; recurso 1 Sala 101, 5 en mantenimiento.

	@Test
	void rechazaUnaOpcionDesconocidaYSaleConCero() throws IOException {
		String salida = ejecutarCon("1\n9\n 0 \n");

		assertEquals(1, veces(salida, "Opcion no valida."));
		assertTrue(salida.contains("Hasta luego."));
	}

	@Test
	void terminaSiLaEntradaSeCierra() throws IOException {
		String salida = ejecutarCon("1\n2\n1\n");

		assertEquals(0, veces(salida, "Opcion no valida."));
		assertTrue(salida.contains("Hasta luego."));
	}

	@Test
	void ignoraLaMarcaInicialDeUnaTuberiaDeWindows() throws IOException {
		String salida = ejecutarCon("﻿1\r\n0\r\n");

		assertTrue(salida.contains("Usuario actual: 1"));
		assertEquals(0, veces(salida, "Opcion no valida."));
	}

	@Test
	void crearAprobarRechazarElSolapamientoVerHistorialYAviso() throws IOException { // etapa 3, P05, P06, P09, EX07
		String salida = ejecutarCon("""
				3
				2
				1
				1
				2026-12-01 08:00
				2026-12-01 10:00
				Clase de redes
				1
				1
				2026-12-01 09:00
				2026-12-01 11:00
				Tutoria
				0
				3
				2
				2
				2
				1
				2
				2
				10
				1
				0
				3
				3
				2
				11
				0
				0
				""");

		assertTrue(salida.contains("Reserva creada con id 1 (PENDIENTE)"));
		assertTrue(salida.contains("Reserva creada con id 2 (PENDIENTE)"));
		assertEquals(1, veces(salida, "Reserva aprobada."));
		assertTrue(salida.contains("Error: El recurso ya tiene una reserva aprobada en ese horario"));
		assertTrue(salida.contains("PENDIENTE -> APROBADA"));
		assertTrue(salida.contains("por el usuario 2"));
		assertTrue(salida.contains("Su reserva 1 fue APROBADA"));
	}

	@Test
	void muestraLosErroresDePermisosYDeDatos() throws IOException { // P04, P07, P08 por consola
		String salida = ejecutarCon("""
				3
				1
				3
				0
				2
				1
				5
				2026-12-01 08:00
				2026-12-01 10:00
				Clase
				1
				1
				2026-12-01 hoy
				2026-12-01 10:00
				2026-12-01 08:00
				Clase
				0
				0
				""");

		assertTrue(salida.contains("Error: El rol SOLICITANTE no puede realizar esta operacion"));
		assertTrue(salida.contains("Error: El recurso Portatil 12 esta MANTENIMIENTO"));
		assertTrue(salida.contains("Fecha no valida."));
		assertTrue(salida.contains("Error: La fecha final debe ser posterior a la inicial"));
	}

	@Test
	void muestraLosReportesAlResponsable() throws IOException { // P10 por consola
		String salida = ejecutarCon("2\n2\n12\n0\n0\n");

		assertTrue(salida.contains("-- Recursos mas solicitados --"));
		assertTrue(salida.contains("Sala 101: 0.0 h"));
	}

	private static long veces(String salida, String texto) {
		return salida.split(java.util.regex.Pattern.quote(texto), -1).length - 1;
	}

	private static String ejecutarCon(String teclado) throws IOException {
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		Main.armar(new BufferedReader(new StringReader(teclado)), new PrintStream(salida)).ejecutar();
		return salida.toString();
	}
}
