require('dotenv').config();
const express = require('express');
const cors = require('cors');

const taskRoutes = require('./routes/tasks');

const app = express();
const PORT = process.env.PORT || 3001;

// Middlewares
app.use(cors());
app.use(express.json()); // Permitir que la API lea JSON

// Rutas
app.use('/api/tasks', taskRoutes);

// Endpoint de prueba ("Ping")
app.get('/', (req, res) => {
    res.json({ 
        success: true, 
        message: '¡API de Gamificación de Rest Cycle funcionando correctamente!',
        version: '1.0'
    });
});

// Iniciar servidor
app.listen(PORT, () => {
    console.log(`🚀 Servidor de Tareas corriendo en http://localhost:${PORT}`);
});
