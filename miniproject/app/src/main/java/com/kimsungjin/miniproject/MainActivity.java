package com.kimsungjin.miniproject;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]
                    {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 2);
        }

        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdPath += "/Android/media/com.kimsungjin.miniproject/data/Black";
        File file = new File(sdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdPath += "/Android/media/com.kimsungjin.miniproject/data/White";
        file = new File(sdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdPath += "/Android/media/com.kimsungjin.miniproject/data/None";
        file = new File(sdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void onStartSecondActivity(View view){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    public void onStartFourthActivity(View view){
        Intent intent = new Intent(this, MainActivity4.class);
        startActivity(intent);
    }

    public void onStartFifthActivity(View view){
        Intent intent = new Intent(this, MainActivity5.class);
        startActivity(intent);
    }

    public void onStartSixthActivity(View view){
        Intent intent = new Intent(this, MainActivity6.class);
        startActivity(intent);
    }
}