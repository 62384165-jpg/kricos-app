package com.example.kricos_app.exception;

import java.util.ArrayList;
import java.util.List;

public class ReglaNegocioException extends RuntimeException {
    private final List<String> detalles;
    private final boolean esConflicto;

    public ReglaNegocioException(String message) {
        super(message);
        this.detalles = new ArrayList<>();
        this.esConflicto = false;
    }

    public ReglaNegocioException(String message, List<String> detalles) {
        super(message);
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        this.esConflicto = false;
    }

    public ReglaNegocioException(String message, boolean esConflicto) {
        super(message);
        this.detalles = new ArrayList<>();
        this.esConflicto = esConflicto;
    }

    public List<String> getDetalles() { return detalles; }
    public boolean esConflicto() { return esConflicto; }
}
