package ru.mtuci.steg;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ru.mtuci.texthidelib.Text.AsyncTaskCallback.TextEncodingCallback;
import ru.mtuci.texthidelib.Text.MtuciHideTextInImage;
import ru.mtuci.texthidelib.Text.TextEncoding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Encode extends AppCompatActivity implements TextEncodingCallback {

    private TextView textView;
    private ImageView imageView;
    private EditText message;
    private EditText secret_key;


    private TextEncoding textEncoding;
    private MtuciHideTextInImage mtuciHideTextInImage;
    private ProgressDialog save;
    private Uri filepath;


    private Bitmap original_image;
    ActivityResultLauncher<Intent> chooseImageForHidingActivityResultLauncher = registerForActivityResult(
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
    private Bitmap encoded_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        Button save_image_button = findViewById(R.id.save_image_button);
        secret_key = findViewById(R.id.secret_key);
        imageView = findViewById(R.id.imageview);
        Button encode_button = findViewById(R.id.encode_button);
        textView = findViewById(R.id.whether_encoded);
        message = findViewById(R.id.message);
        Button choose_image_button = findViewById(R.id.choose_image_button);

        checkAndRequestPermissions();

        choose_image_button.setOnClickListener(view -> select_image_for_hiding());

        encode_button.setOnClickListener(view -> {
            textView.setText("");
            if (filepath != null) {
                if (message.getText() != null) {
                    mtuciHideTextInImage = new MtuciHideTextInImage(message.getText().toString(),
                            secret_key.getText().toString(),
                            original_image);
                    textEncoding = new TextEncoding(Encode.this, Encode.this);
                    textEncoding.execute(mtuciHideTextInImage);
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

    public void select_image_for_hiding() {
        Intent int_choose_image = new Intent(Intent.ACTION_PICK);
        int_choose_image.setType("image/*");
        chooseImageForHidingActivityResultLauncher.launch(int_choose_image);

    }

    @Override
    public void onStartTextEncoding() {
        // обработка текст на начале (особенность TextEncodingCallback)
    }

    @Override
    public void onCompleteTextEncoding(MtuciHideTextInImage result) {
        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image();
            textView.setText(R.string.text_is_hidden_in_image);
            imageView.setImageBitmap(encoded_image);
        }
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        OutputStream outputStream;

        File filepath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            filepath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/StegMTUCI");

        } else {
            filepath = new File(Environment.getExternalStorageDirectory() + "/StegMTUCI");
        }
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "Сохраняю в " + filepath.getAbsolutePath(), Toast.LENGTH_SHORT).show());

        if (filepath.exists()) {
            File file = new File(filepath, "HIDDEN.PNG");
            try {
                outputStream = new FileOutputStream(file);
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
                textView.post(() -> save.dismiss());
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "Ошибка при сохранении картинки с сообщением внутри!", Toast.LENGTH_LONG).show());
            }
        } else {
            boolean is_created_dir = filepath.mkdirs();
            if (is_created_dir) {
                File file = new File(filepath, "HIDDEN.PNG");

                try {
                    outputStream = new FileOutputStream(file);
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    textView.post(() -> save.dismiss());
                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "Ошибка при сохранении картинки с сообщением внутри!", Toast.LENGTH_LONG).show());
                }
            } else {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplicationContext(), "Ошибка создания папки в " + filepath.getAbsolutePath(), Toast.LENGTH_LONG).show());
            }
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
        if (!listPermissionsNeeded.isEmpty())
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);

    }


}
