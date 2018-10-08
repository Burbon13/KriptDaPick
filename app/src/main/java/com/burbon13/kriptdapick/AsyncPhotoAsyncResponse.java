package com.burbon13.kriptdapick;

interface AsyncPhotoAsyncResponse {
    void onFinishEncryption(ImageSavedDTO result);
    void onFinishDecryption(String output);
    void onFinishSaving(ImageSavedDTO result);
}