package com.kimsungjin.miniproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Matrix;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity2 extends AppCompatActivity {
    PreviewView previewView;
    ImageView imageView;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    int mLensFacing = CameraSelector.LENS_FACING_BACK;
    Bitmap cameraFrameBuffer;
    int checked_btn_id = -1;
    String chose_label_name = "";
    File file;
    String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/media/com.kimsungjin.miniproject/data";


    static{
        System.loadLibrary("opencv_java4");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        RadioGroup rg = findViewById(R.id.radiogroup);
        imageView = findViewById(R.id.capturedimage);
        previewView = findViewById(R.id.previewView);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checked_btn_id = i;
            }
        });

        startCamera();
    }

    private void startCamera(){
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try{
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindUseCases(cameraProvider);
                }catch(ExecutionException | InterruptedException e){

                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindUseCases(@NonNull ProcessCameraProvider cameraProvider){
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(mLensFacing).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();
        imageCapture = new ImageCapture.Builder().setTargetRotation(previewView.getDisplay().getRotation()).build();
        imageAnalysis = new ImageAnalysis.Builder().setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                if(cameraFrameBuffer == null){
                    cameraFrameBuffer = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                }
                ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
                ByteBuffer buffer = planeProxy.getBuffer();
                cameraFrameBuffer.copyPixelsFromBuffer(buffer);
                image.close();
            }
        });
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture, preview, imageAnalysis);
    }

    public Bitmap getdata(Mat mt, int a, int b){
        Mat sub = mt.submat((8-b)*60, (9-b)*60, 80+60*(a-1), 80+60*a);
        Bitmap subbitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        org.opencv.core.Size sz = new Size(32, 32);
        Mat resizeimage = new Mat();
        Imgproc.resize(sub, resizeimage, sz);
        Utils.matToBitmap(resizeimage, subbitmap);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap rot_bitmap = Bitmap.createBitmap(subbitmap, 0, 0, subbitmap.getWidth(), subbitmap.getHeight(), matrix, true);
        return rot_bitmap;
    }

    public void ChooseLabel(View view){
        if (checked_btn_id != -1) {
            RadioButton chose_button = findViewById(checked_btn_id);
            chose_label_name = chose_button.getText().toString();
            Toast.makeText(getApplicationContext(), "You chose: " + chose_label_name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "You should choose one label", Toast.LENGTH_SHORT).show();
        }
    }

    public void ClickedCaptureBtn(View view){
        if(chose_label_name == "") {
            Toast.makeText(getApplicationContext(), "Choose a label to capture", Toast.LENGTH_SHORT).show();
        }else {
            Mat mat = new Mat();
            Utils.bitmapToMat(cameraFrameBuffer, mat);
            for (int i = 1; i < 9; i++) {
                for (int j = 1; j < 9; j++) {
                    Bitmap data_xy = getdata(mat, i, j);
                    long t = System.currentTimeMillis();
                    Date date_save = new Date(t);
                    SimpleDateFormat dateFormat_save = new SimpleDateFormat("MMdd_HHmmss");
                    String getTime_save = dateFormat_save.format(date_save);
                    try {
                        file = new File(savePath + "/" + chose_label_name + "/" + getTime_save + "_" + Integer.toString(i) + Integer.toString(j) + ".csv");
                        file.createNewFile();
                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write("r,g,b");
                        bw.write(System.lineSeparator());
                        for (int ii = 0; ii < 32; ii++) {
                            for (int jj = 0; jj < 32; jj++) {
                                String content = Float.toString(data_xy.getColor(ii, jj).red()) + "," + Float.toString(data_xy.getColor(ii, jj).green()) + "," + Float.toString(data_xy.getColor(ii, jj).blue());
                                bw.write(content);
                                bw.write(System.lineSeparator());
                            }
                        }
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            imageView.setImageBitmap(getdata(mat, 1, 1));
            Toast.makeText(getApplicationContext(), "Captured successfully", Toast.LENGTH_SHORT).show();
        }
    }
}










