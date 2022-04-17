package com.ayush.mtucisteg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.ayush.mtucisteglib.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.mtucisteglib.Text.ImageSteganography;
import com.ayush.mtucisteglib.Text.TextDecoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Decode extends AppCompatActivity implements TextDecodingCallback {

    // Создаём переменные для элементов интерфейса
    private TextView textView;
    private ImageView imageView;
    private EditText message;
    private EditText secret_key;
    private Uri filepath;
    private Bitmap original_image;
    ActivityResultLauncher<Intent> chooseImageForDecodingActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    filepath = Objects.requireNonNull(data).getData();
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(filepath);
                        original_image = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                        imageView.setImageBitmap(original_image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

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

        choose_image_button.setOnClickListener(view -> select_image_for_decoding());

        decode_button.setOnClickListener(view -> {
            if (null != filepath) {
                ImageSteganography imageSteganography = new ImageSteganography(secret_key.getText().toString(),
                        original_image);
                TextDecoding textDecoding = new TextDecoding(Decode.this, Decode.this);
                textDecoding.execute(imageSteganography);
            }
        });
    }

    private void select_image_for_decoding() {
        Intent int_choose_image = new Intent(Intent.ACTION_PICK);
        int_choose_image.setType("image/*");
        chooseImageForDecodingActivityResultLauncher.launch(int_choose_image);
    }

    @Override
    public void onStartTextEncoding() {
        // обработка до шифрования текста (особенность TextEncodingCallback)
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
