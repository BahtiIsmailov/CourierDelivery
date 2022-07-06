package ru.wb.go.ui.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.wb.go.R
import ru.wb.go.app.TELEGRAM_SUPPORT_ID
import ru.wb.go.app.TELEGRAM_SUPPORT_LINK
import ru.wb.go.databinding.SupportFragmentBinding

class SupportFragment : BottomSheetDialogFragment() {

    lateinit var binding: SupportFragmentBinding
    private val behavior: BottomSheetBehavior<FrameLayout>
    get() = BottomSheetBehavior.from(dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout)

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            SupportFragmentBinding.bind(inflater.inflate(R.layout.support_fragment, container))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            supportNext.setOnClickListener {
                hide()
                startActivity(telegramIntent(requireContext()))
            }
            supportCancel.setOnClickListener { hide() }
        }



    }

    private fun hide() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun telegramIntent(context: Context): Intent {
        return try {
            try {
                getTelegramPackage("org.telegram.messenger")
            } catch (e: Exception) {
                getTelegramPackage("org.thunderdog.challegram")
            }
            //при наличие имени группы заменить на telegramNameGroupIntent()
            telegramLinkIntent()
        } catch (e: Exception) {
            telegramLinkIntent()
        }
    }

    private fun getTelegramPackage(tag: String) {
        requireContext().packageManager.getPackageInfo(tag, 0)
    }

    private fun telegramLinkIntent() = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://t.me/$TELEGRAM_SUPPORT_LINK")
    )

    private fun telegramNameGroupIntent() = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("tg://resolve?domain=$TELEGRAM_SUPPORT_ID")
    )

}