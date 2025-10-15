// Función para mostrar solo una pantalla
function showScreen(interfaz) {
    // Ocultar todas las pantallas
    const screens = document.querySelectorAll(".screen");

    screens.forEach(screen => screen.classList.remove("active"));
    const screenActive  = document.getElementById(interfaz)
    screenActive.classList.add("active");
    
}

//Evento para ocultar y despletar la opcion de agregar hijos
function addScreen(interfaz) {
    const add = document.getElementById(interfaz)
    if (add.classList.contains("oculto")) {
        add.classList.remove("oculto")
        add.classList.add("visible")
        return
    } 
    if (add.classList.contains("visible")) {
        add.classList.remove("visible")
        add.classList.add("oculto")
        
    }
}




document.getElementById('miSwitch').addEventListener('change', function() {
    if (this.checked) {
        console.log('Switch activado');
        // Tu código aquí
    } else {
        console.log('Switch desactivado');
        // Tu código aquí
    }
});
