-- Solución al error: "null value in column telefono violates not-null constraint"
-- La aplicación permite registrarse sin teléfono, pero la base de datos lo exigía obligatoriamente.

-- Hacer que la columna 'telefono' sea opcional (nullable)
ALTER TABLE public.usuario 
ALTER COLUMN telefono DROP NOT NULL;

-- Recargar caché de Supabase
NOTIFY pgrst, 'reload config';
