package ru.wb.perevozka.ui.auth.keyboard

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import org.joda.time.DateTime
import ru.wb.perevozka.R
import ru.wb.perevozka.app.AppConsts
import ru.wb.perevozka.databinding.KeyboardInputViewBinding
import ru.wb.perevozka.ui.auth.keyboard.KeyboardNumericView.LeftButtonMode
import ru.wb.perevozka.ui.auth.keyboard.KeyboardNumericView.RightButtonMode
import ru.wb.perevozka.utils.time.TimeFormatType
import ru.wb.perevozka.utils.time.TimeFormatter
import ru.wb.perevozka.utils.time.TimeFormatterImpl

class KeyboardInputView : RelativeLayout, OnKeyboardCallbackListener {
    private var binding: KeyboardInputViewBinding? = null
    private var timeFormatter: TimeFormatter? = null
    private var resourceProvider: KeyboardInputResourceProvider? = null
    private var editCode: EditText? = null
    private var callbackListener: OnKeyboardInputViewListener? = null
    private var keyboardMode = KeyboardMode.SIGN_UP
    private var isFingerprintEnabled = false
    private var timer: CountDownTimer? = null

    constructor(context: Context) : super(context) {
        init(null, context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, context)
    }

    private fun init(attrs: AttributeSet?, context: Context) {
        inject(context)
        initUI()
        initAttributes(attrs)
        initListeners()
        initState()
    }

    private fun inject(context: Context) {
        timeFormatter = TimeFormatterImpl()
        resourceProvider = KeyboardInputResourceProviderImpl(context)
    }

