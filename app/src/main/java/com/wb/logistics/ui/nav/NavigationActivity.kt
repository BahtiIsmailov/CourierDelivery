package com.wb.logistics.ui.nav

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
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
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.flights.FlightsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class NavigationActivity : AppCompatActivity(), FlightsFragment.OnFlightsCount {

    private val navigationViewModel by viewModel<NavigationViewModel>()

    private lateinit var binding: NavigationActivityBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var networkIcon: ImageView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NavigationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initStatusBar()
        initToolbar()
        initNavController()
        initView()
        initObserver()
    }

    private fun initObserver() {
        navigationViewModel.navHeader.observe(this) {
            val header: View = binding.navView.getHeaderView(0)
            header.findViewById<TextView>(R.id.nav_header_name).text = it.first
            header.findViewById<TextView>(R.id.nav_header_company).text = it.second
        }
        navigationViewModel.networkState.observe(this) {
            networkIcon.visibility = if (it) GONE else VISIBLE
        }
        navigationViewModel.versionApp.observe(this) {
            binding.versionAppText.text = it
        }
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
                R.id.flightsFragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.exitAppLayout.setOnClickListener { finish() }
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        networkIcon = toolbar.findViewById<ImageView>(R.id.no_internet_image)
        networkIcon.setOnClickListener {
            InformationDialogFragment.newInstance(
                getString(R.string.nav_no_internet_title),
                getString(R.string.nav_no_internet_description),
                getString(R.string.nav_no_internet_button),
            ).show(supportFragmentManager, "TAG")
        }
    }

    private fun setMenuCounter(@IdRes itemId: Int, count: String) {
        val view = binding.navView.menu.findItem(itemId).actionView
        val counter = view.findViewById<TextView>(R.id.counter_flight_text)
        counter.text = count
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun flightCount(count: String) {
        setMenuCounter(getMenuIds().first(), count)
    }

    private fun getMenuIds(): List<Int> {
        val menuItemId: MutableList<Int> = ArrayList()
        val menu = binding.navView.menu
        for (i in 0 until menu.size()) {
            menuItemId.add(menu.getItem(i).itemId)
        }
        return menuItemId
    }

}