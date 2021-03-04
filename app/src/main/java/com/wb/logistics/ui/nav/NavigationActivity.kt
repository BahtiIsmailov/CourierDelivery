package com.wb.logistics.ui.nav

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.NavigationActivityBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class NavigationActivity : AppCompatActivity() {

    private val navigationViewModel by viewModel<NavigationViewModel>()

    private lateinit var binding: NavigationActivityBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NavigationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initStatusBar()
        initToolbar()
        initNavController()
        initView()
    }

    private fun initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun initNavController() {
        binding.navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.exitAppLayout.setOnClickListener { finish() }
    }

    private fun initView() {
        navigationViewModel.countFlight.observe(this) {
            setMenuCounter(getMenuIds().first(), it)
        }
        navigationViewModel.versionApp.observe(this) {
            binding.versionAppText.text = it
        }
    }

    private fun setMenuCounter(@IdRes itemId: Int, count: String) {
        val view = binding.navView.menu.findItem(itemId).actionView
        val counter = view.findViewById<TextView>(R.id.counter_flight_text)
        counter.text = count
    }

    private fun getMenuIds(): List<Int> {
        val menuItemId: MutableList<Int> = ArrayList()
        val menu = binding.navView.menu
        for (i in 0 until menu.size()) {
            menuItemId.add(menu.getItem(i).itemId)
        }
        return menuItemId
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}