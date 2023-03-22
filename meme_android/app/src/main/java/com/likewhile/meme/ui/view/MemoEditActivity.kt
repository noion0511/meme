package com.likewhile.meme.ui.view

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.R
import com.likewhile.meme.data.model.TextMemoItem
import com.likewhile.meme.databinding.ActivityMemoEditBinding
import com.likewhile.meme.ui.viewmodel.TextMemoViewModel
import com.likewhile.meme.util.DateFormatUtil
import java.util.*

class MemoEditActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMemoEditBinding.inflate(layoutInflater) }
    private lateinit var memoViewModel: TextMemoViewModel
    private val itemId by lazy { intent.getLongExtra(MemoWidgetProvider.EXTRA_MEMO_ID, -1) }
    private var isMenuVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        memoViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
            TextMemoViewModel::class.java)

        initMemoData()
        initSave()
        initCancel()
        initToolbar()
    }


    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, MemoWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        // 위젯 업데이트를 요청합니다.
        val intent = Intent(this, MemoWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        sendBroadcast(intent)
    }

    private fun setReadMode() {
        binding.title.editTextTitle.isEnabled = false
        binding.title.editTextTitle.alpha = 1f
        binding.title.editTextTitle.setTextColor(Color.BLACK)
        binding.content.editTextContent.isEnabled = false
        binding.content.editTextContent.alpha = 1f
        binding.content.editTextContent.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.checkBoxFix.isEnabled = false
        binding.bottomBtnEdit.checkBoxFix.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.buttonSave.visibility = View.GONE
        binding.bottomBtnEdit.buttonCancel.visibility = View.GONE
        // 메뉴를 무효화하여 onPrepareOptionsMenu()를 다시 호출
        isMenuVisible = true
        invalidateOptionsMenu()
    }


    private fun setEditMode() {
        binding.title.editTextTitle.isEnabled = true
        binding.content.editTextContent.isEnabled = true
        binding.bottomBtnEdit.checkBoxFix.isEnabled = true
        binding.bottomBtnEdit.buttonSave.visibility = View.VISIBLE
        binding.bottomBtnEdit.buttonCancel.visibility = View.VISIBLE
        // 메뉴를 무효화하여 onPrepareOptionsMenu()를 다시 호출
        isMenuVisible = false
        invalidateOptionsMenu()
    }

    private fun initToolbar() {
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            Gravity.START
        )
        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        binding.toolbar.toolbar.layoutParams = params
        binding.toolbar.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initSave() {
        binding.bottomBtnEdit.buttonSave.setOnClickListener {
            val title = binding.title.editTextTitle.text.toString()
            val content = binding.content.editTextContent.text.toString()
            val time = DateFormatUtil.formatDate(Date(), "yyyy-MM-dd HH:mm")
            val isFixed = binding.bottomBtnEdit.checkBoxFix.isChecked

            val memoItem = TextMemoItem(
                id = itemId,
                title = title,
                content = content,
                date = time,
                isFixed = isFixed,
            )

            if (title.isBlank() || content.isBlank())
                Toast.makeText(this, "제목과 상세내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                if (itemId != -1L) {
                    memoViewModel.updateMemo(memoItem)
                    setReadMode()
                    updateWidget()
                } else {
                    memoViewModel.insertMemo(memoItem)
                    setReadMode()
                }
            }
        }
    }


    private fun initCancel() {
        binding.bottomBtnEdit.buttonCancel.setOnClickListener { finish() }
    }

    private fun initMemoData() {
        if (itemId != -1L) {
            memoViewModel.setItemId(itemId)
            setReadMode()
        } else {
            setEditMode()
        }

        memoViewModel.memo.observe(this) { memo ->
            if (memo != null) {
                binding.title.editTextTitle.setText(memo.title)
                binding.content.editTextContent.setText(memo.content)
                binding.bottomBtnEdit.checkBoxFix.isChecked = memo.isFixed
            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.button_edit_mode -> {
                setEditMode()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.button_edit_mode)
        menuItem.isVisible = isMenuVisible
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        memoViewModel.closeDB()
    }
}