package ru.wb.go.ui.courieragreement

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierAgreementFragmentBinding
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener


class CourierAgreementFragment : Fragment(R.layout.courier_agreement_fragment) {

    private var _binding: CourierAgreementFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<CourierAgreementViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierAgreementFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initAgreement()
        initListeners()
    }

    private fun initListeners() {
        binding.confirm.setOnClickListener { viewModel.onCompleteClick() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initAgreement() {
        with(binding.webBrowser){
            settings.javaScriptEnabled = true
            setInitialScale(1)
            settings.builtInZoomControls = true
            loadUrl(URL_FOR_VEB_VIEW,mutableMapOf("Authorization" to "Bearer test123123"))
            clearCache(true)
            clearFormData()
            clearHistory()
            clearSslPreferences()
            context.deleteDatabase("webview.db")
            context.deleteDatabase("webviewCache.db")
            binding.viewProgress.visibility = GONE
        }
    }

    private fun initView() {
        (activity as NavDrawerListener).lockNavDrawer()
        (activity as NavToolbarListener).hideBackButton()
    }

    private fun initObserver() {
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierAgreementNavigationState.Complete -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
    companion object{
        const val URL_FOR_VEB_VIEW = "https://wbtrans-mobile-api.wildberries.ru/api/v1/oferta"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
