package com.unireserva.application;

import java.util.List;
import java.util.Optional;

import com.unireserva.domain.Recurso;

/** Puerto de persistencia de recursos. Mismo contrato que UsuarioRepository. */
public interface RecursoRepository {

	Recurso guardar(Recurso recurso);

	Optional<Recurso> buscarPorId(long id);

	List<Recurso> listar();
}
