package com.chinaunicom.videorecord;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chinaunicom.videorecord.utils.CameraUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = "MainActivity";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mRecorder;

    private Button start;//开始拍摄按钮
    private Button stop;//结束拍摄按钮
    private Button change;//切换摄像头按钮
    private Camera mCamera;

    private boolean isBack = false;//后置摄像头的标志
    private boolean isRecording = false;//正在录像的标志

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
        mSurfaceHolder.setFixedSize(640,480);
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                isRecording = true;
                if(mCamera == null){
                    initCamera();
                    mCamera.setDisplayOrientation(90);
                }
                configRecord();
                mRecorder.start();
                break;
            case R.id.stop:
                isRecording = false;
                if(mRecorder != null){
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
                break;
            case R.id.change:
                if(isRecording){
                    Toast.makeText(this,"正在录像，无法切换",Toast.LENGTH_SHORT).show();
                }else{
                    isBack = !isBack;
                    initCamera();
                }
                break;
        }
    }

    private void configRecord(){
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
        if(!isBack){
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //获取运行时权限
        RxPermissions.getInstance(MainActivity.this)
                .request(Manifest.permission.CAMERA
                        ,Manifest.permission.RECORD_AUDIO
                        ,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if(aBoolean){
                            initCamera();
                        }else {
                            Log.d(TAG, "call: 444");
                            finish();
                        }
                    }
                });
        Log.d(TAG, "surfaceCreated: 111");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        mCamera.startPreview();
        Log.d(TAG, "surfaceChanged: 222");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraUtil.releaseCamera(mCamera);
        Log.d(TAG, "surfaceDestroyed: 333");
    }

    //初始化摄像头
    private void initCamera(){
        if(mCamera!=null){
            mCamera.stopPreview();
        }
        CameraUtil.releaseCamera(mCamera);
        int cameraIndex;
        if(!isBack){
            cameraIndex = CameraUtil.findCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if(cameraIndex == -1){
                Toast.makeText(this,"打开前置摄像头失败，自动切换",Toast.LENGTH_SHORT).show();
                cameraIndex = CameraUtil.findCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        }
        else cameraIndex = CameraUtil.findCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera = CameraUtil.createCamera(cameraIndex);
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }
}
