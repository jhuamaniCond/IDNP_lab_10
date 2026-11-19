package com.unsa.idnp_lab10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unsa.idnp_lab10.ui.TrabajoScreen
import com.unsa.idnp_lab10.ui.TrabajoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val vm: TrabajoViewModel = viewModel()
                val info by vm.infoTrabajo.collectAsStateWithLifecycle()
                val historial by vm.historial.collectAsStateWithLifecycle()
                Scaffold(modifier = Modifier.fillMaxSize()) { relleno ->
                    TrabajoScreen(
                        info = info,
                        historial = historial,
                        onProgramar = vm::programar,
                        onCancelar = vm::cancelar,
                        modifier = Modifier.padding(relleno)
                    )
                }
            }
        }
    }
}
