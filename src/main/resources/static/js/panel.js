import { Api } from "./api.js";

const selectEstado = document.getElementById("filtro-estado");
const tablaBody = document.getElementById("tabla-pedidos-body");
const btnActualizar = document.getElementById("btn-actualizar");

async function cargarPedidos() {
  const estado = selectEstado.value;
  try {
    const pedidos = await Api.listarPedidos(estado);
    tablaBody.innerHTML = "";
    if (pedidos.length === 0) {
      tablaBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No hay pedidos.</td></tr>';
      return;
    }
    pedidos.forEach(p => {
      const tr = document.createElement("tr");
      const badgeClase = {
        PENDIENTE: "bg-warning text-dark",
        EN_PREPARACION: "bg-info text-dark",
        LISTO: "bg-primary",
        ENTREGADO: "bg-success",
        CANCELADO: "bg-danger",
      }[p.estado] || "bg-secondary";

      const itemsHtml = p.items.map(i => `${escaparHtml(i.nombrePlato)} x${i.cantidad}`).join("<br>");

      const botones = botonesPorEstado(p.id, p.estado);

      tr.innerHTML = `
        <td><strong>${escaparHtml(p.id)}</strong></td>
        <td>${escaparHtml(p.cliente)}</td>
        <td>${escaparHtml(p.telefono)}</td>
        <td>${p.tipoEntrega === "DOMICILIO" ? "Delivery" : "Para llevar"}<br><small>${escaparHtml(p.direccion || "")}</small></td>
        <td>${itemsHtml}</td>
        <td>${soles(p.total)}</td>
        <td><span class="badge ${badgeClase}">${p.estado}</span><br><div class="btn-group btn-group-sm mt-1">${botones}</div></td>
      `;
      tablaBody.appendChild(tr);
    });

    tablaBody.querySelectorAll("button[data-accion]").forEach(btn => {
      btn.addEventListener("click", async () => {
        const id = btn.dataset.id;
        const accion = btn.dataset.accion;
        try {
          await Api.cambiarEstado(id, accion);
          cargarPedidos();
        } catch (e) {
          alert(e.mensaje);
        }
      });
    });
  } catch (e) {
    tablaBody.innerHTML = `<tr><td colspan="7" class="text-danger text-center">Error: ${escaparHtml(e.mensaje)}</td></tr>`;
  }
}

function botonesPorEstado(id, estadoActual) {
  const botones = [];
  const definiciones = {
    PENDIENTE: ["EN_PREPARACION", "CANCELADO"],
    EN_PREPARACION: ["LISTO", "CANCELADO"],
    LISTO: ["ENTREGADO"],
    ENTREGADO: [],
    CANCELADO: [],
  }[estadoActual] || [];

  const etiquetas = {
    EN_PREPARACION: "Preparar",
    LISTO: "Listo",
    ENTREGADO: "Entregar",
    CANCELADO: "Cancelar",
  };
  definiciones.forEach(est => {
    const clase = est === "CANCELADO" ? "btn-danger" : "btn-outline-success";
    botones.push(`<button class="btn btn-sm ${clase}" data-id="${escaparHtml(id)}" data-accion="${est}">${etiquetas[est]}</button>`);
  });
  return botones.join("");
}

btnActualizar.addEventListener("click", cargarPedidos);
selectEstado.addEventListener("change", cargarPedidos);

// Auto-refresh cada 10 segundos
setInterval(cargarPedidos, 10000);
cargarPedidos();
