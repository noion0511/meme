package com.likewhile.meme.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.likewhile.meme.data.model.MemoItem
import com.likewhile.meme.data.model.SortTypeChangeEvent
import com.likewhile.meme.databinding.FragmentCalendarModeBinding
import com.likewhile.meme.ui.adapter.CalendarAdapter
import com.likewhile.meme.ui.viewmodel.CalendarModeViewModel
import com.likewhile.meme.util.CalendarUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class CalendarModeFragment : Fragment(), CalendarAdapter.OnMonthChangeListener, CalendarAdapter.OnItemClickListener {

    private lateinit var binding: FragmentCalendarModeBinding
    private val viewModel: CalendarModeViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var memoList: List<MemoItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCalendarModeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initCalendar()
        initCalendarData()

        return binding.root
    }

    private fun initCalendar() {
        calendarAdapter = CalendarAdapter(this)
        calendarAdapter.setItemClickListener(this)
        binding.calendar.fgCalDay.layoutManager = GridLayoutManager(requireActivity(), CalendarUtil.DAYS_OF_WEEK)
        binding.calendar.fgCalDay.adapter = calendarAdapter

        binding.calendar.fgCalPre.setOnClickListener {
            calendarAdapter.changeToPrevMonth()
        }
        binding.calendar.fgCalNext.setOnClickListener {
            calendarAdapter.changeToNextMonth()
        }
    }

    override fun onMonthChanged(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
        binding.calendar.fgCalMonth.text = sdf.format(calendar.time)
        viewModel.setCalendarMonthData(calendar)
    }

    override fun onClick(v: View, position: Int, day: Int) {
        viewModel.setCalendarDateData(date = day)
        val bottomSheet = MemoListBottomSheet(memoList)
        bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
    }

    private fun initCalendarData() {
        viewModel.memos.observe(viewLifecycleOwner) { memos ->
            memos?.let {
                calendarAdapter.setItems(it)
            }
        }
        viewModel.memosDate.observe(viewLifecycleOwner) { memoDate ->
            memoList = memoDate
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangedEvent(event: SortTypeChangeEvent) {
        viewModel.setSortType(event.type)
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
//        viewModel.refreshMemos()
    }


    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeDB()
    }

    companion object {
        fun newInstance(): CalendarModeFragment {
            return CalendarModeFragment()
        }
    }
}
