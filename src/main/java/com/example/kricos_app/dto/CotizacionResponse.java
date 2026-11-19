package com.example.kricos_app.dto;

import java.math.BigDecimal;
import java.util.List;

public record CotizacionResponse(
    List<ItemPedidoResponse> items,
    BigDecimal subtotal,
    BigDecimal costoDelivery,
    BigDecimal total
) {}
