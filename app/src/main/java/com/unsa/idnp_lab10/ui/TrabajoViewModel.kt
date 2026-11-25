package com.unsa.idnp_lab10.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.unsa.idnp_lab10.worker.ProgramadorTrabajo
import com.unsa.idnp_lab10.worker.SincronizacionWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Una transición de estado observada, con la hora en que llegó. */
data class CambioEstado(val hora: String, val estado: WorkInfo.State)

/**
 * Punto 4: observa el estado del trabajo y lo registra en Logcat.
 * Mantiene el id de la última solicitud y el historial de estados para
 * que la pantalla los muestre y sobrevivan a una rotación.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrabajoViewModel(app: Application) : AndroidViewModel(app) {

    private val programador = ProgramadorTrabajo(app)
    private val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.US)

    private val idActual = MutableStateFlow<UUID?>(null)

    private val _historial = MutableStateFlow<List<CambioEstado>>(emptyList())
    val historial: StateFlow<List<CambioEstado>> = _historial.asStateFlow()

    /** Último WorkInfo de la solicitud actual (null si no hay ninguna). */
    val infoTrabajo: StateFlow<WorkInfo?> = idActual
        .flatMapLatest { id -> if (id == null) flowOf(null) else programador.observar(id) }
        .onEach { info -> if (info != null) registrarCambio(info) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun programar(retardoSegundos: Long) {
        _historial.value = emptyList()
        idActual.value = programador.programarSincronizacion(retardoSegundos, origen = "boton_ui")
    }

    fun programarPeriodico() {
        _historial.value = emptyList()
        idActual.value = programador.programarSincronizacionPeriodica(origen = "boton_ui_periodico")
    }

    fun cancelar() = programador.cancelar()

    fun cancelarPeriodico() = programador.cancelarPeriodico()

    private fun registrarCambio(info: WorkInfo) {
        // getWorkInfoByIdFlow puede reemitir el mismo estado (p. ej. al
        // cambiar el progreso); solo se anotan las transiciones reales.
        if (_historial.value.lastOrNull()?.estado == info.state) return
        val hora = formatoHora.format(Date())
        _historial.update { it + CambioEstado(hora, info.state) }

        val salida = if (info.state == WorkInfo.State.SUCCEEDED)
            " | salida: marca=${info.outputData.getString(SincronizacionWorker.CLAVE_MARCA)}, " +
                    "total=${info.outputData.getInt(SincronizacionWorker.CLAVE_TOTAL, 0)}"
        else ""
        Log.i(SincronizacionWorker.TAG, "Estado del trabajo ${info.id}: ${info.state} " +
                "| intentos=${info.runAttemptCount}$salida")
    }
}
