package com.burbon13.kriptdapick;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class AsyncPhotoSaver extends AsyncTask<Void, Void, String> {
    private Bitmap bitmap;
    private AsyncPhotoAsyncResponse context;

    AsyncPhotoSaver(ImageDTO imgDTO) {
        this.bitmap = imgDTO.getBitmap();
        this.context = imgDTO.getContext();
    }

    private static final String TAG = "AsyncPhotoSaver";

    @Override
    protected String doInBackground(Void... voids) {
        //TODO: Understand the code below
        Log.d(TAG, "Started async");
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Pictures/KriptDaPick");
        myDir.mkdirs();
        Log.d(TAG, "Created directories");
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "IMG-" + n + ".jpg";
        File file = new File(myDir, fname);
        if(file.exists()) {
            file.delete();
            Log.d(TAG, "File already existed, deleted it");
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            Log.d(TAG, "Compressing bitmap");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.d(TAG, "Closing stream");
        } catch(Exception e) {
            Log.w(TAG, e.getMessage());
            return e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        context.onFinishSaving(s);
    }
}
