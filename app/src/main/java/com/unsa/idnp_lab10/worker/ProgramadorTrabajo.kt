package com.unsa.idnp_lab10.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Punto 3: centraliza cómo se programa el trabajo.
 *
 * El Worker solo sabe QUÉ hacer; aquí se decide CÓMO y CUÁNDO se ejecuta
 * (WorkRequest). Para comprobar el funcionamiento se usa un
 * OneTimeWorkRequest con un retardo inicial corto.
 */
class ProgramadorTrabajo(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    /** Encola una sincronización única y devuelve el id de la solicitud. */
    fun programarSincronizacion(retardoSegundos: Long, origen: String): UUID {
        val solicitud = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setInitialDelay(retardoSegundos, TimeUnit.SECONDS)
            .setInputData(workDataOf(SincronizacionWorker.CLAVE_ORIGEN to origen))
            .addTag(ETIQUETA)
            .build()

        // Trabajo único por nombre: si se pulsa otra vez, reemplaza al anterior
        // en lugar de acumular sincronizaciones duplicadas.
        workManager.enqueueUniqueWork(NOMBRE_UNICO, ExistingWorkPolicy.REPLACE, solicitud)
        Log.i(SincronizacionWorker.TAG, "OneTimeWorkRequest encolado | id=${solicitud.id} | " +
                "retardo=${retardoSegundos}s | origen=$origen")
        return solicitud.id
    }

    /** Punto 4: flujo con cada cambio de estado de la solicitud. */
    fun observar(id: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(id)

    fun cancelar() {
        workManager.cancelUniqueWork(NOMBRE_UNICO)
        Log.i(SincronizacionWorker.TAG, "Cancelación solicitada para '$NOMBRE_UNICO'")
    }

    companion object {
        const val NOMBRE_UNICO = "sincronizacion_apuntes_unica"
        const val ETIQUETA = "sincronizacion"
    }
}
