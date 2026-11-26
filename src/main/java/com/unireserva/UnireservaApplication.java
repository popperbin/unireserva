package com.unireserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UnireservaApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(UnireservaApplication.class);
		// El perfil "consola" activa el menu interactivo. Las pruebas no pasan por main() y no lo activan.
		application.setAdditionalProfiles("consola");
		application.run(args);
	}

}
