package com.example.kricos_app.dto;

import java.util.List;

public record PedidoRequest(
    String cliente,
    String telefono,
    String tipoEntrega,
    String direccion,
    List<ItemPedidoRequest> items
) {}
