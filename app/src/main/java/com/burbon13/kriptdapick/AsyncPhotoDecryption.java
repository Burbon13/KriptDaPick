package com.burbon13.kriptdapick;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncPhotoDecryption extends AsyncTask<ImageDTO, Void, String> {
    private AsyncPhotoAsyncResponse context;
    private Bitmap bitmap;
    private static final String TAG = "AsyncPhotoDecryption";

    @Override
    protected String doInBackground(ImageDTO... dtos) {
        bitmap = dtos[0].getBitmap();
        context = dtos[0].getContext();
        return decryptText(decryptLength());
    }

    private int decryptLength() {
        int length = 0, bit0, bit1, bit2, bit3, color, toAdd;
        for(int i = 0; i < 4; i++) {
            color = bitmap.getPixel(0,i);
            Log.d(TAG, Integer.toBinaryString(color));
            color = (color &  (1 | (1 << 8) | (1 << 16) | (1 << 24)));
            bit0 = color & (1);
            bit1 = (color & (1 << 8)) >> 8;
            bit2 = (color & (1 << 16)) >> 16;
            bit3 = (color & (1 << 24)) >> 24;
            toAdd = bit0 + (bit1 << 1) + (bit2 << 2) + (bit3 << 3);
            toAdd <<= 4 * i;
            length += toAdd;
        }
        return length;
    }

    private String decryptText(int length) {
        int x = 0, y = 4, color, bit0, bit1, bit2, bit3, toAdd;
        StringBuilder strB = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int c = 0;
            for(int j = 0; j < 4; j++) {
                color = bitmap.getPixel(x,y);
                y++;
                if(y >= bitmap.getHeight()) {
                    x ++;
                    y = 0;
                }
                color = (color &  (1 | (1 << 8) | (1 << 16) | (1 << 24)));
                bit0 = color & (1);
                bit1 = (color & (1 << 8)) >> 8;
                bit2 = (color & (1 << 16)) >> 16;
                bit3 = (color & (1 << 24)) >> 24;
                toAdd = bit0 + (bit1 << 1) + (bit2 << 2) + (bit3 << 3);
                toAdd <<= 4 * j;
                c += toAdd;
            }
            strB.append((char)c);
        }
        return strB.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        context.onFinishDecryption(s);
    }
}
