package com.wb.logistics.ui.splash

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.wb.logistics.R
import com.wb.logistics.databinding.SplashActivityBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {

    private val splashViewModel by viewModel<SplashViewModel>()

    private lateinit var binding: SplashActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavToolbar()
    }

    private fun initNavToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_auth_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.auth_graph)
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        navController.addOnDestinationChangedListener { _, _, _ ->
            toolbarTitle.text = navController.currentDestination?.label
        }
    }

}