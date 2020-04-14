package cz.prague.cvut.fit.steuejan.amtelapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.SeasonFinisher
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SeasonFinisherService : Service(), CoroutineScope
{
    companion object
    {
        const val GROUPS_EXCEPT_PLAYOFF = "groups"

        private const val CHANNEL_ID = "finishSeasonChannel"
        private const val NOTIFICATION_ID = 184
    }

    private val job: Job = Job()
    private var isSuccess = true

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val notificationManager by lazy {
        NotificationManagerCompat.from(this)
    }

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_list_white_24dp)
            .setContentTitle("Generuji baráž...")
            .setProgress(0, 0, true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        if(intent == null) return START_NOT_STICKY

        val groups = intent.getParcelableArrayListExtra<Group>(GROUPS_EXCEPT_PLAYOFF)
        val seasonFinisher = SeasonFinisher(
            groups
        )

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        launch {
            if(!seasonFinisher.createPlayoff())
            {
                isSuccess = false
                return@launch
            }
            seasonFinisher.updateTeamRanks()
            seasonFinisher.transferTeams()
            updateNotification()
            stopForeground(false)
        }

        return START_STICKY
    }

    private fun updateNotification()
    {
        val title =
            if(isSuccess) "Baráž úspěšně vygenerována"
            else "Baráž se nepodařilo vygenerovat"

        notificationBuilder
            .setContentTitle(title)
            .setContentText(if(!isSuccess) null else "Příští sezóna je připravena.")
            .setProgress(0, 0, false)
            .setOngoing(false)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Generování baráže", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?) = null


}