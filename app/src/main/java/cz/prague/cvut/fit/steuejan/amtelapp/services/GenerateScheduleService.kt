package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.ScheduleGenerator
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GenerateScheduleService : Service(), CoroutineScope
{
    companion object
    {
        const val GROUP = "group"
        const val ROUNDS = "rounds"
        const val REGENERATE = "regenerate"

        private const val CHANNEL_ID = "generateScheduleChannel"
        private const val NOTIFICATION_ID = 69
    }

    private val job: Job = Job()
    private var isSuccess = false

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val notificationManager by lazy {
        NotificationManagerCompat.from(this)
    }

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_list_white_24dp)
            .setContentTitle("Generuji utkání...")
            .setProgress(0, 0, true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        if(intent == null) return START_NOT_STICKY

        val group = intent.getParcelableExtra<Group>(GROUP)
        val rounds = intent.getIntExtra(ROUNDS, 0)
        val regenerate = intent.getBooleanExtra(REGENERATE, false)

        val generator = ScheduleGenerator()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        launch {
            isSuccess =
                if(regenerate) generator.regenerateMatches(group, rounds)
                else generator.generateMatches(group, rounds)
            updateNotification()
            stopForeground(false)
        }

        return START_STICKY
    }

    override fun onDestroy()
    {
        super.onDestroy()
        job.cancel()
    }

    private fun updateNotification()
    {
        val title =
            if(isSuccess) "Utkání byla úspěšně vygenerována"
            else "Utkání se nepodařila vygenerovat"

        notificationBuilder
            .setContentTitle(title)
            .setContentText(if(!isSuccess) null else "Vygenerovaná utkání naleznete v přehledu utkání.")
            .setProgress(0, 0, false)
            .setOngoing(false)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Generování utkání", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent?) = null

}