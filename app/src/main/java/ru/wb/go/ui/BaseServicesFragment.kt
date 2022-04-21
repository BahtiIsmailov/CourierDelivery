package ru.wb.go.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import ru.wb.go.R
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.app.SupportListener

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseServiceFragment<VM : ServicesViewModel, VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    protected abstract val viewModel: VM
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initListener()
    }

    private fun initListener() {
        binding.root.findViewById<ImageButton>(R.id.support_app).setOnClickListener {
            (activity as SupportListener).showSupportDialog()
        }
    }

    private fun initObservers() {
        viewModel.networkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }

            binding.root.findViewById<ImageView>(R.id.no_internet_image).setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.root.findViewById<TextView>(R.id.version_app).text = it
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}