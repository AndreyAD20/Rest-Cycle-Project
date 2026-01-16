# Guía: Configurar Edge Function en Supabase

## Paso 1: Obtener API Key de Resend

1. Ve a https://resend.com
2. Crea una cuenta (gratis)
3. Verifica tu email
4. Ve a **Dashboard** → **API Keys**
5. Haz clic en **Create API Key**
6. Dale un nombre (ej: "Rest Cycle Production")
7. **COPIA LA API KEY** (solo se muestra una vez)
   - Formato: `re_xxxxxxxxxxxxxxxxxxxxxxxxxx`

---

## Paso 2: Crear Edge Function en Supabase

### Opción A: Desde Supabase Dashboard (Más fácil)

1. Abre tu proyecto en https://supabase.com/dashboard
2. Ve a **Edge Functions** en el menú lateral
3. Haz clic en **Create a new function**
4. Nombre: `enviar-codigo-verificacion`
5. Copia y pega el contenido del archivo `supabase/functions/enviar-codigo-verificacion/index.ts`
6. Haz clic en **Deploy**

### Opción B: Desde CLI (Más profesional)

```bash
# 1. Instalar Supabase CLI (si no lo tienes)
npm install -g supabase

# 2. Login en Supabase
supabase login

# 3. Link al proyecto
cd c:\Users\anmdr\AndroidStudioProjects\Rest-Cycle-Project
supabase link --project-ref TU_PROJECT_REF

# 4. Configurar el secreto (API key de Resend)
supabase secrets set RESEND_API_KEY=re_tu_api_key_aqui

# 5. Desplegar la función
supabase functions deploy enviar-codigo-verificacion
```

**Para obtener TU_PROJECT_REF:**

- Ve a Supabase Dashboard → Settings → General
- Copia el "Reference ID"

---

## Paso 3: Configurar Secreto (RESEND_API_KEY)

### Desde Dashboard:

1. Ve a **Edge Functions** → **enviar-codigo-verificacion**
2. Haz clic en **Secrets**
3. Agrega nuevo secreto:
   - Key: `RESEND_API_KEY`
   - Value: `re_tu_api_key_de_resend`
4. Guarda

### Desde CLI:

```bash
supabase secrets set RESEND_API_KEY=re_tu_api_key_aqui
```

---

## Paso 4: Probar la Función

### Desde Dashboard:

1. Ve a **Edge Functions** → **enviar-codigo-verificacion**
2. Haz clic en **Invoke**
3. Usa este JSON de prueba:

```json
{
  "email": "tu@email.com",
  "code": "123456",
  "nombre": "Test User"
}
```

4. Haz clic en **Send**
5. Revisa tu email

### Desde CLI:

```bash
supabase functions invoke enviar-codigo-verificacion \
  --data '{"email":"tu@email.com","code":"123456","nombre":"Test"}'
```

---

## Paso 5: Obtener URL de la Función

La URL será:

```
https://TU_PROJECT_REF.supabase.co/functions/v1/enviar-codigo-verificacion
```

Reemplaza `TU_PROJECT_REF` con tu Reference ID.

---

## Verificación

✅ La función está desplegada
✅ El secreto RESEND_API_KEY está configurado
✅ La prueba envió un email correctamente
✅ Tienes la URL de la función

**Siguiente paso:** Actualizar el código Android para usar esta función.

---

## Troubleshooting

### Error: "RESEND_API_KEY no configurada"

- Verifica que configuraste el secreto correctamente
- Redespliega la función después de configurar el secreto

### Error: "Invalid API key"

- Verifica que copiaste la API key completa de Resend
- Asegúrate de que empiece con `re_`

### Email no llega

- Revisa la carpeta de spam
- Verifica que el email de destino sea válido
- Revisa los logs de la función en Supabase Dashboard

### Error de CORS

- La función ya incluye headers de CORS
- Si persiste, verifica que estés usando la URL correcta
