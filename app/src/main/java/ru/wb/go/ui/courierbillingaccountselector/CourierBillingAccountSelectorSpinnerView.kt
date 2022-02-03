package ru.wb.go.ui.courierbillingaccountselector

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

class CourierBillingAccountSelectorSpinnerView : AppCompatSpinner {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle)

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

}