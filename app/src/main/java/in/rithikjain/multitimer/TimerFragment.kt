package `in`.rithikjain.multitimer

import `in`.rithikjain.multitimer.databinding.FragmentTimerBinding
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2


class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private var pageID: Int = -1
    private val timerID
        get() = pageID + 1

    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timerReceiver: BroadcastReceiver

    private var isTimerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = requireActivity().findViewById(R.id.timer_view_pager)

        binding.toggleButton.setOnClickListener {
            if (isTimerRunning) pauseTimer(timerID) else startTimer(timerID)
        }

        binding.resetImageView.setOnClickListener {
            resetTimer(timerID)
        }
    }

    override fun onResume() {
        super.onResume()
        pageID = viewPager.currentItem
        Log.d("Timer", "Current Timer is $pageID")

        getTimerStatus(timerID)

        // Receiving timer status from service
        val statusFilter = IntentFilter()
        statusFilter.addAction("${TimerService.TIMER_STATUS}$timerID")
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
        requireActivity().registerReceiver(statusReceiver, statusFilter)

        // Receiving time values from service
        val timerFilter = IntentFilter()
        timerFilter.addAction("${TimerService.TIMER_TICK}$timerID")
        timerReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(TimerService.TIME_ELAPSED, 0)!!
                updateTimerValue(timeElapsed)
            }
        }
        requireActivity().registerReceiver(timerReceiver, timerFilter)
    }

    override fun onPause() {
        super.onPause()

        requireActivity().unregisterReceiver(statusReceiver)
        requireActivity().unregisterReceiver(timerReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause)
            binding.resetImageView.visibility = View.INVISIBLE
        } else {
            binding.toggleButton.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_play)
            binding.resetImageView.visibility = View.VISIBLE
        }
    }

    private fun getTimerStatus(timerID: Int) {
        val timerService = Intent(requireActivity(), TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.GET_STATUS)
        requireActivity().startService(timerService)
    }

    private fun startTimer(timerID: Int) {
        val timerService = Intent(requireActivity(), TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.START)
        requireActivity().startService(timerService)
    }

    private fun pauseTimer(timerID: Int) {
        val timerService = Intent(requireActivity(), TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.PAUSE)
        requireActivity().startService(timerService)
    }

    private fun resetTimer(timerID: Int) {
        val timerService = Intent(requireActivity(), TimerService::class.java)
        timerService.putExtra(TimerService.TIMER_ID, timerID)
        timerService.putExtra(TimerService.TIMER_ACTION, TimerService.RESET)
        requireActivity().startService(timerService)
    }
}