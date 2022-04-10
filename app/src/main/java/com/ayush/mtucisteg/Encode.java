package com.ayush.mtucisteg;

import android.widget.*;
import java.util.*;
import android.Manifest;
import java.io.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v7.app.*;

import com.ayush.mtucisteglib.Text.AsyncTaskCallback.TextEncodingCallback;
import com.ayush.mtucisteglib.Text.ImageSteganography;
import com.ayush.mtucisteglib.Text.TextEncoding;




public class Encode extends AppCompatActivity implements TextEncodingCallback {

    private static final int SELECT_PICTURE = 100;

    private TextView whether_encoded;
    private ImageView imageView;
    private EditText message;
    private EditText secret_key;


    private TextEncoding textEncoding;
    private ImageSteganography imageSteganography;
    private ProgressDialog save;
    private Uri filepath;


    private Bitmap original_image;
    private Bitmap encoded_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        //initialized the UI components

        whether_encoded = findViewById(R.id.whether_encoded);

        imageView = findViewById(R.id.imageview);

        message = findViewById(R.id.message);
        secret_key = findViewById(R.id.secret_key);

        Button choose_image_button = findViewById(R.id.choose_image_button);
        Button encode_button = findViewById(R.id.encode_button);
        Button save_image_button = findViewById(R.id.save_image_button);

        checkAndRequestPermissions();

        choose_image_button.setOnClickListener(view -> ImageChooser());

        encode_button.setOnClickListener(view -> {
            whether_encoded.setText("");
            if (filepath != null) {
                if (message.getText() != null) {
                    imageSteganography = new ImageSteganography(message.getText().toString(),
                            secret_key.getText().toString(),
                            original_image);
                    textEncoding = new TextEncoding(Encode.this, Encode.this);
                    textEncoding.execute(imageSteganography);
                }
            }
        });

        save_image_button.setOnClickListener(view -> {
            final Bitmap imgToSave = encoded_image;
            Thread PerformEncoding = new Thread(() -> saveToInternalStorage(imgToSave));
            save = new ProgressDialog(Encode.this);
            save.setMessage("Сохраняю, подождите...");
            save.setTitle("Сохранение изображения");
            save.setIndeterminate(false);
            save.setCancelable(false);
            save.show();
            PerformEncoding.start();
        });

    }

    private void ImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выбери изображение"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Image set to imageView
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filepath = data.getData();
            try {
                original_image = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);

                imageView.setImageBitmap(original_image);
            } catch (IOException e) {
                //обработка ошибок
            }
        }

    }

    @Override
    public void onStartTextEncoding() {
        // обработка текст на начале
    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {

        //By the end of textEncoding

        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image();
            whether_encoded.setText("Encoded");
            imageView.setImageBitmap(encoded_image);
        }
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        OutputStream fOut;

        File filepath = Environment.getExternalStorageDirectory();

        File dir = new File(filepath.getAbsolutePath()
                + "/StegMTUCI/");
        dir.mkdirs();
        File file = new File(dir, "HIDDEN.PNG" );

        try {
            fOut = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.close();
            whether_encoded.post(() -> save.dismiss());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndRequestPermissions() {
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (ReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }


}
