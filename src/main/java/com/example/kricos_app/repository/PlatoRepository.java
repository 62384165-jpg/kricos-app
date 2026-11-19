package com.example.kricos_app.repository;

import com.example.kricos_app.model.Plato;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PlatoRepository {
    private final Map<Long, Plato> platos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public Plato save(Plato plato) {
        if (plato.getId() == null) {
            plato.setId(secuencia.getAndIncrement());
        }
        platos.put(plato.getId(), plato);
        return plato;
    }

    public Optional<Plato> findById(Long id) {
        return Optional.ofNullable(platos.get(id));
    }

    public List<Plato> findAll() {
        List<Plato> lista = new ArrayList<>(platos.values());
        lista.sort(Comparator.comparing(Plato::getId));
        return lista;
    }

    public List<String> findCategorias() {
        return platos.values().stream()
                .map(Plato::getCategoria)
                .distinct()
                .collect(Collectors.toList());
    }

    public void limpiar() {
        platos.clear();
        secuencia.set(1);
    }
}
