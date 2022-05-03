package `in`.rithikjain.multitimer

import `in`.rithikjain.multitimer.databinding.ActivityMainBinding
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var timerViewAdapter: TimerViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timerViewAdapter = TimerViewPagerAdapter(this)
        binding.timerViewPager.apply {
            adapter = timerViewAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            setPageTransformer(MarginPageTransformer(100))
        }

        binding.circleIndicator.setViewPager(binding.timerViewPager)

        binding.timerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.pageNumberTextView.text = "${position + 1} out of 5"
            }
        })
    }

    override fun onStart() {
        super.onStart()

        // Moving the service to background when the app in visible
        moveToBackground()
    }

    override fun onPause() {
        super.onPause()

        // Moving the service to foreground when the app is in background / not visible
        moveToForeground()
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