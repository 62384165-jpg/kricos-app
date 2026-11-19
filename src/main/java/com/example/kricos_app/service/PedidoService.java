package com.example.kricos_app.service;

import com.example.kricos_app.dto.CotizacionResponse;
import com.example.kricos_app.dto.PedidoRequest;
import com.example.kricos_app.dto.PedidoResponse;
import com.example.kricos_app.model.EstadoPedido;
import java.util.List;

public interface PedidoService {
    PedidoResponse registrar(PedidoRequest solicitud);
    CotizacionResponse calcularTotal(PedidoRequest solicitud);
    PedidoResponse obtenerPorId(String id);
    List<PedidoResponse> listarPedidos(String estado);
    PedidoResponse cambiarEstado(String id, EstadoPedido nuevoEstado);
}
