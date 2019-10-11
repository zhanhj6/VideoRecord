package com.chinaunicom.videorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mRecorder;

    private Button start;
    private Button stop;
    private Button change;
    private Camera mCamera;

    private boolean isBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        start.setOnClickListener(this);

        stop = findViewById(R.id.stop);
        stop.setOnClickListener(this);

        change = findViewById(R.id.change);
        change.setOnClickListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.videoView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                RxPermissions.getInstance(MainActivity.this)
                    .request(Manifest.permission.CAMERA
                            ,Manifest.permission.RECORD_AUDIO
                            ,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            if(aBoolean){
                                configRecord(isBack);
                                mRecorder.start();
                            }
                            else{
                                finish();
                            }
                        }
                    });
                break;
            case R.id.stop:
                if(mRecorder != null){
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
                break;
            case R.id.change:
                isBack = ! isBack;
                configRecord(isBack);
                break;
        }
    }

    private void configRecord(boolean flag){
        releaseCamera();
        int cameraIndex;
        if(!flag){
            cameraIndex = findCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if(cameraIndex == -1){
                Toast.makeText(this,"打开前置摄像头失败，自动切换",Toast.LENGTH_SHORT).show();
                cameraIndex = findCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                isBack = !flag;
            }
        }else {
            cameraIndex = findCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        mCamera = Camera.open(cameraIndex);
        mCamera.setDisplayOrientation(90);
        if(mRecorder == null)
            mRecorder = new MediaRecorder();
        mRecorder.reset();
        mCamera.unlock();
        mRecorder.setCamera(mCamera);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setVideoSize(640,480);
        mRecorder.setVideoEncodingBitRate(900000);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        String fileName = simpleDateFormat.format(new Date());
        File f = new File(getExternalCacheDir(),fileName + ".mp4");
        String mOutPath = f.getAbsolutePath();
        if(f.exists())
            f.delete();
        mRecorder.setOutputFile(mOutPath);
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        if(cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT){
            mRecorder.setOrientationHint(270);
        }else {
            mRecorder.setOrientationHint(90);
        }
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int findCamera(int flag){
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
    private void releaseCamera(){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
