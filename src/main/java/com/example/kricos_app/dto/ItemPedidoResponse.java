package com.example.kricos_app.dto;

import java.math.BigDecimal;

public record ItemPedidoResponse(
    Long idPlato,
    String nombrePlato,
    BigDecimal precioUnitario,
    int cantidad,
    BigDecimal subtotal
) {}
