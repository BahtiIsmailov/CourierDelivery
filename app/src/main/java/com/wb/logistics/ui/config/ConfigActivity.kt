package com.wb.logistics.ui.config

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.databinding.ConfigActivityBinding
import com.wb.logistics.ui.config.data.KeyValueDao
import com.wb.logistics.views.SelectorView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfigActivity : AppCompatActivity() {

    private val configViewModel by viewModel<ConfigViewModel>()

    private lateinit var binding: ConfigActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContent()
        initListener()
        initView()
    }

    private fun initContent() {
        binding = ConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initListener() {
        binding.authApiSelector.setItemListener(object : SelectorView.OnItemSelectListener {
            override fun onItemSelected(keyValue: KeyValueDao) {
                configViewModel.onAuthServerSelected(keyValue)
            }
        })
        binding.appApiSelector.setItemListener(object : SelectorView.OnItemSelectListener {
            override fun onItemSelected(keyValue: KeyValueDao) {
                configViewModel.onAppServerSelected(keyValue)
            }
        })
        binding.restart.setOnClickListener {
            setResult(RESULT_OK)
            configViewModel.onRestartClicked()
        }
        binding.close.setOnClickListener { finish() }
    }

    private fun initView() {
        configViewModel.authServerValues.observe(this) {
            binding.authApiSelector.initData(AppPreffsKeys.AUTH_SERVER_KEY, it)
        }
        configViewModel.authServerSelect.observe(this) {
            binding.authApiSelector.selectItem(it)
        }
        configViewModel.appServerValues.observe(this) {
            binding.appApiSelector.initData(AppPreffsKeys.APP_SERVER_KEY, it)
        }
        configViewModel.appServerSelect.observe(this) {
            binding.appApiSelector.selectItem(it)
        }
    }

}