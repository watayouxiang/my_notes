# MediaCodec的简单使用

## 运行环境

```
compileSdk 28
minSdk 21
targetSdk 28

plugins {
    id 'com.android.application' version '7.1.2' apply false
    id 'com.android.library' version '7.1.2' apply false
}

distributionUrl=https\://services.gradle.org/distributions/gradle-7.2-bin.zip
```

## 权限申明

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## 布局文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#fff"
    tools:context=".MainActivity">

    <SurfaceView
        android:id="@+id/preview"
        android:layout_width="368dp"
        android:layout_height="384dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</RelativeLayout>
```

## MainActivity.java

```java
package com.maniu.h264maniu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

        }
        return false;
    }

    H264Player h264Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        initSurface();
    }

    private void initSurface() {
        SurfaceView surfaceView = findViewById(R.id.preview);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Surface surface = surfaceHolder.getSurface();
                String filePath = new File(Environment.getExternalStorageDirectory(), "splice1.h2642").getAbsolutePath();
                h264Player = new H264Player(filePath, surface);
                h264Player.play();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
    }
}
```

## H264Player.java

```java
package com.maniu.h264maniu;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class H264Player implements Runnable {
    private final String path;
    MediaCodec mediaCodec;

    public H264Player(String path, Surface surface) {
        this.path = path;
        try {
            // 初始化 "video/avc" 格式的解码器，并配置
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 364, 368);
            mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(mediaformat, surface, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        mediaCodec.start();
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            decodeH264();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decodeH264() throws IOException {
        // ----------------------------------------
        // 将二进制数据从 CPU 送到 DSP 芯片中进行解码
        // ----------------------------------------

        // 获取 h264 字节数据
        byte[] bytes = getBytes(path);

        int startIndex = 0;
        while (true) {
            // 假如 startIndex = 0 并且 startIndex 为帧的起始位置
            // 那么 nextFrameStart 也会为 0，为了避免该情况，所以这里 +2
            int nextFrameStart = findByFrame(bytes, startIndex + 2);

            // 获取 InputBuffer，InputBuffer 是承数据的容器
            // 当该方法返回值是 InputBuffer 在该容器队列的位置，不为 -1 时说明获取返回成功
            // 该方法是耗时方法，所以入参为 等待超时时长
            int inIndex = mediaCodec.dequeueInputBuffer(10000);

            if (inIndex >= 0) {
                // 根据数据容器的位置，获取空闲的数据容器
                ByteBuffer byteBuffer = mediaCodec.getInputBuffer(inIndex);
                int length = nextFrameStart - startIndex;

                // 将一帧数据丢人数据容器
                byteBuffer.put(bytes, startIndex, length);

                // 提交数据到 DSP 芯片
                mediaCodec.queueInputBuffer(
                        // 数据在那个数据容器中
                        inIndex,
                        // 数据的起始位置
                        0,
                        // 数据的长度
                        length, 0, 0
                );

                startIndex = nextFrameStart;
            }

            // ----------------------------------------
            // 将 DSP 解码后的数据送到 GPU 中显示
            // ----------------------------------------
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            // 获取输出数据容器，此时数据容器中的数据已经经过 DSP 解码，为 YUV 数据
            int outIndex = mediaCodec.dequeueOutputBuffer(info, 10000);
            if (outIndex >= 0) {
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 将 DSP 解码后的数据，交给 Surface 显示
                mediaCodec.releaseOutputBuffer(outIndex, true);
            }
        }

    }

    // 获取下一帧的起始位置
    // 每一帧的分隔符为 00 00 00 01 或者 00 00 01
    private int findByFrame(byte[] bytes, int start) {
        int totalSize = bytes.length;
        for (int i = start; i <= totalSize - 4; i++) {
            if (((bytes[i] == 0x00) && (bytes[i + 1] == 0x00) && (bytes[i + 2] == 0x00) && (bytes[i + 3] == 0x01))
                    || ((bytes[i] == 0x00) && (bytes[i + 1] == 0x00) && (bytes[i + 2] == 0x01))) {
                return i;
            }
        }
        return -1;
    }

    // 读取文件数据
    public byte[] getBytes(String path) throws IOException {
        InputStream is = new DataInputStream(new FileInputStream(path));
        int len;
        int size = 1024;
        byte[] buf;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[size];
        while ((len = is.read(buf, 0, size)) != -1) {
            bos.write(buf, 0, len);
        }
        buf = bos.toByteArray();
        return buf;
    }
}
```