package com.example.kricos_app.service;

import com.example.kricos_app.dto.CotizacionResponse;
import com.example.kricos_app.dto.ItemPedidoRequest;
import com.example.kricos_app.dto.PedidoRequest;
import com.example.kricos_app.dto.PedidoResponse;
import com.example.kricos_app.exception.RecursoNoEncontradoException;
import com.example.kricos_app.exception.ReglaNegocioException;
import com.example.kricos_app.mapper.PedidoMapper;
import com.example.kricos_app.model.EstadoPedido;
import com.example.kricos_app.model.ItemPedido;
import com.example.kricos_app.model.Pedido;
import com.example.kricos_app.model.Plato;
import com.example.kricos_app.model.TipoEntrega;
import com.example.kricos_app.repository.PedidoRepository;
import com.example.kricos_app.repository.PlatoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PlatoRepository platoRepository;

    private final BigDecimal costoDelivery;
    private final BigDecimal deliveryGratisDesde;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            PlatoRepository platoRepository,
            @Value("${kricos.delivery.costo:2.00}") BigDecimal costoDelivery,
            @Value("${kricos.delivery.gratis-desde:40.00}") BigDecimal deliveryGratisDesde) {
        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
        this.costoDelivery = costoDelivery;
        this.deliveryGratisDesde = deliveryGratisDesde;
    }

    @Override
    public PedidoResponse registrar(PedidoRequest solicitud) {
        if (solicitud == null) {
            throw new ReglaNegocioException("La solicitud de pedido es obligatoria.");
        }
        List<String> errores = new ArrayList<>();
        String cliente = solicitud.cliente() == null ? "" : solicitud.cliente().trim();
        if (cliente.length() < 2 || cliente.length() > 60) {
            errores.add("El nombre debe tener entre 2 y 60 caracteres.");
        }
        String telefono = normalizarTelefono(solicitud.telefono());
        if (!telefono.matches("9\\d{8}")) {
            errores.add("El celular debe tener 9 digitos y empezar con 9.");
        }
        TipoEntrega tipo = parsearTipoEntrega(solicitud.tipoEntrega(), errores);
        String direccion = solicitud.direccion() == null ? "" : solicitud.direccion().trim();
        if (tipo == TipoEntrega.DOMICILIO && direccion.length() < 5) {
            errores.add("Para delivery debes indicar una direccion (minimo 5 caracteres).");
        }
        Map<Long, Integer> cantidades = validarItems(solicitud.items(), errores);
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException("El pedido tiene datos invalidos.", errores);
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setTelefono(telefono);
        pedido.setTipoEntrega(tipo);
        pedido.setDireccion(tipo == TipoEntrega.DOMICILIO ? direccion : "");
        pedido.setEstado(EstadoPedido.PENDIENTE);
        calcularImportesEnPedido(pedido, cantidades, tipo);

        Pedido guardado = pedidoRepository.save(pedido);
        return PedidoMapper.aDTO(guardado);
    }

    @Override
    public CotizacionResponse calcularTotal(PedidoRequest solicitud) {
        if (solicitud == null) {
            throw new ReglaNegocioException("La solicitud de cotizacion es obligatoria.");
        }
        List<String> errores = new ArrayList<>();
        TipoEntrega tipo = parsearTipoEntrega(solicitud.tipoEntrega(), errores);
        Map<Long, Integer> cantidades = validarItems(solicitud.items(), errores);
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException("La cotizacion tiene datos invalidos.", errores);
        }

        Pedido pedidoTemp = new Pedido();
        calcularImportesEnPedido(pedidoTemp, cantidades, tipo);
        return new CotizacionResponse( PedidoMapper.aDTO(pedidoTemp).items(), pedidoTemp.getSubtotal(), pedidoTemp.getCostoDelivery(), pedidoTemp.getTotal());

    }

    @Override
    public PedidoResponse obtenerPorId(String id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + id));
        return PedidoMapper.aDTO(pedido);
    }

    @Override
    public List<PedidoResponse> listarPedidos(String estadoStr) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        if (estadoStr != null && !estadoStr.isBlank()) {
            try {
                EstadoPedido estadoFiltro = EstadoPedido.valueOf(estadoStr.trim().toUpperCase());
                pedidos = pedidos.stream()
                        .filter(p -> p.getEstado() == estadoFiltro)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException("Estado de pedido invalido: " + estadoStr);
            }
        }
        return pedidos.stream().map(PedidoMapper::aDTO).collect(Collectors.toList());
    }

    @Override
    public PedidoResponse cambiarEstado(String id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + id));
        if (nuevoEstado == null) {
            throw new ReglaNegocioException("El nuevo estado es obligatorio.");
        }
        Set<EstadoPedido> permitidos = pedido.getEstado().siguientesPermitidos();
        if (!permitidos.contains(nuevoEstado)) {
            throw new ReglaNegocioException("No se puede cambiar el estado de " + pedido.getEstado() + " a " + nuevoEstado + ".", true);
        }
        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);
        return PedidoMapper.aDTO(actualizado);
    }

    private String normalizarTelefono(String tel) {
        if (tel == null) return "";
        String limpio = tel.replaceAll("\\s+", "").replace("+51", "");
        return limpio;
    }

    private TipoEntrega parsearTipoEntrega(String tipoStr, List<String> errores) {
        if (tipoStr == null || tipoStr.isBlank()) {
            errores.add("El tipo de entrega es obligatorio (DOMICILIO o LLEVAR).");
            return TipoEntrega.DOMICILIO;
        }
        try {
            return TipoEntrega.valueOf(tipoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            errores.add("Tipo de entrega invalido. Use DOMICILIO o LLEVAR.");
            return TipoEntrega.DOMICILIO;
        }
    }

    private Map<Long, Integer> validarItems(List<ItemPedidoRequest> itemsReq, List<String> errores) {
        if (itemsReq == null || itemsReq.isEmpty()) {
            errores.add("El pedido debe contener al menos un plato.");
            return Collections.emptyMap();
        }
        Map<Long, Integer> acumulador = new LinkedHashMap<>();
        for (ItemPedidoRequest item : itemsReq) {
            if (item == null || item.idPlato() == null) {
                errores.add("Cada item debe tener un id de plato valido.");
                continue;
            }
            int cantidad = item.cantidad();
            if (cantidad < 1 || cantidad > 20) {
                errores.add("La cantidad por plato debe estar entre 1 y 20.");
                continue;
            }
            acumulador.put(item.idPlato(), acumulador.getOrDefault(item.idPlato(), 0) + cantidad);
        }
        return acumulador;
    }

    private void calcularImportesEnPedido(Pedido pedido, Map<Long, Integer> cantidades, TipoEntrega tipo) {
        List<ItemPedido> lineas = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : cantidades.entrySet()) {
            Long idPlato = e.getKey();
            int cant = e.getValue();
            Plato plato = platoRepository.findById(idPlato)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el plato con id " + idPlato));
            if (!plato.isDisponible()) {
                throw new ReglaNegocioException("El plato \"" + plato.getNombre() + "\" no esta disponible.");
            }
            ItemPedido linea = new ItemPedido(plato.getId(), plato.getNombre(), plato.getPrecio(), cant);
            lineas.add(linea);
            subtotal = subtotal.add(linea.getSubtotal());
        }
        BigDecimal delivery = BigDecimal.ZERO;
        if (tipo == TipoEntrega.DOMICILIO && subtotal.compareTo(deliveryGratisDesde) < 0) {
            delivery = costoDelivery;
        }
        pedido.setItems(lineas);
        pedido.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        pedido.setCostoDelivery(delivery.setScale(2, RoundingMode.HALF_UP));
        pedido.setTotal(subtotal.add(delivery).setScale(2, RoundingMode.HALF_UP));
    }
}
