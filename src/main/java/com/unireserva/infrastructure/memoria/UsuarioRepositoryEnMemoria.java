package com.unireserva.infrastructure.memoria;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unireserva.application.UsuarioRepository;
import com.unireserva.domain.Usuario;

/** Persistencia simulada (§7) de usuarios: mapa en orden de creacion y contador de ids. */
public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

	private final Map<Long, Usuario> usuarios = new LinkedHashMap<>();
	private long ultimoId;

	@Override
	public Usuario guardar(Usuario u) {
		Usuario guardado = u.id() != 0 ? u : new Usuario(++ultimoId, u.nombre(), u.correo(), u.rol(), u.activo());
		usuarios.put(guardado.id(), guardado);
		return guardado;
	}

	@Override
	public Optional<Usuario> buscarPorId(long id) {
		return Optional.ofNullable(usuarios.get(id));
	}

	@Override
	public List<Usuario> listar() {
		return List.copyOf(usuarios.values());
	}
}
