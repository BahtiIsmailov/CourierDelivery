package com.wb.logistics.ui.splash

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.SplashActivityBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class AppActivity : AppCompatActivity(), NavToolbarListener, OnFlightsCount, OnUserInfo,
    NavDrawerListener, KeyboardListener {

    private val viewModel by viewModel<AppViewModel>()

    private lateinit var binding: SplashActivityBinding
    private lateinit var networkIcon: ImageView

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var onDestinationChangedListener: OnDestinationChangedListener

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

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val title = toolbar.findViewById<View>(R.id.toolbar_title) as TextView
        setSupportActionBar(toolbar)
        title.text = toolbar.title
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun initNavController() {
        binding.navView.itemIconTintList = null
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_auth_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        onDestinationChangedListener =
            OnDestinationChangedListener { _: NavController, navDestination: NavDestination, _: Bundle? ->
                when (navDestination.id) {
                    R.id.unloadingScanFragment -> ignoreChangeToolbar()
                    else -> {
                        updateTitle(navDestination.label.toString())
                    }
                }
            }
        navController.addOnDestinationChangedListener(onDestinationChangedListener)

        appBarConfiguration =
            AppBarConfiguration(setOf(R.id.flightsFragment, R.id.flightsEmptyFragment),
                binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun ignoreChangeToolbar() {

    }

    private fun initObserver() {

        viewModel.networkState.observe(this) {
            networkIcon.visibility = if (it) GONE else VISIBLE
            val header: View = binding.navView.getHeaderView(0)
            val status = when (it) {
                true -> getString(R.string.inet_ok)
                false -> getString(R.string.inet_no)
            }
            header.findViewById<TextView>(R.id.inet_app_text).text =
                String.format("%s: %s", getString(R.string.inet_text), status)

        }

        viewModel.versionApp.observe(this) {
            val header: View = binding.navView.getHeaderView(0)
            header.findViewById<TextView>(R.id.version_app_text).text = it
        }
    }

    private fun initListener() {
        binding.logoutLayout.setOnClickListener {
            viewModel.onExitClick()
            panMode()
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            Navigation.findNavController(this, R.id.nav_auth_host_fragment)
                .navigate(R.id.load_navigation)
        }
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
        val toolbarTitle = toolbar.findViewById<View>(R.id.toolbar_title) as TextView
        toolbarTitle.text = title
    }

    override fun hideToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = GONE
    }

    override fun showToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = VISIBLE
    }

    override fun leftIcon(resId: Int) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val leftIcon = toolbar.findViewById<ImageView>(R.id.left_icon)
        leftIcon.setImageResource(resId)
    }

    override fun backButtonIcon(resId: Int) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(resId)
    }

    override fun hideBackButton() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.navigationIcon = null
    }

    override fun onBackPressed() {
        when (findNavController(R.id.nav_auth_host_fragment).currentDestination?.id) {
            R.id.authNumberPhoneFragment -> finish()
            R.id.flightsEmptyFragment, R.id.flightsFragment, R.id.flightDeliveriesFragment, R.id.congratulationFragment -> {
                showExitDialog()
            }
            R.id.unloadingScanFragment -> {
                // TODO: 01.07.2021 выполнить запрос, получить наличие выгружаемых коробок > 0, то выход иначе super.onBackPressed()
            }
            else -> {

                super.onBackPressed()
            }
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
            .setMessage(R.string.exit_app)
            .setPositiveButton(R.string.exit_app_ok) { _, _ -> finish() }
            .setNegativeButton(R.string.exit_app_cancel) { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    override fun flightCount(name: String, company: String) {
        val header: View = binding.navView.getHeaderView(0)
        header.findViewById<TextView>(R.id.nav_header_name).text = name
        header.findViewById<TextView>(R.id.nav_header_company).text = company
    }

    override fun lock() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun unlock() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

    }

    override fun adjustMode() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun panMode() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

}

interface NavToolbarListener {
    fun leftIcon(@DrawableRes resId: Int)
    fun backButtonIcon(@DrawableRes resId: Int)
    fun hideBackButton()
    fun updateTitle(title: String)
    fun hideToolbar()
    fun showToolbar()
}

interface NavDrawerListener {
    fun lock()
    fun unlock()
}

interface KeyboardListener {
    fun adjustMode()
    fun panMode()
}

interface OnUserInfo {
    fun flightCount(name: String, company: String)
}

interface OnFlightsCount {
    fun flightCount(count: String)
}