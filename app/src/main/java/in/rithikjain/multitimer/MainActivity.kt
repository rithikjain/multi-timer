package `in`.rithikjain.multitimer

import `in`.rithikjain.multitimer.databinding.ActivityMainBinding
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timerReceiver: BroadcastReceiver

    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getTimerStatus(1)

        binding.toggleButton.setOnClickListener {
            if (isTimerRunning) pauseTimer(1) else startTimer(1)
        }

        binding.resetImageView.setOnClickListener {
            resetTimer(1)
        }
    }

    override fun onStart() {
        super.onStart()

        // Moving the service to background when the app in visible
        moveToBackground()

        // Receiving timer status from service
        val statusFilter = IntentFilter()
        statusFilter.addAction(TimerService.TIMER_STATUS)
        statusReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(p0: Context?, p1: Intent?) {
                val isRunning = p1?.getBooleanExtra(TimerService.IS_TIMER_RUNNING, false)!!
                isTimerRunning = isRunning
                val timeElapsed = p1.getIntExtra(TimerService.TIME_ELAPSED, 0)

                updateLayout(isTimerRunning)
                updateTimerValue(timeElapsed)
            }
        }
        registerReceiver(statusReceiver, statusFilter)

        // Receiving time values from service
        val timerFilter = IntentFilter()
        timerFilter.addAction(TimerService.TIMER_TICK)
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(TimerService.TIME_ELAPSED, 0)!!
                updateTimerValue(timeElapsed)
            }
        }
        registerReceiver(timerReceiver, timerFilter)
    }

    override fun onPause() {
        super.onPause()

        // Moving the service to foreground when the app is in background / not visible
        moveToForeground()

        unregisterReceiver(statusReceiver)
        unregisterReceiver(timerReceiver)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimerValue(timeElapsed: Int) {
        val hours: Int = (timeElapsed / 60) / 60
        val minutes: Int = timeElapsed / 60
        val seconds: Int = timeElapsed % 60
        binding.timerValueTextView.text =
            "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
    }

    private fun updateLayout(isTimerRunning: Boolean) {
        if (isTimerRunning) {
            binding.toggleButton.icon =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_pause)
            binding.deleteImageView.visibility = View.INVISIBLE
            binding.resetImageView.visibility = View.INVISIBLE
        } else {
            binding.toggleButton.icon =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_play)
            binding.deleteImageView.visibility = View.VISIBLE
            binding.resetImageView.visibility = View.VISIBLE
        }
    }

    private fun getTimerStatus(timerID: Int) {
        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.GET_STATUS)
        startService(timerService)
    }

    private fun startTimer(timerID: Int) {
        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.START)
        startService(timerService)
    }

    private fun pauseTimer(timerID: Int) {
        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.PAUSE)
        startService(timerService)
    }

    private fun resetTimer(timerID: Int) {
        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.RESET)
        startService(timerService)
    }

    private fun moveToForeground() {
        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.MOVE_TO_FOREGROUND)
        startService(timerService)
    }

    private fun moveToBackground() {
        val timerService = Intent(this, TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.MOVE_TO_BACKGROUND)
        startService(timerService)
    }
}