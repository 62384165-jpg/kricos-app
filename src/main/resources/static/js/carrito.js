import { Api } from "./api.js";

let carrito = [];

const carritoBody = document.getElementById("carrito-items");
const carritoTabla = document.getElementById("carrito-tabla");
const resSubtotal = document.getElementById("res-subtotal");
const resDelivery = document.getElementById("res-delivery");
const resTotal = document.getElementById("res-total");
const itemsInput = document.getElementById("items-input");
const formPedido = document.getElementById("form-pedido");
const btnVerFormulario = document.getElementById("btn-ver-formulario");
const btnCerrarFormulario = document.getElementById("btn-cerrar-formulario");
const formularioPedidoDiv = document.getElementById("formulario-pedido");

window.agregarAlCarrito = function (idPlato, nombre, precio) {
  const existente = carrito.find(i => i.idPlato === idPlato);
  if (existente) {
    if (existente.cantidad < 20) {
      existente.cantidad++;
    } else {
      alert("Solo se pueden agregar hasta 20 unidades de un mismo plato.");
      return;
    }
  } else {
    carrito.push({ idPlato, nombre, precioUnitario: precio, cantidad: 1 });
  }
  renderizarCarrito();
};

window.quitarDelCarrito = function (index) {
  carrito.splice(index, 1);
  renderizarCarrito();
};

window.cambiarCantidad = function (index, delta) {
  const item = carrito[index];
  if (!item) return;
  item.cantidad += delta;
  if (item.cantidad < 1) {
    carrito.splice(index, 1);
  } else if (item.cantidad > 20) {
    item.cantidad = 20;
  }
  renderizarCarrito();
};

function renderizarCarrito() {
  const tbody = document.getElementById("carrito-items");
  tbody.innerHTML = "";

  let subtotal = 0;
  carrito.forEach((item, index) => {
    const itemSubtotal = item.precioUnitario * item.cantidad;
    subtotal += itemSubtotal;
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${escaparHtml(item.nombre)}</td>
      <td class="text-center">
        <button class="btn btn-sm btn-outline-secondary" onclick="cambiarCantidad(${index}, -1)">-</button>
        <span>${item.cantidad}</span>
        <button class="btn btn-sm btn-outline-secondary" onclick="cambiarCantidad(${index}, 1)">+</button>
      </td>
      <td class="text-end">${soles(item.precioUnitario)}</td>
      <td class="text-end">${soles(itemSubtotal)}</td>
      <td class="text-center">
        <button class="btn btn-sm btn-danger" onclick="quitarDelCarrito(${index})">Quitar</button>
      </td>
    `;
    tbody.appendChild(row);
  });

  const delivery = subtotal >= 40 ? 0 : 2;
  const total = subtotal + delivery;

  resSubtotal.textContent = soles(subtotal);
  resDelivery.textContent = soles(delivery);
  resTotal.textContent = soles(total);

  const tipoSelect = document.querySelector('select[name="tipoEntrega"]');
  const direccionInput = document.querySelector('input[name="direccion"]');
  const campoDireccion = document.getElementById("campo-direccion");

  if (carrito.length === 0) {
    carritoTabla.style.display = "none";
    document.querySelector(".table-responsive").style.display = "none";
    document.querySelector(".d-flex.justify-content-between.mt-3").style.display = "none";
    btnVerFormulario.style.display = "none";
  } else {
    carritoTabla.style.display = "";
    document.querySelector(".table-responsive").style.display = "";
    document.querySelector(".d-flex.justify-content-between.mt-3").style.display = "";
    btnVerFormulario.style.display = "";
  }

  itemsInput.value = JSON.stringify(carrito.map(i => ({ idPlato: i.idPlato, cantidad: i.cantidad })));

  tipoSelect.addEventListener("change", () => {
    if (tipoSelect.value === "DOMICILIO") {
      campoDireccion.style.display = "";
      direccionInput.required = true;
    } else {
      campoDireccion.style.display = "none";
      direccionInput.required = false;
      direccionInput.value = "";
    }
  });
}

btnVerFormulario.addEventListener("click", () => {
  formularioPedidoDiv.style.display = "";
  formularioPedidoDiv.scrollIntoView({ behavior: "smooth" });
});

btnCerrarFormulario.addEventListener("click", () => {
  formularioPedidoDiv.style.display = "none";
});

formPedido.addEventListener("submit", async (e) => {
  e.preventDefault();
  const btn = formPedido.querySelector('button[type="submit"]');
  btn.disabled = true;
  btn.textContent = "Enviando...";

  const items = carrito.map(i => ({ idPlato: i.idPlato, cantidad: i.cantidad }));
  const solicitud = {
    cliente: document.querySelector('input[name="cliente"]').value.trim(),
    telefono: document.querySelector('input[name="telefono"]').value.trim(),
    tipoEntrega: document.querySelector('select[name="tipoEntrega"]').value,
    direccion: document.querySelector('input[name="direccion"]').value.trim(),
    items,
  };

  try {
    const respuesta = await Api.registrarPedido(solicitud);
    mostrarConfirmacionPedido(respuesta);
    carrito = [];
    renderizarCarrito();
    formPedido.reset();
    formularioPedidoDiv.style.display = "none";
  } catch (error) {
    alert(error.mensaje || "Error al enviar el pedido");
  } finally {
    btn.disabled = false;
    btn.textContent = "Enviar pedido";
  }
});

function mostrarConfirmacionPedido(pedido) {
  const html = `
    <div class="alert alert-success text-center mt-3">
      <h4>¡Pedido enviado!</h4>
      <p><strong>Número de pedido:</strong> ${pedido.id}</p>
      <p><strong>Total:</strong> ${soles(pedido.total)}</p>
      <p>Estado: <span class="badge bg-info">${pedido.estado}</span></p>
    </div>`;
  formularioPedidoDiv.insertAdjacentHTML("afterend", html);
  setTimeout(() => {
    document.querySelector(".alert-success")?.remove();
  }, 5000);
}
