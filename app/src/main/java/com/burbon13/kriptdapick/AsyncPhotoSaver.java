package com.burbon13.kriptdapick;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class AsyncPhotoSaver extends AsyncTask<Void, Void, ImageSavedDTO> {
    private Bitmap bitmap;
    private AsyncPhotoAsyncResponse context;

    AsyncPhotoSaver(ImageDTO imgDTO) {
        this.bitmap = imgDTO.getBitmap();
        this.context = imgDTO.getContext();
    }

    private static final String TAG = "AsyncPhotoSaver";

    @Override
    protected ImageSavedDTO doInBackground(Void... voids) {
        //TODO: Understand the code below
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Pictures/KriptDaPick");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "IMG-" + n + ".jpg";
        File file = new File(myDir, fname);
        if(file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch(Exception e) {
            Log.w(TAG, e.getMessage());
            //return "ERROR: " + e.getMessage();
            return new ImageSavedDTO(e.getMessage(), -1);
        }
        //return root + "/Pictures/KriptDaPick/" + fname;
        return new ImageSavedDTO(root + "/Pictures/KriptDaPick/" + fname, 0);
    }

    @Override
    protected void onPostExecute(ImageSavedDTO s) {
        context.onFinishSaving(s);
    }
}
