package ru.wb.perevozka.ui.couriercarnumber.keyboard

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import io.reactivex.subjects.PublishSubject
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CarNumberKeyboardLayoutBinding
import ru.wb.perevozka.ui.auth.keyboard.KeyboardButtonView
import java.util.*

class CarNumberKeyboardNumericView : RelativeLayout {

    var observableListener = PublishSubject.create<ButtonAction>()

    private lateinit var _binding: CarNumberKeyboardLayoutBinding
    private val binding get() = _binding
    private lateinit var numberButtons: List<KeyboardButtonView>
    private var keyboardMode = CarNumberKeyboardMode.NUMERIC

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

    private fun init(attrs: AttributeSet?) {
        initUI()
        initAttributes(attrs)
        //initDefault()
        initListeners()
    }

    private fun initUI() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _binding = CarNumberKeyboardLayoutBinding.inflate(inflater, this, false)
        ArrayList(
            listOf(
                binding.numeric.button0, binding.numeric.button1, binding.numeric.button2,
                binding.numeric.button3, binding.numeric.button4, binding.numeric.button5,
                binding.numeric.button6, binding.numeric.button7, binding.numeric.button8,
                binding.numeric.button9,
                binding.symbol.buttonA, binding.symbol.buttonB, binding.symbol.buttonE,
                binding.symbol.buttonK, binding.symbol.buttonM, binding.symbol.buttonH,
                binding.symbol.buttonO, binding.symbol.buttonP, binding.symbol.buttonC,
                binding.symbol.buttonT, binding.symbol.buttonY, binding.symbol.buttonX
            )
        ).also { numberButtons = it }
        addView(binding.root)
        initLeftNoneMode()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CarNumberKeyboardView,
            0, 0
        )
        try {
            setKeyboardMode(
                attributes.getInt(
                    R.styleable.CarNumberKeyboardView_carNumberType,
                    CarNumberKeyboardMode.NUMERIC
                )
            )
            setColor(
                attributes.getInt(
                    R.styleable.CarNumberKeyboardView_carNumberKeyboardColor,
                    Color.BLACK
                )
            )
        } finally {
            attributes.recycle()
        }
    }

    private fun initListeners() {
        for (button in numberButtons) {
            button.setOnClickListener { view: View ->
                val keyboardButtonView = view as KeyboardButtonView
                val action = ButtonAction.valueOf(keyboardButtonView.customValue)
                observableListener.onNext(action)
                keyboardButtonView.startAnimation()
            }
        }
        binding.buttonBottomRight.setOnLongClickListener {
            observableListener.onNext(ButtonAction.BUTTON_DELETE_LONG)
            true
        }
        binding.buttonBottomRight.setOnClickListener {
            observableListener.onNext(
                ButtonAction.BUTTON_DELETE
            )
        }
    }

    fun setKeyboardMode(@CarNumberKeyboardMode type: Int) {
        keyboardMode = type
        when (keyboardMode) {
            CarNumberKeyboardMode.NUMERIC -> initNumericInput()
            CarNumberKeyboardMode.SYMBOL -> initSymbolInput()
        }
    }

    private fun setColor(color: Int) {
        for (button in numberButtons) {
            button.setColor(color)
        }
    }

    private fun initNumericInput() {
        binding.numeric.numeric.visibility = VISIBLE
        binding.symbol.symbol.visibility = GONE
    }

    private fun initSymbolInput() {
        binding.numeric.numeric.visibility = GONE
        binding.symbol.symbol.visibility = VISIBLE
    }

    private fun initLeftNoneMode() {
        binding.buttonBottomLeft.visibility = GONE
    }

    fun lock() {
        binding.overlayKeyboard.visibility = VISIBLE
    }

    fun unlock() {
        binding.overlayKeyboard.visibility = GONE
    }

    fun clear() {
        observableListener.onNext(ButtonAction.BUTTON_DELETE_LONG)
    }

    fun inactive() {
        binding.buttonBottomRight.setImageResource(R.drawable.ic_keyboard_backspace_inactive)
    }

    fun active() {
        binding.buttonBottomRight.setImageResource(R.drawable.ic_keyboard_backspace_active)
    }

    enum class ButtonAction(val symbol: String) {
        BUTTON_0("0"), BUTTON_1("1"), BUTTON_2("2"), BUTTON_3("3"),
        BUTTON_4("4"), BUTTON_5("5"), BUTTON_6("6"), BUTTON_7("7"),
        BUTTON_8("8"), BUTTON_9("9"),
        BUTTON_A("A"), BUTTON_B("B"), BUTTON_E("E"), BUTTON_K("K"),
        BUTTON_M("M"), BUTTON_H("H"), BUTTON_O("O"), BUTTON_P("P"),
        BUTTON_C("C"), BUTTON_T("T"), BUTTON_Y("Y"), BUTTON_X("X"),
        BUTTON_DELETE("DEL"), BUTTON_DELETE_LONG("DEL_LONG")
    }

}