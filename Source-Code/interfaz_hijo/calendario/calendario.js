const monthNames = [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ];

        const dayNames = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

        let currentDate = new Date();
        let selectedDate = null;
        let events = {};

        function initCalendar() {
            createDayHeaders();
            updateCalendar();
            
            document.getElementById('prevBtn').addEventListener('click', () => {
                currentDate.setMonth(currentDate.getMonth() - 1);
                updateCalendar();
            });

            document.getElementById('nextBtn').addEventListener('click', () => {
                currentDate.setMonth(currentDate.getMonth() + 1);
                updateCalendar();
            });

            document.getElementById('eventInput').addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    addEvent();
                }
            });
        }

        function createDayHeaders() {
            const grid = document.getElementById('calendarGrid');
            dayNames.forEach(day => {
                const dayElement = document.createElement('div');
                dayElement.className = 'day-header';
                dayElement.textContent = day;
                grid.appendChild(dayElement);
            });
        }

        function updateCalendar() {
            const monthYear = document.getElementById('monthYear');
            monthYear.textContent = `${monthNames[currentDate.getMonth()]} ${currentDate.getFullYear()}`;

            const grid = document.getElementById('calendarGrid');
            // Clear existing days (keep headers)
            const existingDays = grid.querySelectorAll('.day');
            existingDays.forEach(day => day.remove());

            const firstDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
            const lastDay = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
            const startDate = new Date(firstDay);
            startDate.setDate(startDate.getDate() - firstDay.getDay());

            const today = new Date();

            for (let i = 0; i < 42; i++) {
                const cellDate = new Date(startDate);
                cellDate.setDate(startDate.getDate() + i);

                const dayElement = document.createElement('div');
                dayElement.className = 'day';
                dayElement.textContent = cellDate.getDate();

                if (cellDate.getMonth() !== currentDate.getMonth()) {
                    dayElement.classList.add('other-month');
                }

                if (cellDate.toDateString() === today.toDateString()) {
                    dayElement.classList.add('today');
                }

                const dateKey = `${cellDate.getFullYear()}-${cellDate.getMonth()}-${cellDate.getDate()}`;
                if (events[dateKey] && events[dateKey].length > 0) {
                    const indicator = document.createElement('div');
                    indicator.className = 'events-indicator';
                    dayElement.appendChild(indicator);
                }

                dayElement.addEventListener('click', () => selectDate(cellDate, dayElement));
                grid.appendChild(dayElement);
            }
        }

        function selectDate(date, element) {
            // Remove previous selection
            document.querySelectorAll('.day.selected').forEach(el => {
                el.classList.remove('selected');
            });

            // Add selection to clicked day
            element.classList.add('selected');
            selectedDate = date;

            showEventPanel(date);
        }

        function showEventPanel(date) {
            const panel = document.getElementById('eventPanel');
            const eventDate = document.getElementById('eventDate');
            const eventList = document.getElementById('eventList');

            eventDate.textContent = `${date.getDate()} de ${monthNames[date.getMonth()]} ${date.getFullYear()}`;
            
            const dateKey = `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`;
            const dayEvents = events[dateKey] || [];

            eventList.innerHTML = '';
            dayEvents.forEach((event, index) => {
                const li = document.createElement('li');
                li.className = 'event-item';
                li.innerHTML = `${event} <button onclick="removeEvent('${dateKey}', ${index})" style="float: right; background: none; border: none; color: #ff6b6b; cursor: pointer;">✕</button>`;
                eventList.appendChild(li);
            });

            panel.style.display = 'block';
        }

        function addEvent() {
            if (!selectedDate) return;

            const input = document.getElementById('eventInput');
            const eventText = input.value.trim();
            
            if (eventText) {
                const dateKey = `${selectedDate.getFullYear()}-${selectedDate.getMonth()}-${selectedDate.getDate()}`;
                
                if (!events[dateKey]) {
                    events[dateKey] = [];
                }
                
                events[dateKey].push(eventText);
                input.value = '';
                
                showEventPanel(selectedDate);
                updateCalendar();
            }
        }

        function removeEvent(dateKey, index) {
            events[dateKey].splice(index, 1);
            if (events[dateKey].length === 0) {
                delete events[dateKey];
            }
            showEventPanel(selectedDate);
            updateCalendar();
        }

        // Initialize calendar when page loads
        document.addEventListener('DOMContentLoaded', function() {
            initCalendar();
        });