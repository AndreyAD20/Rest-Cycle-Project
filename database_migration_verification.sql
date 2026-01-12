-- Script SQL para agregar campos de verificación a la tabla usuario
-- Ejecutar este script en Supabase SQL Editor

-- Agregar columnas para código de verificación
ALTER TABLE usuario 
ADD COLUMN IF NOT EXISTS codigo_verificacion VARCHAR(6),
ADD COLUMN IF NOT EXISTS codigo_expiracion TIMESTAMP;

-- Crear índice para búsquedas más rápidas por correo
CREATE INDEX IF NOT EXISTS idx_usuario_correo ON usuario(correo);

-- Crear índice para código de verificación
CREATE INDEX IF NOT EXISTS idx_usuario_codigo_verificacion ON usuario(codigo_verificacion) 
WHERE codigo_verificacion IS NOT NULL;

-- Comentarios para documentación
COMMENT ON COLUMN usuario.codigo_verificacion IS 'Código de 6 dígitos para verificación de email';
COMMENT ON COLUMN usuario.codigo_expiracion IS 'Fecha y hora de expiración del código (15 minutos)';
COMMENT ON COLUMN usuario.email_verificado IS 'Indica si el email ha sido verificado';
