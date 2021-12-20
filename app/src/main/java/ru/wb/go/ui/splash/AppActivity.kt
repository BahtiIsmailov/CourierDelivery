package ru.wb.go.ui.splash

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.SplashActivityBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dialogs.InformationDialogFragment
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.SoftKeyboard
import java.util.*


class AppActivity : AppCompatActivity(), NavToolbarListener, OnFlightsStatus,
    OnUserInfo, OnCourierScanner,
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
    }

    private fun ignoreChangeToolbar() {

    }

    private fun initObserver() {

        viewModel.networkState.observe(this) {
            when (it) {
                is NetworkState.Failed -> {
                    networkIcon.visibility = VISIBLE
                    findViewById<TextView>(R.id.inet_app_status_make_text).visibility = GONE
                    findViewById<TextView>(R.id.inet_app_status_no_text).visibility = VISIBLE
                }
                is NetworkState.Complete -> {
                    networkIcon.visibility = GONE
                    findViewById<TextView>(R.id.inet_app_status_make_text).visibility = VISIBLE
                    findViewById<TextView>(R.id.inet_app_status_no_text).visibility = GONE
                }
            }
        }

        viewModel.flightsActionState.observe(this) { status ->
            LogUtils { logDebugApp(status.toString()) }
            when (status) {
                is AppUIState.NotAssigned -> flightNotAssigned(status.delivery)
                is AppUIState.Loading -> {
                    with(binding.navView) {
                        findViewById<TextView>(R.id.delivery).text = status.deliveryId
                        findViewById<TextView>(R.id.status_not_assigned).visibility = GONE
                        findViewById<TextView>(R.id.status_in_transit).visibility = GONE
                        findViewById<TextView>(R.id.status_loading_progress).visibility = VISIBLE

                        findViewById<TextView>(R.id.info_empty).visibility = GONE
                        findViewById<View>(R.id.layout_data).visibility = VISIBLE
                    }
                }
                is AppUIState.InTransit -> {
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

                        findViewById<TextView>(R.id.debt_title).setTextColor(
                            ContextCompat.getColor(
                                context, R.color.light_text
                            )
                        )
                        findViewById<TextView>(R.id.debt_text).text = status.debtBox
                        findViewById<TextView>(R.id.debt_text).setTextColor(
                            ContextCompat.getColor(
                                context, R.color.black_text
                            )
                        )
                    }
                }
                is CounterBoxesActionStatus.AcceptedDebt -> {
                    with(binding.navView) {
                        findViewById<TextView>(R.id.accepted_text).text = status.acceptedBox
                        findViewById<TextView>(R.id.return_text).text = status.returnBox
                        findViewById<TextView>(R.id.delivery_text).text = status.deliveryBox
                        findViewById<TextView>(R.id.debt_title).setTextColor(
                            ContextCompat.getColor(
                                context, R.color.icon_deny
                            )
                        )
                        findViewById<TextView>(R.id.debt_text).text = status.debtBox
                        findViewById<TextView>(R.id.debt_text).setTextColor(
                            ContextCompat.getColor(
                                context, R.color.icon_deny
                            )
                        )
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

        with(binding.navView) {
            findViewById<View>(R.id.billing_layout).setOnClickListener {
                //viewModel.onBillingClick()
                navController.navigate(R.id.courierBalanceFragment)
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
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        networkIcon = toolbar.findViewById(R.id.no_internet_image)
        networkIcon.setOnClickListener { showNetworkDialog() }
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
        InformationDialogFragment.newInstance(
            getString(R.string.nav_no_internet_dialog_title),
            getString(R.string.nav_no_internet_dialog_description),
            getString(R.string.nav_no_internet_dialog_button),
        ).show(supportFragmentManager, "TAG_NETWORK")
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
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
            .setMessage(R.string.exit_app)
            .setPositiveButton(R.string.exit_app_ok) { _, _ -> finish() }
            .setNegativeButton(R.string.exit_app_cancel) { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    override fun userInfo(name: String, company: String) {
        LogUtils { logDebugApp("authRemoteRepository.userInfo() " + name) }
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