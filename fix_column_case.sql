-- Corregir el nombre de la columna para que sea todo minúsculas
-- Esto soluciona el error de "Could not find column" en la API

-- Renombrar columna "Mayoredad" -> "mayoredad"
ALTER TABLE public.usuario 
RENAME COLUMN "Mayoredad" TO mayoredad;

-- Recargar caché de Supabase
NOTIFY pgrst, 'reload config';
