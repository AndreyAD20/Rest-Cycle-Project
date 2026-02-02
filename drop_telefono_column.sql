-- Eliminar la columna 'telefono' de la tabla 'usuario'
-- Advertencia: Esto borrará todos los datos de teléfonos existentes.

ALTER TABLE public.usuario 
DROP COLUMN IF EXISTS telefono;

-- Recargar caché de esquema para que la API deje de esperar este campo
NOTIFY pgrst, 'reload config';
