package ru.wb.go.ui.auth.keyboard

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.MutableLiveData
import ru.wb.go.R
import ru.wb.go.databinding.KeyboardNumericLayoutBinding
import ru.wb.go.utils.VIBRATE_CLICK
import ru.wb.go.utils.vibrateOnAction

class KeyboardNumericView : RelativeLayout {

    private var _binding: KeyboardNumericLayoutBinding? = null
    private val binding get() = _binding!!

    private var numberButtons: List<KeyboardButtonView>? = null
    private var leftButtonMode = LeftButtonMode.NONE
    private var rightButtonMode = RightButtonMode.DELETE



    var observableListener = MutableLiveData<ButtonAction>()
    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val hHalf = w / 2
        super.onSizeChanged(w, hHalf, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> measuredWidth
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> heightSize
            else -> measuredWidth
        }
        setMeasuredDimension(width, height)
    }

    private fun init(attrs: AttributeSet?) {
        initUI()
        initAttributes(attrs)
        initDefault()
        initListeners()

    }

    private fun initListeners() {
        for (button in numberButtons!!) {
            button.setOnClickListener { v: View ->
                val keyboardButtonView = v as KeyboardButtonView
                val action =
                    ButtonAction.valueOf(keyboardButtonView.customValue)
                vibrateOnAction(context, VIBRATE_CLICK)
                observableListener.value = action
                keyboardButtonView.startAnimation()
            }
        }
        binding.buttonBottomRight.setOnLongClickListener {
            observableListener.value = ButtonAction.BUTTON_DELETE_LONG
            true
        }
        binding.buttonBottomRight.setOnClickListener {
            vibrateOnAction(context, VIBRATE_CLICK)
            observableListener.value =
                ButtonAction.BUTTON_DELETE
        }

    }

    private fun initUI() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _binding = KeyboardNumericLayoutBinding.inflate(inflater, this, false)
        ArrayList(
            listOf(
                binding.button0, binding.button1, binding.button2,
                binding.button3, binding.button4, binding.button5,
                binding.button6, binding.button7, binding.button8,
                binding.button9
            )
        ).also { numberButtons = it }
        addView(binding.root)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.KeyboardNumericView,
            0, 0
        )
        try {
            setColor(attributes.getInt(R.styleable.KeyboardNumericView_keyboardColor, Color.BLACK))
        } finally {
            attributes.recycle()
        }
    }

    private fun initDefault() {
        setLeftButtonMode(leftButtonMode)
        setRightButtonMode(rightButtonMode)
    }

    private fun setColor(color: Int) {
        for (button in numberButtons!!) {
            button.setColor(color)
        }
    }

    private fun setLeftButtonMode(newMode: LeftButtonMode) {
        leftButtonMode = newMode
        when (newMode) {
            LeftButtonMode.NONE -> invalidateLeftNoneMode()
        }
    }

    private fun setRightButtonMode(newMode: RightButtonMode) {
        rightButtonMode = newMode
        when (newMode) {
            RightButtonMode.NONE -> invalidateRightNoneMode()
            RightButtonMode.FINGERPRINT -> invalidateRightFingerprintMode()
            RightButtonMode.DELETE -> invalidateRightDeleteMode()
        }
    }

    private fun invalidateLeftNoneMode() {
        binding.buttonBottomLeft.visibility = GONE
    }

    private fun invalidateRightNoneMode() {
        binding.buttonBottomRight.visibility = GONE
    }

    private fun invalidateRightFingerprintMode() {
        binding.buttonBottomRight.setImageResource(R.drawable.ic_keyboard_backspace_active)
        binding.buttonBottomRight.visibility = VISIBLE
    }

    private fun invalidateRightDeleteMode() {
        binding.buttonBottomRight.setImageResource(R.drawable.ic_keyboard_backspace_inactive)
        binding.buttonBottomRight.visibility = VISIBLE
    }

    fun lock() {
        binding.overlayKeyboard.visibility = VISIBLE
    }

    fun unlock() {
        binding.overlayKeyboard.visibility = GONE
    }

    fun clear() {
        observableListener.value = ButtonAction.BUTTON_DELETE_LONG
    }

    fun inactive() {
        binding.buttonBottomRight.setImageResource(R.drawable.ic_keyboard_backspace_inactive)
    }

    fun active() {
        binding.buttonBottomRight.setImageResource(R.drawable.ic_keyboard_backspace_active)
    }

    enum class LeftButtonMode {
        NONE,
    }

    enum class RightButtonMode {
        NONE, FINGERPRINT, DELETE
    }

    enum class ButtonAction {
        BUTTON_0, BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4, BUTTON_5, BUTTON_6, BUTTON_7, BUTTON_8, BUTTON_9, BUTTON_DELETE, BUTTON_DELETE_LONG
    }
}