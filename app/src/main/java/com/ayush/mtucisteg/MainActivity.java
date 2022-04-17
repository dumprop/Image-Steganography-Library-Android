package com.ayush.mtucisteg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button encode = findViewById(R.id.encode_button);
        Button decode = findViewById(R.id.decode_button);

        encode.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), Encode.class)));

        decode.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), Decode.class)));

    }

}
