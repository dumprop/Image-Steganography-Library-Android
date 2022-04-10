package com.ayush.mtucisteglib.Text.AsyncTaskCallback;

import com.ayush.mtucisteglib.Text.ImageSteganography;

/**
 * This the callback interface for TextDecoding AsyncTask.
 */

public interface TextDecodingCallback {

    void onStartTextEncoding();

    void onCompleteTextEncoding(ImageSteganography result);

}
