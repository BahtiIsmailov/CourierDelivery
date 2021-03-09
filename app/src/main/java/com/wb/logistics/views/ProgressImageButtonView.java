package com.wb.logistics.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.wb.logistics.R;

public class
ProgressImageButtonView extends FrameLayout {

    private final static int DEFAULT_CURRENT_STATE = ProgressImageButtonMode.ENABLED;
    private final static int DEFAULT_COLOR = Color.WHITE;

    private View layout;
    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView textView;

    private String text;
    private int currentState = ProgressImageButtonMode.ENABLED;
    private int enableColor;
    private int progressColor;
    private int disableColor;
    private Drawable icon;

    public ProgressImageButtonView(Context context) {
        super(context);
        init(null);
    }

    public ProgressImageButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProgressImageButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View layoutView = inflater.inflate(R.layout.progress_image_button, this, false);
        layout = layoutView.findViewById(R.id.background_layout);
        imageView = layoutView.findViewById(R.id.icon_image);
        progressBar = layoutView.findViewById(R.id.progress_bar);
        textView = layoutView.findViewById(R.id.text);
        addView(layoutView);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressImageButtonView);
            try {
                text = array.getString(R.styleable.ProgressImageButtonView_app_text);
                currentState = array.getInteger(R.styleable.ProgressImageButtonView_app_state, DEFAULT_CURRENT_STATE);
                enableColor = array.getColor(R.styleable.ProgressImageButtonView_app_text_color_enable, DEFAULT_COLOR);
                progressColor = array.getColor(R.styleable.ProgressImageButtonView_app_text_color_progress, DEFAULT_COLOR);
                disableColor = array.getColor(R.styleable.ProgressImageButtonView_app_text_color_disable, DEFAULT_COLOR);
                icon = array.getDrawable(R.styleable.ProgressImageButtonView_app_icon);
                Drawable background = array.getDrawable(R.styleable.ProgressImageButtonView_app_background);
                layout.setBackground(background);
            } catch (NullPointerException exception) {
                initDefaultState();
            } finally {
                array.recycle();
            }
        }
    }

    private void initDefaultState() {
        currentState = DEFAULT_CURRENT_STATE;
    }

    private void initState() {
        switch (currentState) {
            case ProgressImageButtonMode.ENABLED:
                enableState();
                break;
            case ProgressImageButtonMode.PROGRESS:
                progressState();
                break;
            case ProgressImageButtonMode.DISABLED:
                disabledState();
                break;
        }
    }

    private void enableState() {
        layout.setEnabled(true);
        imageView.setVisibility(VISIBLE);
        imageView.setImageDrawable(icon);
        imageView.setColorFilter(enableColor, PorterDuff.Mode.SRC_ATOP);
        progressBar.setVisibility(GONE);
        textView.setText(text);
        textView.setTextColor(enableColor);
    }

    private void progressState() {
        layout.setEnabled(true);
        imageView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        textView.setText(text);
        textView.setTextColor(progressColor);
    }

    private void disabledState() {
        layout.setEnabled(false);
        imageView.setVisibility(VISIBLE);
        imageView.setImageDrawable(icon);
        imageView.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
        progressBar.setVisibility(GONE);
        textView.setText(text);
        textView.setTextColor(disableColor);
    }

    public void setState(int currentState) {
        this.currentState = currentState;
        initState();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        layout.setOnClickListener(l);
    }

}
