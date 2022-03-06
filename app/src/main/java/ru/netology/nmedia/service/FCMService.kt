package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushMessage
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val messageData = message.data["content"]
        println(messageData)
        val pushMessage = gson.fromJson(messageData, PushMessage::class.java)

        val appAuthValue = auth.authStateFlow.value
        if (pushMessage.recipientId == null || pushMessage.recipientId == appAuthValue.id) {
            handleNotification(pushMessage.content)
        } else {
            auth.sendPushToken(appAuthValue.token)
        }
    }

    override fun onNewToken(token: String) {
        auth.sendPushToken(token)
    }

    private fun handleNotification(contentText: String?) {
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Netology")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (contentText != null) {
            notificationBuilder.setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                )
        }

        val notification = notificationBuilder.build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}
