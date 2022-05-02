package `in`.rithikjain.multitimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.collections.HashMap

class TimerService : Service() {
    companion object {
        // Channel ID for notifications
        const val CHANNEL_ID = "Multi_Timer_Notifications"

        // Service Actions
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val GET_STATUS = "GET_STATUS"

        // Intent Extras
        const val TIMER_ID = "TIMER_ID"
        const val TIMER_ACTION = "TIMER_ACTION"
        const val TIME_ELAPSED = "TIME_ELAPSED"
        const val IS_TIMER_RUNNING = "IS_TIMER_RUNNING"

        // Intent Actions
        const val TIMER_TICK = "TIMER_TICK"
        const val TIMER_STATUS = "TIMER_STATUS"
    }

    private val timeMap = HashMap<Int, Int>()
    private val timersMap = HashMap<Int, Timer>()
    private val isTimerRunningMap = HashMap<Int, Boolean>().withDefault { false }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("Timer", "Timer onBind")
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()

        val timerID = intent?.getIntExtra(TIMER_ID, -1) ?: 0
        val action = intent?.getStringExtra(TIMER_ACTION)!!

        Log.d("Timer", "onStartCommand Action: $action")

        when (action) {
            START -> startTimer(timerID)
            PAUSE -> pauseTimer(timerID)
            RESET -> resetTimer(timerID)
            GET_STATUS -> sendStatus(timerID)
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Log.d("Timer", "onTaskRemoved")
    }

    private fun startTimer(timerID: Int) {
        isTimerRunningMap[timerID] = true
        sendStatus(timerID)

        timersMap[timerID] = Timer()
        timersMap[timerID]!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val timerIntent = Intent()
                timerIntent.action = TIMER_TICK

                timeMap[timerID] = timeMap[timerID]?.plus(1) ?: 0

                updateNotification(timerID)

                timerIntent.putExtra(TIME_ELAPSED, timeMap[timerID] ?: 0)
                sendBroadcast(timerIntent)
            }
        }, 0, 1000)

        startForeground(timerID, buildNotification(timerID))
    }

    private fun pauseTimer(timerID: Int) {
        timersMap[timerID]?.cancel()
        isTimerRunningMap[timerID] = false
        sendStatus(timerID)
        updateNotification(timerID)
    }

    private fun resetTimer(timerID: Int) {
        pauseTimer(timerID)
        timeMap[timerID] = 0
        sendStatus(timerID)
        stopForeground(true)
    }

    private fun sendStatus(timerID: Int) {
        val statusIntent = Intent()
        statusIntent.action = TIMER_STATUS
        statusIntent.putExtra(IS_TIMER_RUNNING, isTimerRunningMap[timerID] ?: false)
        statusIntent.putExtra(TIME_ELAPSED, timeMap[timerID] ?: 0)
        sendBroadcast(statusIntent)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Multi Timer",
                NotificationManager.IMPORTANCE_MIN
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(timerID: Int): Notification {
        val title = if (isTimerRunningMap[timerID] == true) {
            "Timer $timerID is running!"
        } else {
            "Timer $timerID is paused!"
        }

        val hours: Int = timeMap[timerID]?.div(60)?.div(60) ?: 0
        val minutes: Int = timeMap[timerID]?.div(60) ?: 0
        val seconds: Int = timeMap[timerID]?.rem(60) ?: 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(title)
            .setContentText(
                "${"%02d".format(hours)}:${"%02d".format(minutes)}:${
                    "%02d".format(
                        seconds
                    )
                }"
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }


    private fun updateNotification(timerID: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)?.notify(
                timerID,
                buildNotification(timerID)
            )
        }
    }
}