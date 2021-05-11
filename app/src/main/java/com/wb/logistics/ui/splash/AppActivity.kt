package com.wb.logistics.ui.splash

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.SplashActivityBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.flights.FlightsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AppActivity : AppCompatActivity(), NavToolbarTitleListener, FlightsFragment.OnFlightsCount {

    private val viewModel by viewModel<AppViewModel>()

    private lateinit var binding: SplashActivityBinding
    private lateinit var networkIcon: ImageView

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initNavController()
        initObserver()
        initView()
        initListener()
    }

    private fun initListener() {
        binding.exitAppLayout.setOnClickListener {
            viewModel.onExitClick()
            finish()
        }
    }

    private fun initObserver() {

        viewModel.navHeader.observe(this) {
            val header: View = binding.navView.getHeaderView(0)
            header.findViewById<TextView>(R.id.nav_header_name).text = it.first
            header.findViewById<TextView>(R.id.nav_header_company).text = it.second
        }

        viewModel.networkState.observe(this) {
            networkIcon.visibility = if (it) View.GONE else View.VISIBLE
        }

        viewModel.versionApp.observe(this) {
            binding.versionAppText.text = it
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        networkIcon = toolbar.findViewById(R.id.no_internet_image)
        networkIcon.setOnClickListener {
            InformationDialogFragment.newInstance(
                getString(R.string.nav_no_internet_dialog_title),
                getString(R.string.nav_no_internet_dialog_description),
                getString(R.string.nav_no_internet_dialog_button),
            ).show(supportFragmentManager, "TAG_NETWORK")
        }
    }

    private fun initNavController() {
        binding.navView.itemIconTintList = null
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_auth_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.flightsFragment), binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
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

    private fun setMenuCounter(@IdRes itemId: Int, count: String) {
        val view = binding.navView.menu.findItem(itemId).actionView
        val counter = view.findViewById<TextView>(R.id.counter_flight_text)
        counter.text = count
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_auth_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun updateTitle(title: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = title
    }

    override fun updateDrawer() {
        viewModel.updateDrawer()
    }

    override fun backButtonIcon(resId: Int) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(resId)
    }

    override fun hideBackButton() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.navigationIcon = null
    }

}

interface NavToolbarTitleListener {

    fun backButtonIcon(@DrawableRes resId: Int)

    fun hideBackButton()

    fun updateTitle(title: String)

    fun updateDrawer()

}