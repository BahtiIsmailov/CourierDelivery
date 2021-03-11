package com.wb.logistics.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.wb.logistics.R;

public class ProgressButtonView extends FrameLayout {

    private final static String DEFAULT_TEXT = "";
    private final static int DEFAULT_CURRENT_STATE = ProgressButtonMode.DISABLE;

    private Button progressButton;
    private ProgressBar progressBar;

    private String text = DEFAULT_TEXT;
    private int currentState = ProgressButtonMode.DISABLE;

    public ProgressButtonView(Context context) {
        super(context);
        init(null);
    }

    public ProgressButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProgressButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.progress_button, this, false);
        progressButton = layoutView.findViewById(R.id.progress_button);
        progressBar = layoutView.findViewById(R.id.progress_bar);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressButtonView);
            try {
                text = array.getString(R.styleable.ProgressButtonView_progress_button_name);
                currentState = array.getInteger(R.styleable.ProgressButtonView_progress_button_state, DEFAULT_CURRENT_STATE);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        text = DEFAULT_TEXT;
        currentState = DEFAULT_CURRENT_STATE;
    }

    private void initState() {
        progressButton.setText(text);
        switch (currentState) {
            case ProgressButtonMode.DISABLE:
                disableState();
                break;
            case ProgressButtonMode.ENABLE:
                enableState();
                break;
            case ProgressButtonMode.PROGRESS:
                progressState();
                break;
        }
    }

    private void disableState() {
        progressButton.setEnabled(false);
        progressButton.setVisibility(VISIBLE);
        progressButton.setText(text);
        progressBar.setVisibility(GONE);
    }

    private void enableState() {
        progressButton.setEnabled(true);
        progressButton.setVisibility(VISIBLE);
        progressButton.setText(text);
        progressBar.setVisibility(GONE);
    }

    private void progressState() {
        progressButton.setEnabled(false);
        progressButton.setVisibility(VISIBLE);
        progressButton.setText(DEFAULT_TEXT);
        progressBar.setVisibility(VISIBLE);
    }

    public void setText(String text) {
        this.text = text;
        progressButton.setText(text);
    }

    public void setState(int currentState) {
        this.currentState = currentState;
        initState();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        progressButton.setOnClickListener(onClickListener);
    }

}
