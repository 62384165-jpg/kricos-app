package com.example.kricos_app.controller;

import com.example.kricos_app.dto.PlatoDTO;
import com.example.kricos_app.service.PlatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlatoController {

    private final PlatoService platoService;

    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    @GetMapping("/platos")
    public ResponseEntity<List<PlatoDTO>> listarPlatos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(platoService.listarPlatos(categoria, q));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        return ResponseEntity.ok(platoService.listarCategorias());
    }

    @GetMapping("/platos/{id}")
    public ResponseEntity<PlatoDTO> obtenerPlatoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(platoService.obtenerPorId(id));
    }
}
