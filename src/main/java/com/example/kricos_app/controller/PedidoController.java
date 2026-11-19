package com.example.kricos_app.controller;

import com.example.kricos_app.dto.CotizacionResponse;
import com.example.kricos_app.dto.EstadoRequest;
import com.example.kricos_app.dto.PedidoRequest;
import com.example.kricos_app.dto.PedidoResponse;
import com.example.kricos_app.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> registrar(@RequestBody PedidoRequest solicitud) {
        PedidoResponse creado = pedidoService.registrar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/cotizacion")
    public ResponseEntity<CotizacionResponse> cotizar(@RequestBody PedidoRequest solicitud) {
        return ResponseEntity.ok(pedidoService.calcularTotal(solicitud));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(pedidoService.listarPedidos(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(@PathVariable String id, @RequestBody EstadoRequest request) {
        PedidoResponse actualizado = pedidoService.cambiarEstado(id, request.estado());
        return ResponseEntity.ok(actualizado);
    }
}
