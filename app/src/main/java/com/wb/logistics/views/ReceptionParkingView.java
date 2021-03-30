package com.wb.logistics.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.wb.logistics.R;

public class ReceptionParkingView extends FrameLayout {

    private final static String DEFAULT_PARKING_TEXT = "";
    private final static int DEFAULT_STATE = ReceptionParkingMode.EMPTY;

    private View background;
    private TextView parkingTitleTextView;
    private TextView parkingNumberTextView;

    private String parkingNumber = DEFAULT_PARKING_TEXT;
    private int currentState = DEFAULT_STATE;

    public ReceptionParkingView(Context context) {
        super(context);
        init(null);
    }

    public ReceptionParkingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ReceptionParkingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        initView();
        initAttrs(attrs);
        initState();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutView = inflater.inflate(R.layout.reception_parking, this, false);
        background = layoutView.findViewById(R.id.background);
        parkingTitleTextView = layoutView.findViewById(R.id.parking_title);
        parkingNumberTextView = layoutView.findViewById(R.id.parking_number);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ReceptionParkingView);
            try {
                parkingNumber = array.getString(R.styleable.ReceptionParkingView_reception_parking_number);
                currentState = array.getInteger(R.styleable.ReceptionParkingView_reception_parking_state, DEFAULT_STATE);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        parkingNumber = getResources().getString(R.string.reception_default_parking);
        currentState = DEFAULT_STATE;
    }

    private void initState() {
        switch (currentState) {
            case ReceptionAcceptedMode.EMPTY:
                emptyState();
                break;
            case ReceptionAcceptedMode.CONTAINS_COMPLETE:
                containsCompleteState();
                break;
            case ReceptionAcceptedMode.CONTAINS_DENY:
                containsDenyState();
                break;
        }
    }

    private void emptyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_empty));
        parkingTitleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_text));
        parkingNumberTextView.setText(getResources().getString(R.string.reception_default_parking));
        parkingNumberTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.base_text));
    }

    private void containsCompleteState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_complete_fill));
        parkingTitleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.bright_text));
        parkingNumberTextView.setText(parkingNumber);
        parkingNumberTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.bright_text));
    }

    private void containsDenyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_deny_fill));
        parkingTitleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.bright_text));
        parkingNumberTextView.setText(parkingNumber);
        parkingNumberTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.bright_text));
    }

    public void setParkingNumber(String parkingNumber, int currentState) {
        this.parkingNumber = parkingNumber;
        this.currentState = currentState;
        initState();
    }

    public void setParkingNumber(int currentState) {
        this.currentState = currentState;
        initState();
    }

}