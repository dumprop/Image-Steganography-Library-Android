package com.ayush.mtucisteglib.Text.AsyncTaskCallback;

import com.ayush.mtucisteglib.Text.ImageSteganography;

/**
 * This the callback interface for TextEncoding AsyncTask.
 */

public interface TextEncodingCallback {

    void onStartTextEncoding();

    void onCompleteTextEncoding(ImageSteganography result);

}
