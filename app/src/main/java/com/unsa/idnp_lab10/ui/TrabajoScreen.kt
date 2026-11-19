package com.unsa.idnp_lab10.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import com.unsa.idnp_lab10.worker.SincronizacionWorker

/** Pantalla sin estado propio: recibe lo que observa el ViewModel. */
@Composable
fun TrabajoScreen(
    info: WorkInfo?,
    historial: List<CambioEstado>,
    onProgramar: (Long) -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var retardo by rememberSaveable { mutableLongStateOf(10L) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sincronización diferida", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("CampusDocs · Lab 10 WorkManager", style = MaterialTheme.typography.bodyMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Programar OneTimeWorkRequest", fontWeight = FontWeight.SemiBold)
                Text("Retardo inicial:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0L, 10L, 30L).forEach { segundos ->
                        FilterChip(
                            selected = retardo == segundos,
                            onClick = { retardo = segundos },
                            label = { Text("$segundos s") }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onProgramar(retardo) }) { Text("Programar") }
                    OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Estado del trabajo", fontWeight = FontWeight.SemiBold)
                if (info == null) {
                    Text("Aún no se ha programado ningún trabajo.")
                } else {
                    Text("id: ${info.id.toString().take(8)}…", fontFamily = FontFamily.Monospace)
                    Text(info.state.name, style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = colorEstado(info.state))
                    Text("Intentos: ${info.runAttemptCount}")
                    if (info.state == WorkInfo.State.SUCCEEDED) {
                        Text("Marca de tiempo: " +
                                info.outputData.getString(SincronizacionWorker.CLAVE_MARCA))
                        Text("Total de registros: " +
                                info.outputData.getInt(SincronizacionWorker.CLAVE_TOTAL, 0))
                    }
                }
            }
        }

        if (historial.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Historial de estados", fontWeight = FontWeight.SemiBold)
                    historial.forEach {
                        Text("${it.hora}  →  ${it.estado}", fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun colorEstado(estado: WorkInfo.State): Color = when (estado) {
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> Color(0xFF8A6D00)
    WorkInfo.State.RUNNING -> Color(0xFF1565C0)
    WorkInfo.State.SUCCEEDED -> Color(0xFF2E7D32)
    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> Color(0xFFC62828)
}
