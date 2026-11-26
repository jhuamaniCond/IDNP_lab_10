package com.unsa.idnp_lab10.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Punto 2 y 7: Unidad de trabajo en segundo plano de la práctica.
 *
 * Simula la sincronización de apuntes de CampusDocs (proyecto final) y deja
 * tres huellas observables: mensajes en Logcat con la etiqueta "Lab10WM",
 * una línea con marca de tiempo en el archivo registro_sincronizacion.txt
 * del almacenamiento interno y un resultado (outputData) que la interfaz lee.
 *
 * Cumplimiento del Punto 7:
 * Al extender de [CoroutineWorker], la ejecución de [doWork] se gestiona fuera
 * del hilo principal (Main Thread). Adicionalmente, las operaciones de Entrada/Salida
 * de disco (escritura y lectura de archivo) se encapsulan explícitamente dentro de
 * [Dispatchers.IO] mediante `withContext(Dispatchers.IO)`, garantizando que el hilo
 * de interfaz de usuario permanezca completamente fluido (60/120 fps) y libre de bloqueos.
 */
class SincronizacionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val origen = inputData.getString(CLAVE_ORIGEN) ?: "desconocido"
        Log.i(TAG, "doWork() iniciado | id=$id | origen=$origen | " +
                "intento=${runAttemptCount + 1} | hilo=${Thread.currentThread().name}")

        return try {
            // Simulación de la descarga de apuntes (trabajo de red prolongado).
            delay(DURACION_SIMULADA_MS)

            // Punto 7: Despacho explícito a Dispatchers.IO para operaciones I/O de disco
            val (marca, total) = withContext(Dispatchers.IO) {
                Log.d(TAG, "Operación I/O en disco ejecutándose en hilo: ${Thread.currentThread().name}")
                val marcaTiempo = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val registro = File(applicationContext.filesDir, ARCHIVO_REGISTRO)
                registro.appendText("$marcaTiempo | sincronización de apuntes | origen=$origen\n")
                val totalLineas = registro.readLines().size
                marcaTiempo to totalLineas
            }

            Log.i(TAG, "Registro guardado: \"$marca\" | total de registros=$total | hilo=${Thread.currentThread().name}")
            Result.success(
                workDataOf(CLAVE_MARCA to marca, CLAVE_TOTAL to total)
            )
        } catch (e: IOException) {
            Log.e(TAG, "No se pudo escribir el registro en almacenamiento", e)
            Result.failure()
        }
    }

    companion object {
        const val TAG = "Lab10WM"
        const val ARCHIVO_REGISTRO = "registro_sincronizacion.txt"
        const val DURACION_SIMULADA_MS = 3_000L

        // Claves de entrada y salida
        const val CLAVE_ORIGEN = "origen"
        const val CLAVE_MARCA = "marca_tiempo"
        const val CLAVE_TOTAL = "total_registros"
    }
}
