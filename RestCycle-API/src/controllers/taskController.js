const supabase = require('../config/supabaseClient');

// Crear una nueva tarea (Padre)
const createTask = async (req, res) => {
    try {
        const { padre_id, hijo_id, titulo, descripcion, recompensa_minutos } = req.body;

        if (!padre_id || !hijo_id || !titulo || !recompensa_minutos) {
            return res.status(400).json({ success: false, message: 'Faltan parámetros obligatorios' });
        }

        const { data, error } = await supabase
            .from('api_tareas_recompensas_hijo')
            .insert([{ padre_id, hijo_id, titulo, descripcion, recompensa_minutos }])
            .select();

        if (error) throw error;

        res.status(201).json({ success: true, message: 'Tarea creada con éxito', data: data[0] });
    } catch (error) {
        res.status(500).json({ success: false, message: 'Error al crear la tarea', error: error.message });
    }
};

// Listar tareas de un hijo
const getTasksByChild = async (req, res) => {
    try {
        const { child_id } = req.params;

        const { data, error } = await supabase
            .from('api_tareas_recompensas_hijo')
            .select('*')
            .eq('hijo_id', child_id)
            .order('fecha_creacion', { ascending: false });

        if (error) throw error;

        res.json({ success: true, data });
    } catch (error) {
        res.status(500).json({ success: false, message: 'Error al obtener tareas', error: error.message });
    }
};

// Listar tareas creadas por un padre
const getTasksByParent = async (req, res) => {
    try {
        const { parent_id } = req.params;

        const { data, error } = await supabase
            .from('api_tareas_recompensas_hijo')
            .select('*')
            .eq('padre_id', parent_id)
            .order('fecha_creacion', { ascending: false });

        if (error) throw error;

        res.json({ success: true, data });
    } catch (error) {
        res.status(500).json({ success: false, message: 'Error al obtener tareas', error: error.message });
    }
};

// Marcar como Completada (por el Hijo)
const completeTask = async (req, res) => {
    try {
        const { task_id } = req.params;

        const { data, error } = await supabase
            .from('api_tareas_recompensas_hijo')
            .update({ estado: 'ESPERANDO_APROBACION', fecha_actualizacion: new Date() })
            .eq('id', task_id)
            .select();

        if (error) throw error;
        if (data.length === 0) return res.status(404).json({ success: false, message: 'Tarea no encontrada' });

        res.json({ success: true, message: 'Tarea enviada a aprobación', data: data[0] });
    } catch (error) {
        res.status(500).json({ success: false, message: 'Error al actualizar la tarea', error: error.message });
    }
};

// Aprobar la tarea (Padre) y dar recompensa
const approveTask = async (req, res) => {
    try {
        const { task_id } = req.params;

        // 1. Marcar la tarea como APROBADA
        const { data: taskData, error: taskError } = await supabase
            .from('api_tareas_recompensas_hijo')
            .update({ estado: 'APROBADA', fecha_actualizacion: new Date() })
            .eq('id', task_id)
            .select();

        if (taskError) throw taskError;
        if (taskData.length === 0) return res.status(404).json({ success: false, message: 'Tarea no encontrada' });

        // AQUI: En un futuro puedes conectar esto para actualizar los minutos de pantalla del hijo 
        // en tu tabla principal de RestCycle.
        // Ejemplo: await supabase.rpc('sumar_minutos', { uuid: taskData[0].hijo_id, mins: taskData[0].recompensa_minutos });
        // (Por ahora dejamos solo el cambio de estado completado)

        res.json({ 
            success: true, 
            message: `Tarea Aprobada. Se han otorgado ${taskData[0].recompensa_minutos} minutos de recompensa.`,
            data: taskData[0]
        });

    } catch (error) {
        res.status(500).json({ success: false, message: 'Error al aprobar la tarea', error: error.message });
    }
};

// Rechazar la tarea (Padre)
const rejectTask = async (req, res) => {
    try {
        const { task_id } = req.params;
        const { motivo } = req.body;

        const { data, error } = await supabase
            .from('api_tareas_recompensas_hijo')
            .update({ estado: 'RECHAZADA', motivo_rechazo: motivo, fecha_actualizacion: new Date() })
            .eq('id', task_id)
            .select();

        if (error) throw error;
        if (data.length === 0) return res.status(404).json({ success: false, message: 'Tarea no encontrada' });

        res.json({ success: true, message: 'Tarea rechazada. El hijo deberá intentarlo de nuevo.', data: data[0] });
    } catch (error) {
        res.status(500).json({ success: false, message: 'Error al rechazar la tarea', error: error.message });
    }
};


module.exports = {
    createTask,
    getTasksByChild,
    getTasksByParent,
    completeTask,
    approveTask,
    rejectTask
};
