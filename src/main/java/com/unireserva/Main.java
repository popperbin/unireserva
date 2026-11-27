package com.unireserva;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.unireserva.consola.MenuConsola;

/**
 * Punto de entrada y raiz de composicion: el unico lugar que crea los objetos y
 * entrega a cada uno sus dependencias por constructor. Los repositorios y
 * servicios se construiran aqui cuando existan.
 */
public class Main {

	public static void main(String[] args) throws IOException {
		BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
		new MenuConsola(entrada, System.out).ejecutar();
	}

}
