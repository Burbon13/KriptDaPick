package com.burbon13.kriptdapick;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

class AsyncPhotoEncryption extends AsyncTask<ImageDTO, Void, ImageSavedDTO> {
    private AsyncPhotoAsyncResponse context;
    private Bitmap bitmap;
    private String text;
    private static final String TAG = "AsyncPhotoEncryption";

    @Override
    protected ImageSavedDTO doInBackground(ImageDTO... dtos) {
        bitmap = dtos[0].getBitmap();
        text = dtos[0].getText();
        context = dtos[0].getContext();

        if(!isImageBigEnough())
            return new ImageSavedDTO("Image size too small", -1);

        //First implementation: put on the first pixels the data
        //First 4 pixels will contain the length of the string
        encryptLength();

        //Continue with the rest to encrypt the text
        encryptText();
        return new ImageSavedDTO("Text hid in the picture", 0);
    }

    private boolean isImageBigEnough() {
        return ((bitmap.getHeight() * bitmap.getWidth() - 4) / 4) > text.length();
    }

    private void encryptLength() {
        //Will hide the length in the following pixels (assuming length is not longer than short):
        //(0,0),(0,1),(0,2),(0,3) - consider the length to be short
        int length = text.length();
        int color, bit1, bit2, bit3, bit0;
        for(int i = 0; i < 4; i++) {
            color = bitmap.getPixel(0,i);
            bit0 = length & 1;
            bit1 = (length & 2) >> 1;
            bit2 = (length & 4) >> 2;
            bit3 = (length & 8) >> 3;
            length >>= 4;

            color = (color & (~ (1 | (1 << 8) | (1 << 16) | (1 << 24))));
            color |= bit0;
            color |= (bit1 << 8);
            color |= (bit2 << 16);
            color |= (bit3 << 24);
            bitmap.setPixel(0,i,color);
        }
    }

    private void encryptText() {
        int x = 0, y = 4, color, bit0, bit1, bit2, bit3;
        for(int index = 0; index < text.length(); index ++) {
            //For every char we need 4 pixels (a char occupies 2 bytes)
            char c = text.charAt(index);
            for(int i = 0; i < 4; i++) {
                color = bitmap.getPixel(x,y);

                bit0 = c & 1;
                bit1 = (c & 2) >> 1;
                bit2 = (c & 4) >> 2;
                bit3 = (c & 8) >> 3;
                c >>= 4;

                color = (color & (~ (1 | (1 << 8) | (1 << 16) | (1 << 24))));
                color |= bit0;
                color |= (bit1 << 8);
                color |= (bit2 << 16);
                color |= (bit3 << 24);
                bitmap.setPixel(x,y,color);

                y++;
                if(y >= bitmap.getHeight()) {
                    x ++;
                    y = 0;
                }
            }
        }
    }

    @Override
    protected void onPostExecute(ImageSavedDTO result) {
        context.onFinishEncryption(result);
    }
}
