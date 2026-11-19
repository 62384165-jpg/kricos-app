const BASE = "/api";

export class ApiError extends Error {
  constructor(status, mensaje, detalles = []) {
    super(mensaje);
    this.status = status;
    this.mensaje = mensaje;
    this.detalles = detalles;
  }
}

function conCuerpo(metodo, datos) {
  return {
    method: metodo,
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(datos),
  };
}

async function peticion(ruta, opciones = {}) {
  let respuesta;
  try {
    respuesta = await fetch(BASE + ruta, {
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      ...opciones,
    });
  } catch (e) {
    throw new ApiError(0, "No se pudo conectar con el servidor. Verifica que la aplicación esté encendida.");
  }
  let datos = null;
  const texto = await respuesta.text();
  if (texto) {
    try {
      datos = JSON.parse(texto);
    } catch (e) {
      datos = null;
    }
  }
  if (!respuesta.ok) {
    throw new ApiError(
      respuesta.status,
      datos?.mensaje ?? `Error ${respuesta.status} del servidor.`,
      datos?.detalles ?? []
    );
  }
  return datos;
}

export const Api = {
  listarCategorias: () => peticion("/categorias"),
  listarPlatos: ({ categoria = "", q = "" } = {}) => {
    const params = new URLSearchParams();
    if (categoria) params.set("categoria", categoria);
    if (q) params.set("q", q);
    const consulta = params.toString();
    return peticion("/platos" + (consulta ? "?" + consulta : ""));
  },
  cotizar: (solicitud) => peticion("/pedidos/cotizacion", conCuerpo("POST", solicitud)),
  registrarPedido: (solicitud) => peticion("/pedidos", conCuerpo("POST", solicitud)),
  obtenerPedido: (id) => peticion(`/pedidos/${encodeURIComponent(id)}`),
  listarPedidos: (estado = "") => peticion("/pedidos" + (estado ? `?estado=${encodeURIComponent(estado)}` : "")),
  cambiarEstado: (id, estado) => peticion(`/pedidos/${encodeURIComponent(id)}/estado`, conCuerpo("PATCH", { estado })),
};
