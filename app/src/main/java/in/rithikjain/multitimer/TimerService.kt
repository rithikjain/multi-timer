package `in`.rithikjain.multitimer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
}