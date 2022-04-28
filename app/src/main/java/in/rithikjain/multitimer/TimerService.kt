package `in`.rithikjain.multitimer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*
import kotlin.collections.HashMap

class TimerService : Service() {
    companion object {
        const val CHANNEL_ID = "Multi_Timer_Notifications"
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val GET_STATUS = "GET_STATUS"
    }

    private val timeMap = HashMap<Int, Int>()
    private val timersMap = HashMap<Int, Timer>()
    private val isTimerRunningMap = HashMap<Int, Boolean>().withDefault { false }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("Timer", "Timer onBind")
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timerID = intent?.getIntExtra("TimerID", -1) ?: 0

        when (intent?.getStringExtra("TimerAction")!!) {
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
                timerIntent.action = "Timer"

                timeMap[timerID] = timeMap[timerID]!!.plus(1)
                timerIntent.putExtra("TimeElapsed", timeMap[timerID] ?: 0)
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

    }

    private fun sendStatus(timerID: Int) {
        val statusIntent = Intent()
        statusIntent.action = "TimerStatus"
        statusIntent.putExtra("IsTimerRunning", isTimerRunningMap[timerID] ?: false)
        statusIntent.putExtra("TimeElapsed", timeMap[timerID] ?: 0)
        sendBroadcast(statusIntent)
    }
}