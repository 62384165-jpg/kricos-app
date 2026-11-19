package com.example.kricos_app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.kricos_app.model.EstadoPedido;
import com.example.kricos_app.model.TipoEntrega;

public record PedidoResponse(
    String id,
    String cliente,
    String telefono,
    TipoEntrega tipoEntrega,
    String direccion,
    EstadoPedido estado,
    List<ItemPedidoResponse> items,
    BigDecimal subtotal,
    BigDecimal costoDelivery,
    BigDecimal total,
    LocalDateTime fechaCreacion
) {}
