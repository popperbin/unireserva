package com.unireserva;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Prueba de arranque: requiere PostgreSQL en ejecucion (docker compose up -d --wait). */
@SpringBootTest
class UnireservaApplicationTests {

	@Autowired
	JdbcClient jdbc;

	@Autowired
	ApplicationContext contexto;

	@Test
	void arrancaYFlywayPreparaLaBaseDeDatos() {
		boolean existeHistorial = jdbc
				.sql("select to_regclass('flyway_schema_history') is not null")
				.query(Boolean.class)
				.single();

		assertThat(existeHistorial).isTrue();
	}

	@Test
	void lasPruebasNoActivanElMenuInteractivo() {
		assertThat(contexto.containsBean("menuConsola")).isFalse();
	}

}
