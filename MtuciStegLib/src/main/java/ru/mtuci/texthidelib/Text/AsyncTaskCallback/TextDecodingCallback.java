package ru.mtuci.texthidelib.Text.AsyncTaskCallback;

import ru.mtuci.texthidelib.Text.MtuciHideTextInImage;

/**
 * This the callback interface for TextDecoding AsyncTask.
 */

public interface TextDecodingCallback {

    void onStartTextEncoding();

    void onCompleteTextEncoding(MtuciHideTextInImage result);

}
