package com.unireserva.application;

import java.util.List;
import java.util.Optional;

import com.unireserva.domain.Usuario;

/** Puerto de persistencia de usuarios: los servicios dependen de esta interfaz, no de la coleccion. */
public interface UsuarioRepository {

	/** Con id 0 crea el registro y le asigna un id; con otro id lo reemplaza. Devuelve lo guardado. */
	Usuario guardar(Usuario usuario);

	Optional<Usuario> buscarPorId(long id);

	/** Copia de todos los registros, en orden de creacion. */
	List<Usuario> listar();
}
