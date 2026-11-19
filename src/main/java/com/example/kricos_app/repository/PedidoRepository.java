package com.example.kricos_app.repository;

import com.example.kricos_app.model.Pedido;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PedidoRepository {
    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1000);

    public Pedido save(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId("KR-" + secuencia.getAndIncrement());
        }
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    public Optional<Pedido> findById(String id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public List<Pedido> findAll() {
        List<Pedido> lista = new ArrayList<>(pedidos.values());
        lista.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));
        return lista;
    }
}
