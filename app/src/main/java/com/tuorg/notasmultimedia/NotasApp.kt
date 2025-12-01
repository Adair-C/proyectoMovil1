package com.tuorg.notasmultimedia

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tuorg.notasmultimedia.data.sync.NotesSyncWorker
import com.tuorg.notasmultimedia.di.Graph
import java.util.concurrent.TimeUnit

class NotasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.init(this)

        // (Opcional) una corrida única al arrancar
        WorkManager.getInstance(this).enqueueUniqueWork(
            "notes-sync-once",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<NotesSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )

        // (Opcional) trabajo periódico cada 6 horas
        val periodic = PeriodicWorkRequestBuilder<NotesSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notes-sync-periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }
}
