document.addEventListener('DOMContentLoaded', () => {
    // Función para crear temporizador
    function createTimer(selectId, btnId, counterId) {
        const select = document.getElementById(selectId);
        const btn = document.getElementById(btnId);
        const counter = document.getElementById(counterId);
        const btnText = btn.querySelector('.btn-text');
        let selectedTime = 0;
        let interval;
        let remainingTime = 0;

        select.addEventListener('change', (e) => {
            selectedTime = parseInt(e.target.value);
            remainingTime = selectedTime * 60;
            counter.textContent = formatTime(remainingTime);
            btn.disabled = selectedTime === 0;
        });

        btn.addEventListener('click', () => {
            if (selectedTime === 0) return;
            
            if (btnText.textContent === 'Iniciar') {
                startCounter();
                btnText.textContent = '';
                select.disabled = true;
            } else {
                clearInterval(interval);
                btnText.textContent = '';
                select.disabled = false;
            }
        });

        function startCounter() {
            clearInterval(interval);
            interval = setInterval(() => {
                remainingTime--;
                counter.textContent = formatTime(remainingTime);

                if (remainingTime <= 0) {
                    clearInterval(interval);
                    alert('¡Tiempo completado!');
                    btnText.textContent = 'Iniciar';
                    btn.disabled = true;
                    select.disabled = false;
                    select.value = '0';
                }
            }, 1000);
        }
    }

    // Crear los cuatro temporizadores
    createTimer('tiempoSelect', 'iniciarTiempo', 'contador');
    createTimer('tiempoSelect2', 'iniciarTiempo2', 'contador2');
    createTimer('tiempoSelect3', 'iniciarTiempo3', 'contador3');
    createTimer('tiempoSelect4', 'iniciarTiempo4', 'contador4');

    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
});