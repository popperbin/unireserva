package com.unireserva.infrastructure.memoria;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.unireserva.application.RecursoRepository;
import com.unireserva.domain.Recurso;

/** Persistencia simulada (§7) de recursos: mapa en orden de creacion y contador de ids. */
public class RecursoRepositoryEnMemoria implements RecursoRepository {

	private final Map<Long, Recurso> recursos = new LinkedHashMap<>();
	private long ultimoId;

	@Override
	public Recurso guardar(Recurso r) {
		Recurso guardado = r.id() != 0 ? r
				: new Recurso(++ultimoId, r.nombre(), r.tipo(), r.descripcion(), r.ubicacion(), r.estado());
		recursos.put(guardado.id(), guardado);
		return guardado;
	}

	@Override
	public Optional<Recurso> buscarPorId(long id) {
		return Optional.ofNullable(recursos.get(id));
	}

	@Override
	public List<Recurso> listar() {
		return List.copyOf(recursos.values());
	}
}
