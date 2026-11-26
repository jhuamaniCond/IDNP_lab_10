package com.unsa.idnp_lab10.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Punto 3, 5, 6, 7 y 8: Centraliza cómo se programa el trabajo (OneTime y Periodic),
 * añadiendo restricciones pertinentes (Punto 5), soporte periódico (Punto 6)
 * y garantizando la desvinculación total del hilo principal (Punto 7).
 *
 * Criterio de arquitectura (Punto 8):
 * Se delega en WorkManager porque la sincronización de apuntes de CampusDocs es un
 * trabajo diferible pero garantizado (debe completarse incluso si el usuario sale
 * de la app, bloquea la pantalla o se reinicia el dispositivo).
 */
class ProgramadorTrabajo(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    /**
     * Punto 5: Restricciones pertinentes.
     * Como la sincronización de apuntes requiere acceso a internet (CampusDocs),
     * exigimos que haya red conectada.
     */
    private val restriccionesSincronizacion = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Encola una sincronización única con restricciones y retardo opcional. */
    fun programarSincronizacion(retardoSegundos: Long, origen: String): UUID {
        val solicitud = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setInitialDelay(retardoSegundos, TimeUnit.SECONDS)
            .setConstraints(restriccionesSincronizacion)
            .setInputData(workDataOf(SincronizacionWorker.CLAVE_ORIGEN to origen))
            .addTag(ETIQUETA)
            .build()

        workManager.enqueueUniqueWork(NOMBRE_UNICO, ExistingWorkPolicy.REPLACE, solicitud)
        Log.i(SincronizacionWorker.TAG, "OneTimeWorkRequest encolado | id=${solicitud.id} | " +
                "retardo=${retardoSegundos}s | origen=$origen | restricción=NETWORK_CONNECTED")
        return solicitud.id
    }

    /**
     * Punto 6: Configurar trabajo periódico respetando las restricciones de intervalo de Android.
     * Android / WorkManager exige un intervalo mínimo de 15 minutos para PeriodicWorkRequest.
     */
    fun programarSincronizacionPeriodica(origen: String): UUID {
        val solicitudPeriodica = PeriodicWorkRequestBuilder<SincronizacionWorker>(
            15, TimeUnit.MINUTES // Intervalo mínimo exigido por el sistema
        )
            .setConstraints(restriccionesSincronizacion)
            .setInputData(workDataOf(SincronizacionWorker.CLAVE_ORIGEN to "$origen (periódico)"))
            .addTag(ETIQUETA_PERIODICA)
            .build()

        workManager.enqueueUniquePeriodicWork(
            NOMBRE_PERIODICO,
            ExistingPeriodicWorkPolicy.KEEP,
            solicitudPeriodica
        )
        Log.i(SincronizacionWorker.TAG, "PeriodicWorkRequest encolado | id=${solicitudPeriodica.id} | " +
                "intervalo=15 min | origen=$origen")
        return solicitudPeriodica.id
    }

    /** Punto 4: flujo con cada cambio de estado de la solicitud. */
    fun observar(id: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(id)

    fun cancelar() {
        workManager.cancelUniqueWork(NOMBRE_UNICO)
        Log.i(SincronizacionWorker.TAG, "Cancelación solicitada para '$NOMBRE_UNICO'")
    }

    fun cancelarPeriodico() {
        workManager.cancelUniqueWork(NOMBRE_PERIODICO)
        Log.i(SincronizacionWorker.TAG, "Cancelación solicitada para periódico '$NOMBRE_PERIODICO'")
    }

    companion object {
        const val NOMBRE_UNICO = "sincronizacion_apuntes_unica"
        const val NOMBRE_PERIODICO = "sincronizacion_apuntes_periodica"
        const val ETIQUETA = "sincronizacion"
        const val ETIQUETA_PERIODICA = "sincronizacion_periodica"
    }
}
