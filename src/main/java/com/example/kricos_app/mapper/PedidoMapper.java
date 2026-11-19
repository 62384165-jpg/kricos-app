package com.example.kricos_app.mapper;

import com.example.kricos_app.dto.ItemPedidoResponse;
import com.example.kricos_app.dto.PedidoResponse;
import com.example.kricos_app.model.Pedido;

import java.util.stream.Collectors;

public class PedidoMapper {
    public static PedidoResponse aDTO(Pedido pedido) {
        if (pedido == null) return null;
        var itemsDTO = pedido.getItems().stream()
            .map(item -> new ItemPedidoResponse(
                item.getIdPlato(),
                item.getNombrePlato(),
                item.getPrecioUnitario(),
                item.getCantidad(),
                item.getSubtotal()
            ))
            .collect(Collectors.toList());

        return new PedidoResponse(
            pedido.getId(),
            pedido.getCliente(),
            pedido.getTelefono(),
            pedido.getTipoEntrega(),
            pedido.getDireccion(),
            pedido.getEstado(),
            itemsDTO,
            pedido.getSubtotal(),
            pedido.getCostoDelivery(),
            pedido.getTotal(),
            pedido.getFechaCreacion()
        );
    }
}
