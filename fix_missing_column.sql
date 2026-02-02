-- Solución al error: "Could not find the 'mayoredad' column"
-- Este script asegura que la columna exista en la base de datos con el nombre correcto.

-- 1. Agregar la columna 'mayoredad' si no existe
ALTER TABLE public.usuario 
ADD COLUMN IF NOT EXISTS mayoredad BOOLEAN DEFAULT FALSE;

-- 2. Recargar la caché del esquema de Supabase (PostgREST)
-- Esto es necesario para que la API reconozca la nueva columna inmediatamente
NOTIFY pgrst, 'reload config';
