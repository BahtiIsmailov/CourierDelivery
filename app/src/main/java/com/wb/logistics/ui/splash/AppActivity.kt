package com.wb.logistics.ui.splash

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.wb.logistics.BuildConfig
import com.wb.logistics.R
import com.wb.logistics.databinding.SplashActivityBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.flightsloader.FlightActionStatus
import com.wb.logistics.utils.LogUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.util.*


class AppActivity : AppCompatActivity(), NavToolbarListener, OnFlightsStatus,
    OnUserInfo, NavDrawerListener, KeyboardListener {

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

    private fun showInstallOption(destination: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                File(destination)
            )
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = contentUri
            this.startActivity(install)
            finish()
        } else {
            val uri = Uri.parse("$FILE_BASE_PATH$destination")
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(uri, APP_INSTALL_PATH)
            this.startActivity(install)
            finish()
        }
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
            AppBarConfiguration(setOf(R.id.flightsFragment,
                R.id.flightsErrorFragment,
                R.id.flightsEmptyFragment),
                binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun ignoreChangeToolbar() {

    }

    private fun initObserver() {

        viewModel.networkState.observe(this) {
            networkIcon.visibility = if (it) GONE else VISIBLE
            with(binding.navView) {
                if (it) {
                    findViewById<TextView>(R.id.inet_app_status_make_text).visibility = VISIBLE
                    findViewById<TextView>(R.id.inet_app_status_no_text).visibility = GONE
                } else {
                    findViewById<TextView>(R.id.inet_app_status_make_text).visibility = GONE
                    findViewById<TextView>(R.id.inet_app_status_no_text).visibility = VISIBLE
                }
            }
        }

        viewModel.flightsActionState.observe(this) { status ->
            LogUtils { logDebugApp(status.toString()) }
            when (status) {
                is FlightActionStatus.NotAssigned -> flightNotAssigned(status.delivery)
                is FlightActionStatus.Loading -> {
                    with(binding.navView) {
                        findViewById<TextView>(R.id.delivery).text = status.deliveryId
                        findViewById<TextView>(R.id.status_not_assigned).visibility = GONE
                        findViewById<TextView>(R.id.status_in_transit).visibility = GONE
                        findViewById<TextView>(R.id.status_loading_progress).visibility = VISIBLE

                        findViewById<TextView>(R.id.info_empty).visibility = GONE
                        findViewById<View>(R.id.layout_data).visibility = VISIBLE
                    }
                }
                is FlightActionStatus.InTransit -> {
                    with(binding.navView) {
                        findViewById<TextView>(R.id.delivery).text = status.deliveryId
                        findViewById<TextView>(R.id.status_not_assigned).visibility = GONE
                        findViewById<TextView>(R.id.status_in_transit).visibility = VISIBLE
                        findViewById<TextView>(R.id.status_loading_progress).visibility = GONE

                        findViewById<TextView>(R.id.info_empty).visibility = GONE
                        findViewById<View>(R.id.layout_data).visibility = VISIBLE
                    }
                }
            }
        }

        viewModel.counterBoxesActionStatus.observe(this) { status ->
            when (status) {
                is CounterBoxesActionStatus.Accepted -> {
                    with(binding.navView) {
                        findViewById<TextView>(R.id.accepted_text).text = status.acceptedBox
                        findViewById<TextView>(R.id.return_text).text = status.returnBox
                        findViewById<TextView>(R.id.delivery_text).text = status.deliveryBox

                        findViewById<TextView>(R.id.debt_title).setTextColor(ContextCompat.getColor(
                            context, R.color.light_text))
                        findViewById<TextView>(R.id.debt_text).text = status.debtBox
                        findViewById<TextView>(R.id.debt_text).setTextColor(ContextCompat.getColor(
                            context, R.color.black_text))
                    }
                }
                is CounterBoxesActionStatus.AcceptedDebt -> {
                    with(binding.navView) {
                        findViewById<TextView>(R.id.accepted_text).text = status.acceptedBox
                        findViewById<TextView>(R.id.return_text).text = status.returnBox
                        findViewById<TextView>(R.id.delivery_text).text = status.deliveryBox
                        findViewById<TextView>(R.id.debt_title).setTextColor(ContextCompat.getColor(
                            context, R.color.icon_deny))
                        findViewById<TextView>(R.id.debt_text).text = status.debtBox
                        findViewById<TextView>(R.id.debt_text).setTextColor(ContextCompat.getColor(
                            context, R.color.icon_deny))
                    }
                }
            }
        }

        viewModel.versionApp.observe(this) {
            with(binding.navView) {
                findViewById<TextView>(R.id.version_app).text = it
            }
        }

        viewModel.appVersionState.observe(this) {
            when (it) {
                is AppVersionState.Update -> {
                    findViewById<View>(R.id.check_version_layout).visibility = GONE
                    findViewById<View>(R.id.update_version_layout).isEnabled = true
                    findViewById<View>(R.id.update_version_layout).visibility = VISIBLE
                    findViewById<ImageView>(R.id.update_image).visibility = VISIBLE
                    findViewById<ProgressBar>(R.id.update_progress).visibility = INVISIBLE
                    findViewById<TextView>(R.id.current_version_update_title).text =
                        getString(R.string.app_update_version,
                            it.fileName.substringBefore(".apk"))
                    findViewById<TextView>(R.id.update_version_app).text =
                        getString(R.string.app_update_version_status)

                }
                AppVersionState.UpdateProgress -> {
                    findViewById<View>(R.id.check_version_layout).visibility = GONE
                    findViewById<View>(R.id.update_version_layout).isEnabled = false
                    findViewById<View>(R.id.update_version_layout).visibility = VISIBLE
                    findViewById<ImageView>(R.id.update_image).visibility = INVISIBLE
                    findViewById<ProgressBar>(R.id.update_progress).visibility = VISIBLE
                    findViewById<TextView>(R.id.update_version_app).text =
                        getString(R.string.app_update_version_load_status)
                }
                is AppVersionState.UpdateComplete -> showInstallOption(it.pathFile)
                is AppVersionState.UpdateError -> {
                    findViewById<View>(R.id.update_version_layout).visibility = GONE
                    findViewById<View>(R.id.check_version_layout).isEnabled = true
                    findViewById<View>(R.id.check_version_layout).visibility = VISIBLE
                    findViewById<ImageView>(R.id.check_version).visibility = VISIBLE
                    findViewById<ProgressBar>(R.id.progress_check_update).visibility = INVISIBLE
                    Toast.makeText(this,
                        getString(R.string.app_update_version_error),
                        Toast.LENGTH_LONG).show()
                }
                is AppVersionState.UpToDate -> {
                    findViewById<View>(R.id.update_version_layout).visibility = GONE
                    findViewById<View>(R.id.check_version_layout).isEnabled = true
                    findViewById<View>(R.id.check_version_layout).visibility = VISIBLE
                    findViewById<ImageView>(R.id.check_version).visibility = VISIBLE
                    findViewById<ProgressBar>(R.id.progress_check_update).visibility = INVISIBLE
                    Toast.makeText(this,
                        getString(R.string.app_update_version_up_to_date),
                        Toast.LENGTH_LONG).show()
                }
                AppVersionState.UpToDateProgress -> {
                    findViewById<View>(R.id.update_version_layout).visibility = GONE
                    findViewById<View>(R.id.check_version_layout).isEnabled = false
                    findViewById<View>(R.id.check_version_layout).visibility = VISIBLE
                    findViewById<ImageView>(R.id.check_version).visibility = INVISIBLE
                    findViewById<ProgressBar>(R.id.progress_check_update).visibility = VISIBLE
                }

            }
        }
    }

    private fun initListener() {

        binding.navView.findViewById<View>(R.id.check_version_layout).setOnClickListener {
            viewModel.checkUpdateVersionApp()
        }

        binding.navView.findViewById<View>(R.id.update_version_layout).setOnClickListener {
            if (hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                updateVersionApp()
            } else {
                requestPermissionExtStorage()
            }
        }

        with(binding.navView) {
            findViewById<View>(R.id.logout_layout).setOnClickListener {
                viewModel.onExitClick()
                panMode()
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                Navigation.findNavController(this@AppActivity, R.id.nav_auth_host_fragment)
                    .navigate(R.id.load_navigation)
            }
        }

    }

    private fun requestPermissionExtStorage() {
        requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun updateVersionApp() {
        val destination = "$cacheDir/"
        viewModel.updateVersionApp(destination)
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                updateVersionApp()
            } else {
                // TODO: 06.08.2021 добавить разметку перехода в настройки разрешений
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                        val uri = Uri.fromParts(AppConsts.APP_PACKAGE, packageName, null)
//                        intent.data = uri
//                        startActivityForResult(intent, PERMISSION_EXT_STORAGE_REQUEST_CODE)
//                    }
//                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_EXT_STORAGE_REQUEST_CODE) {
            requestPermissionExtStorage()
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
            R.id.flightsEmptyFragment, R.id.flightsErrorFragment, R.id.flightsFragment, R.id.flightDeliveriesFragment, R.id.congratulationFragment -> {
                showExitDialog()
            }
            R.id.unloadingScanFragment -> {
                val toolbar = findViewById<Toolbar>(R.id.toolbar)
                if (toolbar.navigationIcon == null) {
                    showExitDialog()
                } else {
                    super.onBackPressed()
                }
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

    override fun flightUserInfo(name: String, company: String) {
        with(binding.navView) {
            findViewById<TextView>(R.id.nav_header_name).text = name
            findViewById<TextView>(R.id.nav_header_company).text = company
        }
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

    override fun flightNotAssigned(delivery: String) {
        with(binding.navView) {
            findViewById<TextView>(R.id.delivery).text = delivery
            findViewById<TextView>(R.id.status_not_assigned).visibility = VISIBLE
            findViewById<TextView>(R.id.status_in_transit).visibility = GONE
            findViewById<TextView>(R.id.status_loading_progress).visibility = GONE

            findViewById<TextView>(R.id.info_empty).visibility = VISIBLE
            findViewById<View>(R.id.layout_data).visibility = GONE
        }
    }

    companion object {
        const val PERMISSION_EXT_STORAGE_REQUEST_CODE = 500
        private const val FILE_BASE_PATH = "file://"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
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
    fun flightUserInfo(name: String, company: String)
}

interface OnFlightsStatus {
    fun flightNotAssigned(delivery: String)
}

fun AppActivity.hasPermissions(vararg permissions: String): Boolean =
    permissions.all(::hasPermission)

fun AppActivity.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}