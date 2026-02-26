import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import nodemailer from "npm:nodemailer@6.9.13";

const GMAIL_USER = Deno.env.get('GMAIL_USER');
const GMAIL_APP_PASSWORD = Deno.env.get('GMAIL_APP_PASSWORD');

interface EmailRequest {
  email: string;
  code: string;
  nombre: string;
}

console.info('Edge Function: enviar-codigo-verificacion (Gmail SMTP) iniciada');

Deno.serve(async (req: Request) => {
  // Manejar CORS preflight
  if (req.method === 'OPTIONS') {
    return new Response('ok', {
      headers: {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'POST',
        'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
      }
    });
  }

  try {
    // Parsear request
    const { email, code, nombre }: EmailRequest = await req.json();
    
    console.log(`📧 Intentando enviar código a ${email} usando Gmail`);
    
    // Validar datos
    if (!email || !code || !nombre) {
      throw new Error('Faltan parámetros requeridos: email, code, nombre');
    }

    // Validar credenciales
    if (!GMAIL_USER || !GMAIL_APP_PASSWORD) {
      throw new Error('Credenciales de Gmail (GMAIL_USER, GMAIL_APP_PASSWORD) no configuradas');
    }

    // Configurar transporte SMTP
    const transporter = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: GMAIL_USER,
        pass: GMAIL_APP_PASSWORD,
      },
    });

    // Configurar mensaje
    const mailOptions = {
      from: `"Rest Cycle" <${GMAIL_USER}>`,
      to: email,
      subject: 'Código de verificación - Rest Cycle',
      html: `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin: 0; padding: 0; background-color: #f5f5f5; font-family: Arial, sans-serif;">
          <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 20px;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                  <!-- Header -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #00BCD4 0%, #80DEEA 100%); padding: 30px; text-align: center;">
                      <h1 style="margin: 0; color: #ffffff; font-size: 28px;">Rest Cycle</h1>
                    </td>
                  </tr>
                  
                  <!-- Content -->
                  <tr>
                    <td style="padding: 40px 30px;">
                      <h2 style="color: #333333; margin: 0 0 20px 0;">¡Hola ${nombre}! 👋</h2>
                      <p style="color: #666666; font-size: 16px; line-height: 1.5; margin: 0 0 30px 0;">
                        Gracias por registrarte en Rest Cycle. Para completar tu registro, por favor ingresa el siguiente código de verificación:
                      </p>
                      
                      <!-- Código -->
                      <table width="100%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td align="center" style="padding: 20px 0;">
                            <div style="background-color: #f8f9fa; border: 2px dashed #00BCD4; border-radius: 8px; padding: 20px; display: inline-block;">
                              <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">Tu código de verificación:</p>
                              <p style="margin: 0; font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #00BCD4; font-family: 'Courier New', monospace;">
                                ${code}
                              </p>
                            </div>
                          </td>
                        </tr>
                      </table>
                      
                      <p style="color: #999999; font-size: 14px; line-height: 1.5; margin: 30px 0 0 0; text-align: center;">
                        ⏱️ Este código expirará en <strong>15 minutos</strong>
                      </p>
                    </td>
                  </tr>
                  
                  <!-- Footer -->
                  <tr>
                    <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                      <p style="margin: 0; color: #999999; font-size: 12px;">
                        Si no solicitaste este código, puedes ignorar este mensaje de forma segura.
                      </p>
                      <p style="margin: 10px 0 0 0; color: #cccccc; font-size: 11px;">
                        © 2026 Rest Cycle. Todos los derechos reservados.
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
      `
    };

    // Enviar email
    const info = await transporter.sendMail(mailOptions);
    console.log(`✅ Email enviado exitosamente: ${info.messageId}`);

    return new Response(
      JSON.stringify({ 
        success: true,
        message: 'Email enviado correctamente',
        id: info.messageId
      }), 
      {
        headers: { 
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*',
        }
      }
    );

  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error';
    console.error(`❌ Error al enviar email: ${errorMessage}`);
    
    return new Response(
      JSON.stringify({ 
        success: false,
        error: errorMessage
      }), 
      {
        status: 500,
        headers: { 
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*',
        }
      }
    );
  }
});
