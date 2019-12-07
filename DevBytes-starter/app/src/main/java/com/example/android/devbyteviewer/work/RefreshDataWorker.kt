package com.example.android.devbyteviewer.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.devbyteviewer.R
import com.example.android.devbyteviewer.database.getDatabase
import com.example.android.devbyteviewer.repository.VideosRepository
import com.example.android.devbyteviewer.ui.DevByteActivity
import kotlinx.android.synthetic.main.devbyte_item.view.*
import retrofit2.HttpException
import timber.log.Timber

class RefreshDataWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "com.example.android.devbyteviewer.work.RefreshDataWorker"
        private var notificationManager: NotificationManager? = null
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        const val STARTED_NOTIFICATION_ID = 0
        const val FINISHED_NOTIFICATION_ID = 1
    }
    override suspend fun doWork(): Result {
        //Toast.makeText(applicationContext,"work started!",Toast.LENGTH_SHORT).show()
        createNotificationChannel()
        showNotification(applicationContext.getString(R.string.work_scheduled_notif_title),
                applicationContext.getString(R.string.work_scheduled_notif_text),
                STARTED_NOTIFICATION_ID)
        val database = getDatabase(applicationContext)
        val repository = VideosRepository(database)
        try {
            repository.refreshVideos {
                showNotification(applicationContext.getString(R.string.work_finished_notif_title),
                        applicationContext.getString(R.string.work_finished_notif_text), FINISHED_NOTIFICATION_ID)
            }
            Timber.d("Work request for sync started running")
        }
        catch (e : HttpException) {
            //Toast.makeText(applicationContext,"work failded and will retry",Toast.LENGTH_SHORT).show()
            return Result.retry()
        }
        //Toast.makeText(applicationContext,"work was successfully completed",Toast.LENGTH_SHORT).show()
        return Result.success()
    }

    private fun showNotification(title:String,text:String,id:Int) {

        val pendingIntent = PendingIntent.getActivity(applicationContext, STARTED_NOTIFICATION_ID,
                Intent(applicationContext,DevByteActivity::class.java),PendingIntent.FLAG_CANCEL_CURRENT)
        val context = applicationContext
        val builder = NotificationCompat.Builder(
                context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_circle_outline_black_48dp)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager?.notify(id, builder.build())
    }

    fun createNotificationChannel() {
        val context = applicationContext
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    PRIMARY_CHANNEL_ID, context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = context.getString(R.string.notification_channel_description)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }
}