package com.likewhile.meme.ui.view


import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.likewhile.meme.*
import com.likewhile.meme.data.model.SortTypeChangeEvent
import com.likewhile.meme.databinding.ActivityMainBinding
import com.likewhile.meme.ui.viewmodel.MainViewModel
import com.likewhile.meme.util.MemoNotificationService
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var mainViewModel: MainViewModel
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(MainViewModel::class.java)

        if (savedInstanceState == null) {
            replaceFragment("calendar_mode_fragment", CalendarModeFragment::newInstance)
        }

        initNotificationService()
        initButtonSetViewType()
        initCreateBtn()
        initSortMemu()
        initToolbar()
        initDrawer()
        checkNotificationEnable()
    }

    private fun initNotificationService() {
        val intent = Intent(this, MemoNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.include.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.include.btnDrawer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun initDrawer() {
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.button_privacy_policy -> {
                    val intent = Intent(this, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.button_terms_conditions -> {
                    val intent = Intent(this, TermsConditionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }


    private fun replaceFragment(tag: String, newInstance: () -> Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        for (existingFragment in fragmentManager.fragments) {
            fragmentTransaction.hide(existingFragment)
        }

        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = newInstance()
            fragmentTransaction.add(R.id.fragment_container, fragment, tag)
        } else {
            fragmentTransaction.show(fragment)
        }

        fragmentTransaction.commit()
    }

    private fun initButtonSetViewType() {
        binding.btnCalendarView.setOnClickListener {
            replaceFragment("calendar_mode_fragment", CalendarModeFragment::newInstance)
        }
        binding.btnSimpleView.setOnClickListener {
            replaceFragment("title_mode_fragment", TitleModeFragment::newInstance)
        }
        binding.btnDetailView.setOnClickListener {
            replaceFragment("detail_mode_fragment", DetailModeFragment::newInstance)
        }
    }

    private fun initCreateBtn() {
        binding.floatingActionButton.setOnClickListener {
            val navigationOptions = arrayOf(
                getString(R.string.navigation_option_memo_edit),
                getString(R.string.navigation_option_list_memo_edit)
            )

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.choose_a_screen))
                .setItems(navigationOptions) { _, which ->
                    val intent = when (navigationOptions[which]) {
                        getString(R.string.navigation_option_memo_edit) -> Intent(this, MemoEditActivity::class.java)
                        getString(R.string.navigation_option_list_memo_edit) -> Intent(this, ListMemoEditActivity::class.java)
                        else -> null
                    }
                    intent?.let { startActivity(it) }
                }
            builder.create().show()
        }
    }

    private fun initSortMemu() {
        val items = listOf(
            getString(R.string.sort_by_latest),
            getString(R.string.sort_alphabetically),
            getString(R.string.sort_by_creation_date)
        )

        val adapter = ArrayAdapter(this, R.layout.item_menu_sort, items)
        binding.textviewSort.setAdapter(adapter)
        binding.textviewSort.setText(items[0], false)

        binding.textviewSort.setOnItemClickListener { _, _, position, _ ->
            when(position) {
                0 -> {
                    EventBus.getDefault().post(SortTypeChangeEvent(1))
                }
                1 -> {
                    EventBus.getDefault().post(SortTypeChangeEvent(2))
                }
                2 -> {
                    EventBus.getDefault().post(SortTypeChangeEvent(3))
                }
            }
        }
    }


    private fun checkNotificationEnable() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val areNotificationsEnabled = notificationManager.areNotificationsEnabled()

        if (!areNotificationsEnabled) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }

            AlertDialog.Builder(this).apply {
                setMessage("메모를 상단바에 띄우기 위한 알림 권한이 필요합니다.")
                setPositiveButton("설정") { _, _ ->
                    startActivity(intent)
                }
                setNegativeButton("취소", null)
            }.show()
        }

    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshMemos()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.closeDB()
    }
}