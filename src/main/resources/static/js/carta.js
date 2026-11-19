import { Api } from "./api.js";

const contCategorias = document.getElementById("categorias");
const contPlatos = document.getElementById("lista");
const mensajeVacio = document.getElementById("vacio");
const buscador = document.getElementById("buscador");

let categoriaActual = "";
let textoBusqueda = "";
let temporizador = null;
let ultimaPeticion = 0;
const platosVisibles = new Map();

function pintarCategorias(categorias) {
  contCategorias.innerHTML = "";
  categorias.forEach(function (cat) {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.textContent = cat;
    boton.className = "btn btn-outline-danger";
    if (cat === categoriaActual) {
      boton.classList.add("active");
    }
    boton.addEventListener("click", function () {
      categoriaActual = cat;
      textoBusqueda = "";
      buscador.value = "";
      contCategorias.querySelectorAll("button").forEach(function (b) {
        b.classList.toggle("active", b.textContent === cat);
      });
      cargarPlatos();
    });
    contCategorias.appendChild(boton);
  });
}

function pintarPlatos(platos) {
  contPlatos.innerHTML = "";
  platosVisibles.clear();
  platos.forEach(function (plato) {
    platosVisibles.set(plato.id, plato);
    const boton = plato.disponible
      ? `<button type="button" class="btn btn-warning btn-sm btn-agregar" data-id="${plato.id}">Agregar al pedido</button>`
      : `<button type="button" class="btn btn-secondary btn-sm" disabled>No disponible</button>`;

    contPlatos.insertAdjacentHTML("beforeend", `
      <div class="col-md-6 col-lg-4">
        <div class="plato">
          <div>
            <div class="d-flex justify-content-between">
              <h3>${escaparHtml(plato.nombre)}</h3>
              <span class="precio">${soles(plato.precio)}</span>
            </div>
            <p>${escaparHtml(plato.descripcion)}</p>
          </div>
          <div>${boton}</div>
        </div>
      </div>`);
  });
  mensajeVacio.classList.toggle("d-none", platos.length > 0);
}

function mostrarError(mensaje) {
  mensajeVacio.classList.add("d-none");
  contPlatos.innerHTML = `
    <div class="col-12">
      <div class="alert alert-danger d-flex justify-content-between align-items-center" role="alert">
        <span>${escaparHtml(mensaje)}</span>
        <button type="button" class="btn btn-sm btn-outline-danger" id="btn-reintentar">Reintentar</button>
      </div>
    </div>`;
  document.getElementById("btn-reintentar").addEventListener("click", iniciar);
}

async function cargarPlatos() {
  const numero = ++ultimaPeticion;
  try {
    // Con texto en el buscador se busca en toda la carta; si no, solo en la categoría elegida.
    const platos = textoBusqueda
      ? await Api.listarPlatos({ q: textoBusqueda })
      : await Api.listarPlatos({ categoria: categoriaActual });
    if (numero !== ultimaPeticion) return; // llegó una respuesta más nueva
    pintarPlatos(platos);
  } catch (error) {
    if (numero !== ultimaPeticion) return;
    mostrarError(error.mensaje || "No se pudo cargar la carta.");
  }
}

async function iniciar() {
  try {
    const categorias = await Api.listarCategorias();
    categoriaActual = categorias[0] || "";
    pintarCategorias(categorias);
  } catch (error) {
    mostrarError(error.mensaje || "No se pudo cargar la carta.");
    return;
  }
  await cargarPlatos();
}

contPlatos.addEventListener("click", function (e) {
  const boton = e.target.closest(".btn-agregar");
  if (!boton) return;
  const plato = platosVisibles.get(Number(boton.dataset.id));
  if (plato) window.agregarAlCarrito(plato.id, plato.nombre, Number(plato.precio));
});

buscador.addEventListener("input", function () {
  clearTimeout(temporizador);
  temporizador = setTimeout(function () {
    textoBusqueda = buscador.value.trim();
    cargarPlatos();
  }, 250);
});

iniciar();
