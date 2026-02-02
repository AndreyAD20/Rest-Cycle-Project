-- Deshabilitar RLS en la tabla eventos
-- Esto es necesario porque la app envía las peticiones con la llave ANÓNIMA (sin token de usuario),
-- por lo que la política de seguridad bloqueaba la inserción (Error 401).

ALTER TABLE public.eventos DISABLE ROW LEVEL SECURITY;

-- Asegurar permisos para la llave anónima (por si acaso)
GRANT ALL ON TABLE public.eventos TO anon;
GRANT ALL ON TABLE public.eventos TO authenticated;
GRANT ALL ON TABLE public.eventos TO service_role;
