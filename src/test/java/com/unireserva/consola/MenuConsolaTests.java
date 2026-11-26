package com.unireserva.consola;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

class MenuConsolaTests {

	@Test
	void rechazaUnaOpcionDesconocidaYSaleConCero() throws IOException {
		String salida = ejecutarCon("9\n 0 \n");

		assertEquals(1, rechazos(salida));
		assertTrue(salida.contains("Hasta luego."));
	}

	@Test
	void terminaSiLaEntradaSeCierra() throws IOException {
		String salida = ejecutarCon("");

		assertEquals(0, rechazos(salida));
		assertTrue(salida.contains("Hasta luego."));
	}

	@Test
	void ignoraLaMarcaInicialDeUnaTuberiaDeWindows() throws IOException {
		String salida = ejecutarCon("﻿0\r\n");

		assertEquals(0, rechazos(salida));
		assertTrue(salida.contains("Hasta luego."));
	}

	private long rechazos(String salida) {
		return salida.split("Opcion no valida\\.", -1).length - 1;
	}

	private String ejecutarCon(String teclado) throws IOException {
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		new MenuConsola(new BufferedReader(new StringReader(teclado)), new PrintStream(salida)).ejecutar();
		return salida.toString();
	}

}
