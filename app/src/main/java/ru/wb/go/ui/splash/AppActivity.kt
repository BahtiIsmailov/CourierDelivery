package ru.wb.go.ui.splash

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import ru.wb.go.BuildConfig
import ru.wb.go.R
import ru.wb.go.databinding.SplashActivityBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.auth.AppVersionState
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.SoftKeyboard
import java.io.*
import java.util.*


class AppActivity : AppCompatActivity(), NavToolbarListener,
    OnFlightsStatus, OnUserInfo, OnCourierScanner, OnSoundPlayer,
    NavDrawerListener, KeyboardListener, DialogConfirmInfoFragment.SimpleDialogListener {

    private val viewModel by viewModel<AppViewModel>()

    private lateinit var binding: SplashActivityBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var onDestinationChangedListener: NavController.OnDestinationChangedListener

    private val player = MediaPlayer()

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
        val toolbar: Toolbar = binding.layoutHost.toolbarLayout.toolbar
        setSupportActionBar(toolbar)
        binding.layoutHost.toolbarLayout.toolbarTitle.text = toolbar.title
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun initNavController() {
        binding.navView.itemIconTintList = null
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_auth_host_fragment) as NavHostFragment

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
            setOf(R.id.courierWarehouseFragment),
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
        }

        viewModel.flightsActionState.observe(this) { status ->
            LogUtils { logDebugApp(status.toString()) }
            when (status) {
                is AppUIState.NotAssigned -> flightNotAssigned(status.delivery)
                is AppUIState.Loading -> {
                    with(binding.navigationHeaderMain) {
                        deliveryText.text = status.deliveryId
                        statusNotAssigned.visibility = GONE
                        statusInTransit.visibility = GONE
                        statusLoadingProgress.visibility = VISIBLE

                        infoEmpty.visibility = GONE
                        layoutData.visibility = VISIBLE
                    }
                }
                is AppUIState.InTransit -> {
                    with(binding.navigationHeaderMain) {
                        delivery.text = status.deliveryId
                        statusNotAssigned.visibility = GONE
                        statusInTransit.visibility = VISIBLE
                        statusLoadingProgress.visibility = GONE

                        infoEmpty.visibility = GONE
                        layoutData.visibility = VISIBLE
                    }
                }
            }
        }

        viewModel.counterBoxesActionStatus.observe(this) { status ->
            when (status) {
                is CounterBoxesActionStatus.Accepted -> {
                    with(binding.navigationHeaderMain) {
                        acceptedText.text = status.acceptedBox
                        returnText.text = status.returnBox
                        deliveryText.text = status.deliveryBox

                        debtTitle.setTextColor(
                            ContextCompat.getColor(this@AppActivity, R.color.light_text)
                        )
                        debtText.text = status.debtBox
                        debtText.setTextColor(
                            ContextCompat.getColor(this@AppActivity, R.color.black_text)
                        )
                    }
                }
                is CounterBoxesActionStatus.AcceptedDebt -> {
                    with(binding.navigationHeaderMain) {
                        acceptedText.text = status.acceptedBox
                        returnText.text = status.returnBox
                        deliveryText.text = status.deliveryBox
                        debtTitle.setTextColor(
                            ContextCompat.getColor(this@AppActivity, R.color.icon_deny)
                        )
                        debtText.text = status.debtBox
                        debtText.setTextColor(
                            ContextCompat.getColor(this@AppActivity, R.color.icon_deny)
                        )
                    }
                }
            }
        }

        viewModel.versionApp.observe(this) {
            with(binding.navigationHeaderMain) {
                versionApp.text = it
            }
        }

        viewModel.appVersionState.observe(this) {
            when (it) {
                is AppVersionState.Update -> {
                    with(binding.navigationHeaderMain) {
                        checkVersionLayout.visibility = GONE
                        updateVersionLayout.isEnabled = true
                        updateVersionLayout.visibility = VISIBLE
                        updateImage.visibility = VISIBLE
                        updateProgress.visibility = INVISIBLE
                        currentVersionUpdateTitle.text =
                            getString(
                                R.string.app_update_version,
                                it.fileName.substringBefore(".apk")
                            )
                        updateVersionApp.text = getString(R.string.app_update_version_status)
                    }
                }
                AppVersionState.UpdateProgress -> {
                    with(binding.navigationHeaderMain) {
                        checkVersionLayout.visibility = GONE
                        updateVersionLayout.isEnabled = false
                        updateVersionLayout.visibility = VISIBLE
                        updateImage.visibility = INVISIBLE
                        updateProgress.visibility = VISIBLE
                        updateVersionApp.text = getString(R.string.app_update_version_load_status)
                    }
                }
                is AppVersionState.UpdateComplete -> showInstallOption(it.pathFile)
                is AppVersionState.UpdateError -> {
                    with(binding.navigationHeaderMain) {
                        updateVersionLayout.visibility = GONE
                        checkVersionLayout.isEnabled = true
                        checkVersionLayout.visibility = VISIBLE
                        checkVersion.visibility = VISIBLE
                        progressCheckUpdate.visibility = INVISIBLE
                        Toast.makeText(
                            this@AppActivity,
                            getString(R.string.app_update_version_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
                is AppVersionState.UpToDate -> {
                    with(binding.navigationHeaderMain) {
                        updateVersionLayout.visibility = GONE
                        checkVersionLayout.isEnabled = true
                        checkVersionLayout.visibility = VISIBLE
                        checkVersion.visibility = VISIBLE
                        progressCheckUpdate.visibility = INVISIBLE
                        Toast.makeText(
                            this@AppActivity,
                            getString(R.string.app_update_version_up_to_date),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                AppVersionState.UpToDateProgress -> {
                    with(binding.navigationHeaderMain) {
                        updateVersionLayout.visibility = GONE
                        checkVersionLayout.isEnabled = false
                        checkVersionLayout.visibility = VISIBLE
                        checkVersion.visibility = INVISIBLE
                        progressCheckUpdate.visibility = VISIBLE
                    }
                }
            }
        }
    }

    private fun initListener() {

        with(binding.navView) {
            findViewById<View>(R.id.billing_layout).setOnClickListener {
                //viewModel.onBillingClick()
                navController.navigate(R.id.courierBalanceFragment)
            }
        }

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

//        binding.layoutHost.toolbarLayout.search.setOnQueryTextListener(object :
//            SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String): Boolean {
//                viewModel.onSearchChange(newText)
//                return false
//            }
//
//            override fun onQueryTextSubmit(query: String): Boolean {
//                viewModel.onSearchChange(query)
//                return false
//            }
//        })
//        binding.layoutHost.toolbarLayout.search.setBackgroundResource(R.drawable.rounded_search_layout)

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

    private fun initView() {
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        networkIcon = toolbar.findViewById(R.id.no_internet_image)
        binding.layoutHost.toolbarLayout.noInternetImage.setOnClickListener { showNetworkDialog() }
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
        val toolbarLayout = findViewById<RelativeLayout>(R.id.toolbar_layout)
        toolbarLayout.visibility = GONE
    }

    override fun showToolbar() {
        val toolbarLayout = findViewById<RelativeLayout>(R.id.toolbar_layout)
        toolbarLayout.visibility = VISIBLE
    }

//    override fun showToolbarSearch() {
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        toolbar.visibility = VISIBLE
//    }

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
        when (findNavController(R.id.nav_auth_host_fragment).currentDestination?.id) {
            R.id.authNumberPhoneFragment -> finish()
            R.id.couriersCompleteRegistrationFragment, R.id.courierWarehouseFragment,
            R.id.courierUnloadingScanFragment, R.id.courierIntransitFragment -> showExitDialog()
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
            positiveButtonName = getText(R.string.exit_app_ok).toString(),
            negativeButtonName = getText(R.string.exit_app_cancel).toString()
        ).show(supportFragmentManager, DIALOG_CONFIRM_INFO_TAG)
    }

    override fun userInfo(name: String, company: String) {
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

    override fun holdBackButtonOnScanBox() {
        isLoadingCourierBox = true
    }

    override fun play(resId: Int) {
        val source =
            Uri.parse("android.resource://$packageName/raw/$resId")
        player.reset()
        player.setDataSource(this, source)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.prepare()
        player.start()
    }

    companion object {
        const val PERMISSION_EXT_STORAGE_REQUEST_CODE = 500
        private const val FILE_BASE_PATH = "file://"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
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
    fun showNetworkDialog()
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
    fun userInfo(name: String, company: String)
}

interface OnFlightsStatus {
    fun flightNotAssigned(delivery: String)
}

interface OnCourierScanner {
    fun holdBackButtonOnScanBox()
}

interface OnSoundPlayer {
    fun play(resId: Int)
}

fun AppActivity.hasPermissions(vararg permissions: String): Boolean =
    permissions.all(::hasPermission)

fun AppActivity.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}