package com.example.kricos_app.service;

import com.example.kricos_app.dto.ItemPedidoRequest;
import com.example.kricos_app.dto.PedidoRequest;
import com.example.kricos_app.dto.PedidoResponse;
import com.example.kricos_app.exception.ReglaNegocioException;
import com.example.kricos_app.model.EstadoPedido;
import com.example.kricos_app.model.Plato;
import com.example.kricos_app.model.TipoEntrega;
import com.example.kricos_app.repository.PedidoRepository;
import com.example.kricos_app.repository.PlatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceImplTest {

    private PedidoRepository pedidoRepository;
    private PlatoRepository platoRepository;
    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        pedidoRepository = new PedidoRepository();
        platoRepository = new PlatoRepository();
        platoRepository.save(new Plato(1L, "Hamburguesas", "Royal", new BigDecimal("12.00"), "Pan, carne, huevo", true));
        platoRepository.save(new Plato(2L, "Bebidas", "Chicha morada", new BigDecimal("5.00"), "Refresco natural", true));

        pedidoService = new PedidoServiceImpl(pedidoRepository, platoRepository, new BigDecimal("2.00"), new BigDecimal("40.00"));
    }

    @Test
    void registrarPedidoExitosoConDelivery() {
        PedidoRequest req = new PedidoRequest(
                "Juan Pérez",
                "943692981",
                "DOMICILIO",
                "Jr. Lima 123",
                List.of(new ItemPedidoRequest(1L, 2))
        );

        PedidoResponse resp = pedidoService.registrar(req);

        assertNotNull(resp.id());
        assertEquals("Juan Pérez", resp.cliente());
        assertEquals("943692981", resp.telefono());
        assertEquals(TipoEntrega.DOMICILIO, resp.tipoEntrega());
        assertEquals(EstadoPedido.PENDIENTE, resp.estado());
        assertEquals(new BigDecimal("24.00"), resp.subtotal());
        assertEquals(new BigDecimal("2.00"), resp.costoDelivery());
        assertEquals(new BigDecimal("26.00"), resp.total());
    }

    @Test
    void registrarPedidoDeliveryGratis() {
        PedidoRequest req = new PedidoRequest(
                "María Gómez",
                "987654321",
                "DOMICILIO",
                "Av. Central 456",
                List.of(new ItemPedidoRequest(1L, 4)) // 4 * 12 = 48 (>= 40)
        );

        PedidoResponse resp = pedidoService.registrar(req);

        assertEquals(new BigDecimal("48.00"), resp.subtotal());
        assertEquals(new BigDecimal("0.00"), resp.costoDelivery());
        assertEquals(new BigDecimal("48.00"), resp.total());
    }

    @Test
    void registrarPedidoDatosInvalidos() {
        PedidoRequest req = new PedidoRequest(
                "J", // muy corto
                "12345", // celular inválido
                "DOMICILIO",
                "Cort", // dirección muy corta
                List.of() // sin items
        );

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> {
            pedidoService.registrar(req);
        });

        assertFalse(ex.getDetalles().isEmpty());
    }

    @Test
    void cambiarEstadoValido() {
        PedidoRequest req = new PedidoRequest(
                "Carlos Ruiz",
                "911222333",
                "LLEVAR",
                "",
                List.of(new ItemPedidoRequest(1L, 1))
        );
        PedidoResponse creado = pedidoService.registrar(req);

        PedidoResponse actualizado = pedidoService.cambiarEstado(creado.id(), EstadoPedido.EN_PREPARACION);
        assertEquals(EstadoPedido.EN_PREPARACION, actualizado.estado());
    }

    @Test
    void cambiarEstadoInvalidoGeneraConflicto() {
        PedidoRequest req = new PedidoRequest(
                "Ana Torres",
                "944555666",
                "LLEVAR",
                "",
                List.of(new ItemPedidoRequest(1L, 1))
        );
        PedidoResponse creado = pedidoService.registrar(req);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> {
            pedidoService.cambiarEstado(creado.id(), EstadoPedido.LISTO);
        });

        assertTrue(ex.esConflicto());
    }
}
