package com.ayush.mtucisteg;

import android.widget.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import com.ayush.mtucisteglib.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.mtucisteglib.Text.ImageSteganography;
import com.ayush.mtucisteglib.Text.TextDecoding;

public class Decode extends AppCompatActivity implements TextDecodingCallback {

    private static final int INTENT_FOR_CHOOSING_PICTURE = 100;
    // Создаём переменные для элементов интерфейса
    private TextView textView;
    private ImageView imageView;
    private EditText message;
    private EditText secret_key;
    private Uri filepath;
    private Bitmap original_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        Button choose_image_button = findViewById(R.id.choose_image_button);
        textView = findViewById(R.id.whether_decoded);
        Button decode_button = findViewById(R.id.decode_button);
        imageView = findViewById(R.id.imageview);

        message = findViewById(R.id.message);
        secret_key = findViewById(R.id.secret_key);

        choose_image_button.setOnClickListener(view -> ImageChooser());

        decode_button.setOnClickListener(view -> {
            if (null != filepath) {
                ImageSteganography imageSteganography = new ImageSteganography(secret_key.getText().toString(),
                        original_image);
                TextDecoding textDecoding = new TextDecoding(Decode.this, Decode.this);
                textDecoding.execute(imageSteganography);
            }
        });
    }

    private void ImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выбери изображение"), INTENT_FOR_CHOOSING_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_FOR_CHOOSING_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData();
            try {
                original_image = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                imageView.setImageBitmap(original_image);
            } catch (Exception e) {
                // обработка ошибки
            }
        }

    }

    @Override
    public void onStartTextEncoding() {
        // обработка до шифрования текста
    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {
        if (result != null) {
            if (!result.isDecoded())
                textView.setText("Не удалось найти текст в изображении");
            else {
                if (!result.isSecretKeyWrong()) {
                    textView.setText("Получен текст из изображения");
                    message.setText(result.getMessage());
                } else {
                    textView.setText("Неправильный ключ");
                }
            }
        } else {
            textView.setText("Сначала нужно выбрать изображение");
        }
    }
}
