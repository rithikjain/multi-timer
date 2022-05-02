package `in`.rithikjain.multitimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class TimerService : Service() {
    companion object {
        // Channel ID for notifications
        const val CHANNEL_ID = "Multi_Timer_Notifications"

        // Service Actions
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val GET_STATUS = "GET_STATUS"
        const val MOVE_TO_FOREGROUND = "MOVE_TO_FOREGROUND"
        const val MOVE_TO_BACKGROUND = "MOVE_TO_BACKGROUND"

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

    private var updateTimer = Timer()

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
            MOVE_TO_FOREGROUND -> moveToForeground()
            MOVE_TO_BACKGROUND -> moveToBackground()
        }

        return START_STICKY
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

                timerIntent.putExtra(TIME_ELAPSED, timeMap[timerID] ?: 0)
                sendBroadcast(timerIntent)
            }
        }, 0, 1000)
    }

    private fun moveToForeground() {
        val runningIDs = mutableListOf<Int>()

        for ((timerID, value) in isTimerRunningMap) {
            if (value) {
                startForeground(timerID, buildNotification(timerID))
                runningIDs.add(timerID)
            }
        }

        updateTimer = Timer()

        if (runningIDs.isNotEmpty()) {
            updateTimer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    for (timerID in runningIDs) {
                        updateNotification(timerID)
                    }
                }
            }, 0, 1000)
        }
    }

    private fun moveToBackground() {
        updateTimer.cancel()
        stopForeground(true)
    }

    private fun pauseTimer(timerID: Int) {
        timersMap[timerID]?.cancel()
        isTimerRunningMap[timerID] = false
        sendStatus(timerID)
    }

    private fun resetTimer(timerID: Int) {
        pauseTimer(timerID)
        timeMap[timerID] = 0
        sendStatus(timerID)
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
                NotificationManager.IMPORTANCE_LOW
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
            .setContentTitle(title)
            .setContentText(
                "${"%02d".format(hours)}:${"%02d".format(minutes)}:${
                    "%02d".format(
                        seconds
                    )
                }"
            )
            .setColorized(true)
            .setColor(Color.parseColor("#BEAEE2"))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOnlyAlertOnce(true)
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