package ru.wb.go.ui.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color

import android.os.Build
import android.os.Bundle
import android.view.Gravity.LEFT
import android.view.View
import android.view.View.*
import android.view.WindowManager.LayoutParams.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.SplashActivityBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.courierdata.CourierDataFragmentDirections
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.SoftKeyboard


class AppActivity : AppCompatActivity(), NavToolbarListener,
    OnUserInfo, OnCourierScanner, StatusBarListener,
    NavDrawerListener, KeyboardListener, DialogConfirmInfoFragment.SimpleDialogListener {

    private val viewModel by viewModel<AppViewModel>()

    private lateinit var binding: SplashActivityBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var onDestinationChangedListener: NavController.OnDestinationChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initNavController()
        initObserver()
        initListener()
        hideStatusBar()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = binding.layoutHost.toolbarLayout.toolbar
        setSupportActionBar(toolbar)
        binding.layoutHost.toolbarLayout.toolbarTitle.text = toolbar.title
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun initNavController() {
        binding.navView.itemIconTintList = null
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        onDestinationChangedListener =
            NavController.OnDestinationChangedListener { _: NavController, navDestination: NavDestination, _: Bundle? ->
                when (navDestination.id) {
                    else -> {
                        updateTitle(navDestination.label.toString())
                    }
                }
            }
        navController.addOnDestinationChangedListener(onDestinationChangedListener)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.courierWarehousesFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun initObserver() {

        viewModel.networkState.observe(this) {

            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.layoutHost.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(this, ic)
            )

            when (it) {
                is NetworkState.Failed -> {
                    binding.navigationHeaderMain.inetAppStatusMakeText.visibility = GONE
                    binding.navigationHeaderMain.inetAppStatusNoText.visibility = VISIBLE
                }
                is NetworkState.Complete -> {
                    binding.navigationHeaderMain.inetAppStatusMakeText.visibility = VISIBLE
                    binding.navigationHeaderMain.inetAppStatusNoText.visibility = GONE
                }
            }
        }

        viewModel.versionApp.observe(this) {
            binding.layoutHost.toolbarLayout.toolbarVersion.text = it
            binding.navigationHeaderMain.versionApp.text = it
        }

    }

    private fun initListener() {

        with(binding.navView) {
            findViewById<View>(R.id.billing_layout).setOnClickListener {
                navController.navigate(R.id.courierBalanceFragment)
            }

            findViewById<View>(R.id.logout_layout).setOnClickListener {
                viewModel.onExitClick()
                panMode()
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                Navigation.findNavController(this@AppActivity, R.id.nav_host_fragment)
                    .navigate(R.id.load_navigation)
            }
            
            findViewById<View>(R.id.settings_layout).setOnClickListener {
                navController.navigate(R.id.settingsFragment)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
                SoftKeyboard.hideKeyBoard(this@AppActivity)
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun updateTitle(title: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val toolbarTitle = toolbar.findViewById<View>(R.id.toolbar_title) as TextView
        toolbarTitle.text = title
    }

    override fun hideToolbar() {
        val toolbarLayout = findViewById<RelativeLayout>(R.id.toolbar_layout)
        toolbarLayout.visibility = GONE
    }

    override fun showToolbar() {
        val toolbarLayout = findViewById<RelativeLayout>(R.id.toolbar_layout)
        toolbarLayout.visibility = VISIBLE
    }

    override fun showStatusBar() {

    }

    override fun hideStatusBar() {
        makeStatusBarTransparent()
    }

    override fun showNetworkDialog() {
        DialogInfoFragment.newInstance(
            type = DialogInfoStyle.WARNING.ordinal,
            title = getString(R.string.nav_no_internet_dialog_title),
            message = getString(R.string.nav_no_internet_dialog_description),
            positiveButtonName = getString(R.string.nav_no_internet_dialog_button),
        ).show(supportFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
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

    private var isLoadingCourierBox = false

    override fun onBackPressed() {
        when (findNavController(R.id.nav_host_fragment).currentDestination?.id) {
            R.id.authNumberPhoneFragment -> finish()
            R.id.userFormFragment -> findNavController(R.id.nav_host_fragment).navigate(
                CourierDataFragmentDirections.actionUserFormFragmentToAuthNavigation()
            )
            R.id.couriersCompleteRegistrationFragment, R.id.courierWarehousesFragment,
            R.id.courierUnloadingScanFragment, R.id.courierIntransitFragment,
            R.id.courierOrderTimerFragment, R.id.courierStartDeliveryFragment -> showExitDialog()
            R.id.courierScannerLoadingScanFragment -> {
                if (isLoadingCourierBox) showExitDialog() else super.onBackPressed()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun showExitDialog() {
        DialogConfirmInfoFragment.newInstance(
            resultTag = EXIT_DIALOG_TAG,
            type = DialogInfoStyle.INFO.ordinal,
            title = getText(R.string.exit_app).toString(),
            message = "",
            positiveButtonName = getText(R.string.ok_button_title).toString(),
            negativeButtonName = getText(R.string.exit_app_cancel).toString()
        ).show(supportFragmentManager, DIALOG_CONFIRM_INFO_TAG)
    }

    override fun userInfo(name: String, company: String) {
        with(binding.navView) {
            findViewById<TextView>(R.id.nav_header_name).text = name
        }
    }

    override fun lockNavDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun unlockNavDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    @SuppressLint("RtlHardcoded")
    override fun showNavDrawer() {
        binding.drawerLayout.openDrawer(LEFT)
    }

    override fun adjustMode() {
        window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun panMode() {
        window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun holdBackButtonOnScanBox() {
        isLoadingCourierBox = true
    }

    companion object {
        private const val EXIT_DIALOG_TAG = "EXIT_DIALOG_TAG"
    }

    override fun onPositiveDialogClick(resultTag: String) {
        if (resultTag == EXIT_DIALOG_TAG)
            finish()
    }

    override fun onNegativeDialogClick(resultTag: String) {

    }

}

interface NavToolbarListener {
    fun leftIcon(@DrawableRes resId: Int)
    fun backButtonIcon(@DrawableRes resId: Int)
    fun hideBackButton()
    fun updateTitle(title: String)
    fun hideToolbar()
    fun showToolbar()
    fun showStatusBar()
    fun showNetworkDialog()
}

interface NavDrawerListener {
    fun lockNavDrawer()
    fun unlockNavDrawer()
    fun showNavDrawer()
}

interface StatusBarListener {
    fun showStatusBar()
    fun hideStatusBar()
}

interface KeyboardListener {
    fun adjustMode()
    fun panMode()
}

interface OnUserInfo {
    fun userInfo(name: String, company: String)
}

interface OnCourierScanner {
    fun holdBackButtonOnScanBox()
}

fun AppActivity.hasPermissions(vararg permissions: String): Boolean =
    permissions.all(::hasPermission)

fun AppActivity.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.makeStatusBarTransparent() {
    window.apply {
        clearFlags(FLAG_TRANSLUCENT_STATUS)
        addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        statusBarColor = Color.TRANSPARENT
    }
}