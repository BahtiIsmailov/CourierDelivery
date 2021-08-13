package ru.wb.perevozka.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import ru.wb.perevozka.R;


public class ProgressButtonView extends FrameLayout {

    private final static String DEFAULT_TEXT = "Далее";
    private final static int DEFAULT_CURRENT_STATE = ProgressButtonMode.ENABLE;
    private final static int DEFAULT_COLOR = Color.WHITE;

    private View layout;
    private TextView textView;
    private ProgressBar progressBar;

    private int enableColor;
    private int progressColor;
    private int disableColor;

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
        layout = layoutView.findViewById(R.id.background_layout);
        textView = layoutView.findViewById(R.id.text);
        progressBar = layoutView.findViewById(R.id.progress_bar);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressButtonView);
            try {
                text = array.getString(R.styleable.ProgressButtonView_progress_button_name);
                currentState = array.getInteger(R.styleable.ProgressButtonView_progress_button_state, DEFAULT_CURRENT_STATE);
                enableColor = array.getColor(R.styleable.ProgressButtonView_progress_button_text_color_enable, DEFAULT_COLOR);
                progressColor = array.getColor(R.styleable.ProgressButtonView_progress_button_text_color_progress, DEFAULT_COLOR);
                disableColor = array.getColor(R.styleable.ProgressButtonView_progress_button_text_color_disable, DEFAULT_COLOR);
                Drawable background = array.getDrawable(R.styleable.ProgressButtonView_progress_button_background);
                layout.setBackground(background);
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
        layout.setEnabled(false);
        textView.setText(text);
        textView.setTextColor(disableColor);
        progressBar.setVisibility(GONE);
    }

    private void enableState() {
        layout.setEnabled(true);
        textView.setText(text);
        textView.setTextColor(enableColor);
        progressBar.setVisibility(GONE);
    }

    private void progressState() {
        layout.setEnabled(false);
        textView.setText(text);
        textView.setTextColor(progressColor);
        progressBar.setVisibility(VISIBLE);
    }

    public void setText(String text) {
        this.text = text;
        textView.setText(text);
    }

    public void setState(int currentState) {
        this.currentState = currentState;
        initState();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
        layout.setOnClickListener(onClickListener);
    }

}
