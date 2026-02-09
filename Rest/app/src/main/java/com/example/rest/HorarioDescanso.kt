package com.example.rest

data class HorarioDescanso(
    val id: Int,
    val nombre: String,
    val horaInicio: String,
    val horaFin: String,
    val diasActivos: List<Boolean> = List(7) { true }, // L M M J V S D
    val activo: Boolean = true,
    val bedtimeMode: Boolean = false
)
