package com.wb.logistics.ui.splash

import android.Manifest
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.UserManager
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.wb.logistics.ui.flightsloader.FlightActionStatus
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.apache.commons.net.ftp.FTPClient
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.util.*


class AppActivity : AppCompatActivity(), NavToolbarListener, OnFlightsStatus,
    OnUserInfo,
    NavDrawerListener, KeyboardListener {

    private val viewModel by viewModel<AppViewModel>()

    private lateinit var binding: SplashActivityBinding
    private lateinit var networkIcon: ImageView

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var onDestinationChangedListener: OnDestinationChangedListener

//    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
//        if (super.onPreferenceChange(preference, newValue)) return true
//        val value = newValue as Boolean
//        if (preference == enableCheckbox) {
//            if (value != adminActive) {
//                if (value) {
//                    // Launch the activity to have the user enable our admin.
//                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
//                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminSample)
//                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                            activity.getString(R.string.add_admin_extra_app_text))
//                    }
//                    startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//                    // return false - don't update checkbox until we're really active
//                    return false
//                } else {
//                    dpm.removeActiveAdmin(deviceAdminSample)
//                    enableDeviceCapabilitiesArea(false)
//                    adminActive = false
//                }
//            }
//        } else if (preference == disableCameraCheckbox) {
//            dpm.setCameraDisabled(deviceAdminSample, value)
//        }
//        return true
//    }

    private lateinit var dpm: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)

        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyDevicePolicyReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Разрешения для обновления APK") //getString(R.string.admin_explanation)

        startActivity(intent)


        if (dpm.isAdminActive(componentName)) {
            LogUtils {logDebugApp("FTP dpm.isAdminActive")}
        }
//        dpm.addUserRestriction(componentName, UserManager.DISALLOW_INSTALL_APPS)
//        dpm.addUserRestriction(componentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)

        setContentView(binding.root)
        initToolbar()
        initNavController()
        initObserver()
        initView()
        initListener()
    }

    private fun ftp() {

        try {

            LogUtils { logDebugApp("ftp() run") }
            val ftpClient = FTPClient()
//            ftpClient.localPort

            ftpClient.enterLocalPassiveMode()
            val ftpRemoteFile = "/ftp/files/debug_0.8.62_1.apk"


            //val downPath = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).absolutePath
            val downPath = cacheDir
            val apkLocalFile = File("$downPath/debug_0.8.62_1.apk")
            val outputStream: OutputStream = BufferedOutputStream(FileOutputStream(apkLocalFile))
            LogUtils { logDebugApp("ftpClient absolute local path " + apkLocalFile.absolutePath) }
            val success: Boolean = ftpClient.retrieveFile(ftpRemoteFile, outputStream)
            LogUtils { logDebugApp("ftpClient.retrieveFile " + success) }
            outputStream.close()

            //apkLocalFile.setReadable(true, false)

            if (success) {
                LogUtils { logDebugApp("ftpClient promptInstall complete") }

                val fileSize = (apkLocalFile.length() / 1024).toString().toInt()
                LogUtils { logDebugApp("ftpClient download file " + apkLocalFile.toString() + " size:" + fileSize + "Kb") }

//                val promptInstall = Intent(Intent.ACTION_VIEW)
//                    .setDataAndType(Uri.fromFile(apkLocalFile), "application/vnd.android.package-archive")
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(promptInstall)


                val pi: PackageInstaller = this.packageManager.packageInstaller
                val sessionId = pi.createSession(SessionParams(SessionParams.MODE_FULL_INSTALL))
                //clearMyRestrictions(this)
                val session = pi.openSession(sessionId)
                //addMyRestrictions(this)

                val input: InputStream?
                val output: OutputStream?

                input = FileInputStream(apkLocalFile)
                output = session.openWrite("wb_app_session", 0, apkLocalFile.length())
                var total = 0
                val buffer = ByteArray(65536)
                var c: Int
                while (input.read(buffer).also { c = it } != -1) {
                    total += c
                    output.write(buffer, 0, c)
                }

                LogUtils { logDebugApp("ftpClient output complete") }

                session.fsync(output)
                input.close()
                output.close()

                val intent = Intent(this, AppActivity::class.java)
                val alarmtest = PendingIntent.getBroadcast(this,
                    1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                session.commit(alarmtest.intentSender)
                session.close()

                //session.commit(createIntentSender(sessionId))

                //LogUtils { logDebugApp("ftpClient session.commit") }


//                val intent = Intent(this, AlarmReceiver::class.java)


            } else {
                LogUtils { logDebugApp("ftpClient promptInstall file not success") }
                ftpClient.logout()
                ftpClient.disconnect()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils { logDebugApp("ftpClient.Exception " + e.toString()) }
        }
    }

    private fun createIntentSender(sessionId: Int): IntentSender {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            sessionId,
            Intent("ACTION_INSTALL_COMPLETE"),
            0)
        return pendingIntent.intentSender
    }


    fun getDpm(context: Context): DevicePolicyManager {
        return context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    fun getAdmin(context: Context): ComponentName {
        return ComponentName(context, MyDevicePolicyReceiver::class.java)
    }


    fun clearMyRestrictions(context: Context) {
        //getDpm(context).clearUserRestriction(getAdmin(context), UserManager.DISALLOW_INSTALL_APPS)
        //getDpm(context).clearUserRestriction(getAdmin(context), UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
    }

    fun addMyRestrictions(context: Context) {
        getDpm(context).addUserRestriction(getAdmin(context), UserManager.DISALLOW_INSTALL_APPS)
        getDpm(context).addUserRestriction(getAdmin(context), UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
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
                findViewById<TextView>(R.id.version_app_text).text = it
            }
        }
    }

    private fun initListener() {

        binding.navView.findViewById<View>(R.id.ver_layout).setOnClickListener {


            if (hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                Completable.fromAction { ftp() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            } else {
                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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


    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

            } else {

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