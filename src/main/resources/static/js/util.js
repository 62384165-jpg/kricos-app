function soles(numero) {
  return "S/. " + Number(numero).toFixed(2);
}

function escaparHtml(texto) {
  const div = document.createElement("div");
  div.textContent = texto == null ? "" : String(texto);
  return div.innerHTML;
}