    private fun initUI() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = KeyboardInputViewBinding.inflate(inflater, this, false)
        addView(binding!!.root)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.KeyboardInputView,
            0, 0
        )
        keyboardMode = try {
            attributes.getInt(R.styleable.KeyboardInputView_type, KeyboardMode.SMS)
        } finally {
            attributes.recycle()
        }
    }

    private fun initListeners() {
        binding!!.viewKeyboard.setCallbackListener(this)
        binding!!.buttonRepeatSendSms.setOnClickListener { v: View? ->
            if (callbackListener != null) {
                callbackListener!!.onRepeatSmsClicked()
            }
        }
    }

    fun initState() {
        when (keyboardMode) {
            KeyboardMode.SMS -> initSmsInput()
            KeyboardMode.PIN_INPUT -> initInputPinInput()
            KeyboardMode.PIN_CREATE -> initCreatePinInput()
            KeyboardMode.SIGN_UP -> initSignUp()
        }
    }

    private fun initInputPinInput() {
        showPinCode()
        binding!!.viewKeyboard.setLeftButtonMode(LeftButtonMode.FORGET)
        setRightButtonMode()
    }

    private fun setRightButtonMode() {
        if (isFingerprintEnabled && editCode!!.text.toString().isEmpty()) {
            binding!!.viewKeyboard.setRightButtonMode(RightButtonMode.FINGERPRINT)
        } else {
            binding!!.viewKeyboard.setRightButtonMode(RightButtonMode.DELETE)
        }
    }

    private fun initCreatePinInput() {
        showPinCode()
        binding!!.viewKeyboard.setLeftButtonMode(LeftButtonMode.NONE)
        binding!!.viewKeyboard.setRightButtonMode(RightButtonMode.DELETE)
    }

    private fun initSignUp() {
        showSignUp()
    }

    private fun initSmsInput() {
        showSmsCode()
        binding!!.viewKeyboard.setRightButtonMode(RightButtonMode.DELETE)
        binding!!.viewKeyboard.setLeftButtonMode(LeftButtonMode.NONE)
    }

    private fun showSmsCode() {
        ////editCode = binding.viewSmsCode;
        ////editSmsCode.setVisibility(VISIBLE);
        binding!!.viewPinCode.visibility = GONE
    }

    private fun showPinCode() {
        editCode = binding!!.viewPinCode
        ////editSmsCode.setVisibility(GONE);
        binding!!.viewPinCode.visibility = VISIBLE
        binding!!.viewKeyboard.visibility = VISIBLE
    }

    private fun showSignUp() {
        ////editSmsCode.setVisibility(GONE);
        binding!!.viewPinCode.visibility = GONE
        binding!!.viewKeyboard.visibility = GONE
    }

    override fun onNumberClicked(`val`: Int) {
        var code = editCode!!.text.toString()
        code += `val`.toString()
        editCode!!.setText(code)
        setRightButtonMode()
        if (code.length == CODE_SIZE) {
            if (callbackListener != null) {
                callbackListener!!.onCodeEntered(code)
            }
        }
    }

    override fun onLeftButtonClicked(mode: LeftButtonMode) {
        when (mode) {
            LeftButtonMode.FORGET -> if (callbackListener != null) {
                callbackListener!!.onForgotPasswordPressed()
            }
            LeftButtonMode.NONE -> {
            }
        }
    }

    override fun onRightButtonClicked(mode: RightButtonMode) {
        when (mode) {
            RightButtonMode.DELETE -> deleteLastSymbol()
            RightButtonMode.FINGERPRINT -> if (callbackListener != null) {
                callbackListener!!.onFingerprintPressed()
            }
            RightButtonMode.NONE -> {
            }
        }
    }

    fun clearCode() {
        if (editCode!!.text.length > 0) {
            editCode!!.text.clear()
        }
        setRightButtonMode()
    }

    fun setDescription(description: String) {
        binding!!.textDescription.text = description
    }

    fun enableRepeatSmsButton() {
        binding!!.buttonRepeatSendSms.isEnabled = true
    }

    fun disableRepeatSmsButton() {
        binding!!.buttonRepeatSendSms.isEnabled = false
    }

    fun enableKeyboard() {
        binding!!.viewKeyboard.isClickable = true
    }

    fun disableKeyboard() {
        binding!!.viewKeyboard.isClickable = false
    }

    fun setKeyboardMode(@KeyboardMode type: Int) {
        keyboardMode = type
        initState()
    }

    fun deleteLastSymbol() {
        val code = editCode!!.text.toString()
        if (editCode != null && code.length > 0) {
            editCode!!.setText(code.substring(0, code.length - 1))
        }
        setRightButtonMode()
    }

    fun startProgressTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.start()
        } else {
            timer = createTimer(AppConsts.REPEAT_SMS_DURATION, AppConsts.REPEAT_SMS_TICK)
            timer!!.start()
        }
    }

    private fun createTimer(millisInFuture: Long, countDownInterval: Long): CountDownTimer {
        return object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                updateProgress(millisUntilFinished)
            }

            override fun onFinish() {
                showRepeatSmsButton()
            }
        }
    }

    private fun updateProgress(millisUntilFinished: Long) {
        val dateTime = DateTime(millisUntilFinished)
        val timeLeft = timeFormatter!!.format(dateTime, TimeFormatType.MIN_AND_SEC)
        binding!!.buttonRepeatSendSms.text = resourceProvider!!.getTimeLeftUntilRepeatText(timeLeft)
    }

    fun showRepeatSmsButton() {
        binding!!.buttonRepeatSendSms.text = resourceProvider!!.repeatSmsButtonTitle
        binding!!.buttonRepeatSendSms.isEnabled = true
    }

    fun hideRepeatSmsButton() {
        binding!!.buttonRepeatSendSms.visibility = GONE
    }

    fun setCallbackListener(callbackListener: OnKeyboardInputViewListener) {
        this.callbackListener = callbackListener
    }

    fun setFingerAvailable(fingerAvailable: Boolean) {
        isFingerprintEnabled = fingerAvailable
        initState()
    }

    companion object {
        private const val CODE_SIZE = 4
    }

}