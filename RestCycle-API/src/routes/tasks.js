const express = require('express');
const router = express.Router();
const taskController = require('../controllers/taskController');

// Rutas de Gamificación

// GET endpoints
router.get('/child/:child_id', taskController.getTasksByChild); // Ver las tareas si soy hijo
router.get('/parent/:parent_id', taskController.getTasksByParent); // Ver las tareas si soy padre

// POST endpoints
router.post('/create', taskController.createTask); // El papá crea una tarea nueva
router.post('/:task_id/complete', taskController.completeTask); // El hijo da click en "Hecho"
router.post('/:task_id/approve', taskController.approveTask); // El papá dice "Sí, lo hizo bien"
router.post('/:task_id/reject', taskController.rejectTask); // El papá dice "No, vuelve a intentarlo"

module.exports = router;
