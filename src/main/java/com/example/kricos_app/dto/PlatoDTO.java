package com.example.kricos_app.dto;

import java.math.BigDecimal;

public record PlatoDTO(
    Long id,
    String categoria,
    String nombre,
    BigDecimal precio,
    String descripcion,
    boolean disponible
) {}
