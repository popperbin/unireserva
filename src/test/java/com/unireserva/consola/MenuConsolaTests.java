package com.unireserva.consola;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

/** Prueba sin Spring ni base de datos. */
class MenuConsolaTests {

	@Test
	void rechazaUnaOpcionDesconocidaYSaleConCero() throws IOException {
		assertThat(ejecutarCon("9\n 0 \n"))
				.containsOnlyOnce("Opcion no valida.")
				.contains("Hasta luego.");
	}

	@Test
	void terminaSiLaEntradaSeCierra() throws IOException {
		assertThat(ejecutarCon(""))
				.doesNotContain("Opcion no valida.")
				.contains("Hasta luego.");
	}

	@Test
	void ignoraLaMarcaInicialDeUnaTuberiaDeWindows() throws IOException {
		assertThat(ejecutarCon("﻿0\r\n"))
				.doesNotContain("Opcion no valida.")
				.contains("Hasta luego.");
	}

	private String ejecutarCon(String teclado) throws IOException {
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		new MenuConsola().ejecutar(new BufferedReader(new StringReader(teclado)), new PrintStream(salida));
		return salida.toString();
	}

}
