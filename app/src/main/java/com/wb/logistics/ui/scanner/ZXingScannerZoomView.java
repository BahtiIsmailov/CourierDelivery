package com.wb.logistics.ui.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import me.dm7.barcodescanner.core.CameraWrapper;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ZXingScannerZoomView extends ZXingScannerView {

    private ZXingScannerComplete cameraCompleteListener = null;

    interface ZXingScannerComplete {
        void onComplete();
    }

    private CameraWrapper cameraWrapper = null;

    public ZXingScannerZoomView(Context context) {
        super(context);
    }

    public ZXingScannerZoomView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setCameraCompleteListener(ZXingScannerComplete cameraCompleteListener) {
        this.cameraCompleteListener = cameraCompleteListener;
    }

    public int getMaxZoom() {
        int maxZoom = 0;
        if (cameraWrapper != null) {
            Camera.Parameters parameters = cameraWrapper.mCamera.getParameters();
            maxZoom = parameters.getMaxZoom();
        }
        return maxZoom;
    }

    public void zoom(int zoom) {
        if (cameraWrapper != null) {
            Camera.Parameters parameters = cameraWrapper.mCamera.getParameters();
            int maxZoom = parameters.getMaxZoom();
            if (parameters.isZoomSupported()) {
                if (zoom >= 0 && zoom < maxZoom) {
                    parameters.setZoom(zoom);
                } else {
                    parameters.setZoom(maxZoom / 2);
                }
                cameraWrapper.mCamera.setParameters(parameters);
            }
        }
    }

    @Override
    public void setupCameraPreview(CameraWrapper cameraWrapper) {
        super.setupCameraPreview(cameraWrapper);
        this.cameraWrapper = cameraWrapper;
        if (cameraCompleteListener != null) {
            cameraCompleteListener.onComplete();
        }
    }

}
