package com.example.rest.data.repository

/**
 * EJEMPLO DE USO DEL REPOSITORIO DE USUARIO
 * 
 * Este archivo muestra cómo usar UsuarioRepository en tus Activities/ViewModels
 */

/*
// ==================== EJEMPLO 1: LOGIN ====================

// En tu LoginComposeActivity o ViewModel:
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginComposeActivity : BaseComposeActivity() {
    private val usuarioRepository = UsuarioRepository()
    
    private fun realizarLogin(correo: String, contraseña: String) {
        lifecycleScope.launch {
            when (val resultado = usuarioRepository.login(correo, contraseña)) {
                is UsuarioRepository.Result.Success -> {
                    val usuario = resultado.data
                    // Login exitoso
                    Log.d("Login", "Bienvenido ${usuario.nombre}")
                    // Guardar datos del usuario en SharedPreferences o similar
                    // Navegar a InicioComposeActivity
                    val intent = Intent(this@LoginComposeActivity, InicioComposeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is UsuarioRepository.Result.Error -> {
                    // Mostrar error al usuario
                    Log.e("Login", "Error: ${resultado.message}")
                    // Mostrar Toast o Snackbar con el error
                }
                is UsuarioRepository.Result.Loading -> {
                    // Mostrar indicador de carga
                }
            }
        }
    }
}

// ==================== EJEMPLO 2: REGISTRO ====================

// En tu RegistroComposeActivity:
import com.example.rest.data.models.RegistroRequest
import java.time.LocalDate
import java.time.Period

class RegistroComposeActivity : BaseComposeActivity() {
    private val usuarioRepository = UsuarioRepository()
    
    private fun realizarRegistro(
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String,
        fechaNacimiento: String, // Formato: "YYYY-MM-DD"
        contraseña: String
    ) {
        lifecycleScope.launch {
            // Calcular si es mayor de edad
            val fechaNac = LocalDate.parse(fechaNacimiento)
            val edad = Period.between(fechaNac, LocalDate.now()).years
            val esMayorDeEdad = edad >= 18
            
            val request = RegistroRequest(
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                telefono = telefono,
                fechaNacimiento = fechaNacimiento,
                contraseña = contraseña,
                rol = if (esMayorDeEdad) "padre" else "hijo",
                mayorEdad = esMayorDeEdad
            )
            
            when (val resultado = usuarioRepository.registrar(request)) {
                is UsuarioRepository.Result.Success -> {
                    val usuario = resultado.data
                    // Registro exitoso
                    Log.d("Registro", "Usuario creado: ${usuario.id}")
                    // Navegar a LoginComposeActivity o directamente a Inicio
                    val intent = Intent(this@RegistroComposeActivity, LoginComposeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is UsuarioRepository.Result.Error -> {
                    // Mostrar error
                    Log.e("Registro", "Error: ${resultado.message}")
                    // Mostrar Toast o Snackbar
                }
                is UsuarioRepository.Result.Loading -> {
                    // Mostrar indicador de carga
                }
            }
        }
    }
}

// ==================== EJEMPLO 3: VERIFICAR CORREO ====================

// Verificar si un correo ya existe antes de registrar:
private suspend fun verificarCorreo(correo: String): Boolean {
    return usuarioRepository.verificarCorreoExiste(correo)
}

// Uso:
lifecycleScope.launch {
    if (verificarCorreo("ejemplo@correo.com")) {
        // El correo ya existe
        Toast.makeText(this@RegistroComposeActivity, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
    } else {
        // Continuar con el registro
    }
}

// ==================== EJEMPLO 4: OBTENER USUARIO POR ID ====================

private fun cargarDatosUsuario(idUsuario: Int) {
    lifecycleScope.launch {
        when (val resultado = usuarioRepository.obtenerUsuarioPorId(idUsuario)) {
            is UsuarioRepository.Result.Success -> {
                val usuario = resultado.data
                // Usar los datos del usuario
                Log.d("Usuario", "Nombre: ${usuario.nombre} ${usuario.apellido}")
            }
            is UsuarioRepository.Result.Error -> {
                Log.e("Usuario", "Error: ${resultado.message}")
            }
            is UsuarioRepository.Result.Loading -> {
                // Mostrar carga
            }
        }
    }
}

// ==================== EJEMPLO 5: ACTUALIZAR USUARIO ====================

private fun actualizarPerfil(usuario: Usuario) {
    lifecycleScope.launch {
        when (val resultado = usuarioRepository.actualizarUsuario(usuario.id!!, usuario)) {
            is UsuarioRepository.Result.Success -> {
                val usuarioActualizado = resultado.data
                Log.d("Actualizar", "Usuario actualizado correctamente")
                // Actualizar UI
            }
            is UsuarioRepository.Result.Error -> {
                Log.e("Actualizar", "Error: ${resultado.message}")
            }
            is UsuarioRepository.Result.Loading -> {
                // Mostrar carga
            }
        }
    }
}

// ==================== NOTAS IMPORTANTES ====================

/*
 * 1. Todas las funciones del repositorio son suspend functions, 
 *    por lo que deben llamarse desde una coroutine (lifecycleScope.launch)
 * 
 * 2. El patrón Result permite manejar tres estados:
 *    - Success: Operación exitosa con datos
 *    - Error: Operación fallida con mensaje de error
 *    - Loading: Operación en progreso
 * 
 * 3. Para usar en Compose, considera crear un ViewModel:
 *    class LoginViewModel : ViewModel() {
 *        private val repository = UsuarioRepository()
 *        val loginState = MutableStateFlow<UsuarioRepository.Result<Usuario>>(UsuarioRepository.Result.Loading)
 *        
 *        fun login(correo: String, contraseña: String) {
 *            viewModelScope.launch {
 *                loginState.value = repository.login(correo, contraseña)
 *            }
 *        }
 *    }
 * 
 * 4. Recuerda manejar los errores de red y mostrar mensajes apropiados al usuario
 * 
 * 5. Para debugging, revisa los logs de Retrofit en Logcat (tag: OkHttp)
 */
*/
