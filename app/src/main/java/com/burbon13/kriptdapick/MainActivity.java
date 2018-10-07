package com.burbon13.kriptdapick;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;


//TODO: See where to move the interfaces & change the fucking name :))))
interface AsyncPhotoAsyncResponse {
    void onFinishEncryption(String output);
    void onFinishDecryption(String output);
}

public class MainActivity extends AppCompatActivity implements AsyncPhotoAsyncResponse {
    private static final int PHOTO_PICKER_CODE = 123;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 69;
    private static final String TAG = "MainActivity";
    private Bitmap imageBitmap = null;
    private ImageView ivEncrypt = null;
    private EditText etData = null;
    private boolean imageIsSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivEncrypt = findViewById(R.id.ivEncrypt);
        etData = findViewById(R.id.etData);
        setListeners();
    }

    private void setListeners() {
        //Image view to select image to encrypt
        findViewById(R.id.ivEncrypt).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: Make picture fullscreen to let the user examine it
                Toast.makeText(getApplicationContext(), "Make full screen", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pickPhotoFromPhone() {
        //Checking for permissions
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE);
            return;
        }

        pickPhotoStartActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickPhotoFromPhone();
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void pickPhotoStartActivity() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, PHOTO_PICKER_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PHOTO_PICKER_CODE && resultCode == RESULT_OK && data != null)
            loadImageFromUri(data.getData());
    }

    private void loadImageFromUri(Uri pickedImageUri) {
//        Log.d(TAG, "Uri: " + pickedImageUri);
//        String[] filePath = {MediaStore.Images.Media.DATA};
//        Log.d(TAG, "MediaStore.Images.Media.DATA: " + MediaStore.Images.Media.DATA);
//        Cursor cursor = getContentResolver().query(pickedImageUri, filePath, null,
//                null , null);
//        Log.d(TAG, "Cursor: " + cursor);
//        cursor.moveToFirst();
//        Log.d(TAG, "Cursor -> move to firse: " + cursor);
//        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
//        Log.d(TAG, "imagePath: " + imagePath);
//        cursor.close();

        //TODO: Verify is the image is at least 100X100
        Log.d(TAG, "image uri: " + pickedImageUri);
        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImageUri)
            .copy(Bitmap.Config.ARGB_8888, true);

            ImageView ivEncrypt = (ImageView) findViewById(R.id.ivEncrypt);
            ivEncrypt.setImageBitmap(imageBitmap);
            // the setPixel() method was multiplying the red,green, and blue values
            // by the alpha, then only setting r,g,b.
            imageBitmap.setHasAlpha(true); //Huh
            imageIsSelected = true;
        } catch (IOException e) {
            Log.w(TAG, "Loading imageBitmap did not work: " + e.getMessage());
            Log.d(TAG, "exception", e);
        }

    }

    public void onEncryptEvent(View view) {
        //Make a thread here boss!
        if(imageBitmap == null) {
            Toast.makeText(getApplicationContext(), R.string.no_selection, Toast.LENGTH_LONG).show();
            return;
        }

        //TODO:String length max 32767
        ImageDTO imageDTO = new ImageDTO(imageBitmap, etData.getText().toString(),this);

        //TODO: Verify first if the image is big enough!
        AsyncPhotoEncryption myThread = new AsyncPhotoEncryption();
        myThread.execute(imageDTO,null,null);
        //TODO: May want to have an encryption function for the string
    }

    public void onDecryptEvent(View view) {
        if(imageBitmap == null) {
            Toast.makeText(getApplicationContext(), R.string.no_selection, Toast.LENGTH_LONG).show();
            return;
        }

        ImageDTO imageDTO = new ImageDTO(imageBitmap, null, this);

        AsyncPhotoDecryption myThread = new AsyncPhotoDecryption();
        myThread.execute(imageDTO,null,null);
    }

    public void onSelectEvent(View view) {
        pickPhotoFromPhone();
    }

    @Override
    public void onFinishEncryption(String output) {
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
        ivEncrypt.setImageBitmap(imageBitmap);
    }

    @Override
    public void onFinishDecryption(String output) {
        Toast.makeText(getApplicationContext(), "Decryption finished", Toast.LENGTH_LONG).show();
        etData.setText(output);
    }
}








