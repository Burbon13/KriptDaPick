package com.burbon13.kriptdapick;

import android.graphics.Bitmap;

class ImageDTO {
    private Bitmap bitmap;
    private String text;
    private AsyncPhotoAsyncResponse context;

    ImageDTO(Bitmap bitmap, String text, AsyncPhotoAsyncResponse context) {
        this.bitmap = bitmap;
        this.text = text;
        this.context = context;
    }

    public AsyncPhotoAsyncResponse getContext() {
        return context;
    }

    public String getText() {
        return text;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}