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

import com.wb.logistics.R;

public class ReceptionAcceptedView extends FrameLayout {

    private final static String DEFAULT_COUNT_BOX_TEXT = "";
    private final static int DEFAULT_CURRENT_STATE = ReceptionAcceptedMode.EMPTY;

    private View background;
    private TextView countBoxTextView;
    private TextView listBoxTextView;

    private String countBox = DEFAULT_COUNT_BOX_TEXT;
    private int currentState = DEFAULT_CURRENT_STATE;

    public ReceptionAcceptedView(Context context) {
        super(context);
        init(null);
    }

    public ReceptionAcceptedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ReceptionAcceptedView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.reception_accepted, this, false);
        background = layoutView.findViewById(R.id.background);
        countBoxTextView = layoutView.findViewById(R.id.count_box);
        listBoxTextView = layoutView.findViewById(R.id.list_box);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ReceptionAcceptedView);
            try {
                countBox = array.getString(R.styleable.ReceptionAcceptedView_reception_accepted_box);
                currentState = array.getInteger(R.styleable.ReceptionAcceptedView_reception_accepted_state, DEFAULT_CURRENT_STATE);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        countBox = getResources().getString(R.string.reception_default_count_box);
        currentState = DEFAULT_CURRENT_STATE;
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
        countBoxTextView.setText(getResources().getString(R.string.reception_default_count_box));
        listBoxTextView.setVisibility(INVISIBLE);
    }

    private void containsCompleteState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_complete_trans));
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(VISIBLE);
    }

    private void containsDenyState() {
        background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.reception_rounded_corner_deny_trans));
        countBoxTextView.setText(countBox);
        listBoxTextView.setVisibility(VISIBLE);
    }

    public void setCountBox(String countBox, int currentState) {
        this.countBox = countBox;
        this.currentState = currentState;
        initState();
    }

    public void setCountBox(int currentState) {
        this.currentState = currentState;
        initState();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        listBoxTextView.setOnClickListener(onClickListener);
    }

}
