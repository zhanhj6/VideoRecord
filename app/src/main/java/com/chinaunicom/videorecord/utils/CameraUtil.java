package com.chinaunicom.videorecord.utils;

import android.hardware.Camera;

import java.util.List;

public class CameraUtil {

    /**
     * 实例化相机
     * @return
     */
    public static Camera createCamera(int cameraIndex){
        return Camera.open(cameraIndex);
    }

    /**
     * 查找相机
     * @param flag
     * @return
     */
    public static int findCamera(int flag){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for(int index = 0;index<cameraCount;index++){
            Camera.getCameraInfo(index,cameraInfo);
            if(cameraInfo.facing == flag)
                return index;
        }
        return -1;
    }

    /**
     * 释放相机资源
     * @param mCamera
     */
    public static void releaseCamera(Camera mCamera){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 根据给定的尺寸选择合适的相机大小
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
