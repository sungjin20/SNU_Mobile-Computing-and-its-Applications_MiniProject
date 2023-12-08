package com.kimsungjin.miniproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class MainActivity4 extends AppCompatActivity {
    PreviewView previewView;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    int mLensFacing = CameraSelector.LENS_FACING_BACK;
    Bitmap cameraFrameBuffer;
    int InputSize = 32;
    String model_name = "recognition_model.tflite";
    String quantized_model_name = "quantized_recognition_model_int8.tflite";
    MappedByteBuffer tfliteModel;
    Interpreter segmentationDNN;
    Interpreter.Options options = new Interpreter.Options();
    float[][][][] input_data = new float[1][InputSize][InputSize][3];
    Map<Integer, Object> output_data = new HashMap<>();
    Vector<float[][]> separatedOutputs = new Vector<>();
    String chose_label_name = "";
    int chose_label_idx = -1;
    ImageView imageView;
    int[][] imageView_id = new int[9][9];
    int[][] imagetype = new int[9][9];
    EditText editText;
    public static Context context_main;


    static{
        System.loadLibrary("opencv_java4");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        context_main = this;
        RadioGroup rg = findViewById(R.id.radiogroup);
        previewView = findViewById(R.id.previewView);
        editText = findViewById(R.id.treesearchnum);
        imageView_id = id_array();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton chose_button = findViewById(i);
                chose_label_name = chose_button.getText().toString();
                chose_label_idx = i;
            }
        });

        startCamera();

        try{
            //tfliteModel = loadModelFile(this, model_name);
            tfliteModel = loadModelFile(this, quantized_model_name);
        }catch(Exception e){
        }
        segmentationDNN = new Interpreter(tfliteModel, options);

        float[][] output = new float[1][3];
        output_data.put(0, output);
        separatedOutputs.add(output);


    }

    public int[][] id_array(){
        int[][] id = new int[9][9];
        id[1][1] = R.id.b11;
        id[1][2] = R.id.b12;
        id[1][3] = R.id.b13;
        id[1][4] = R.id.b14;
        id[1][5] = R.id.b15;
        id[1][6] = R.id.b16;
        id[1][7] = R.id.b17;
        id[1][8] = R.id.b18;

        id[2][1] = R.id.b21;
        id[2][2] = R.id.b22;
        id[2][3] = R.id.b23;
        id[2][4] = R.id.b24;
        id[2][5] = R.id.b25;
        id[2][6] = R.id.b26;
        id[2][7] = R.id.b27;
        id[2][8] = R.id.b28;

        id[3][1] = R.id.b31;
        id[3][2] = R.id.b32;
        id[3][3] = R.id.b33;
        id[3][4] = R.id.b34;
        id[3][5] = R.id.b35;
        id[3][6] = R.id.b36;
        id[3][7] = R.id.b37;
        id[3][8] = R.id.b38;

        id[4][1] = R.id.b41;
        id[4][2] = R.id.b42;
        id[4][3] = R.id.b43;
        id[4][4] = R.id.b44;
        id[4][5] = R.id.b45;
        id[4][6] = R.id.b46;
        id[4][7] = R.id.b47;
        id[4][8] = R.id.b48;

        id[5][1] = R.id.b51;
        id[5][2] = R.id.b52;
        id[5][3] = R.id.b53;
        id[5][4] = R.id.b54;
        id[5][5] = R.id.b55;
        id[5][6] = R.id.b56;
        id[5][7] = R.id.b57;
        id[5][8] = R.id.b58;

        id[6][1] = R.id.b61;
        id[6][2] = R.id.b62;
        id[6][3] = R.id.b63;
        id[6][4] = R.id.b64;
        id[6][5] = R.id.b65;
        id[6][6] = R.id.b66;
        id[6][7] = R.id.b67;
        id[6][8] = R.id.b68;

        id[7][1] = R.id.b71;
        id[7][2] = R.id.b72;
        id[7][3] = R.id.b73;
        id[7][4] = R.id.b74;
        id[7][5] = R.id.b75;
        id[7][6] = R.id.b76;
        id[7][7] = R.id.b77;
        id[7][8] = R.id.b78;

        id[8][1] = R.id.b81;
        id[8][2] = R.id.b82;
        id[8][3] = R.id.b83;
        id[8][4] = R.id.b84;
        id[8][5] = R.id.b85;
        id[8][6] = R.id.b86;
        id[8][7] = R.id.b87;
        id[8][8] = R.id.b88;

        return id;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String model_name) throws IOException{
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(model_name);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
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

    public void ClickedCaptureBtn(View view){
        Mat mat = new Mat();
        Utils.bitmapToMat(cameraFrameBuffer, mat);
        long recognition_time_64 = 0;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                Bitmap data_xy = getdata(mat, i, j);
                for (int ii = 0; ii < 32; ii++) {
                    for (int jj = 0; jj < 32; jj++) {
                        input_data[0][ii][jj][0] = data_xy.getColor(ii, jj).red();
                        input_data[0][ii][jj][1] = data_xy.getColor(ii, jj).green();
                        input_data[0][ii][jj][2] = data_xy.getColor(ii, jj).blue();
                    }
                }

                Object[] inputArray = {input_data};
                long beforeTime = System.currentTimeMillis();
                segmentationDNN.runForMultipleInputsOutputs(inputArray, output_data);
                long afterTime = System.currentTimeMillis();
                recognition_time_64 += (afterTime - beforeTime);
                float[][] output_1 = (float[][]) output_data.get(0);
                float max_val = -999f;
                int max_num = -1;
                for(int k=0; k<3; k++){
                    if(output_1[0][k] > max_val){
                        max_val = output_1[0][k];
                        max_num = k;
                    }
                }
                imageView = findViewById(imageView_id[i][j]);
                if(max_num == 0){
                    imageView.setImageResource(R.drawable.black);
                }else if(max_num == 1){
                    imageView.setImageResource(R.drawable.white);
                }else{
                    imageView.setImageResource(R.drawable.none);
                }
                imagetype[i][j] = max_num;
            }
        }
        Toast.makeText(getApplicationContext(), "Captured successfully in " + Long.toString(recognition_time_64) + "ms", Toast.LENGTH_SHORT).show();
    }

    public void onStartThirdActivity(View view){
        if(chose_label_name == ""){
            Toast.makeText(getApplicationContext(), "Choose your turn", Toast.LENGTH_SHORT).show();
            return;
        }
        int val = 0;
        try{
            val = Integer.parseInt(editText.getText().toString());
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Tree Search Num should be Integer!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(val <= 0){
            Toast.makeText(getApplicationContext(), "Tree Search Num should be a positive num!", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(this, MainActivity3.class);
            startActivity(intent);
        }
    }
}