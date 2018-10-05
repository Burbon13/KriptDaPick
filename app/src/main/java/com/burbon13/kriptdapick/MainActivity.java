package com.burbon13.kriptdapick;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int PHOTO_PICKER_CODE = 123;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListeners();
    }

    private void setListeners() {
        //Image view to select image to encrypt
        findViewById(R.id.ivEncrypt).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pickPhotoFromPhone();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(pickedImageUri, filePath, null, null , null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
        cursor.close();
        
        ImageView ivEncrypt = (ImageView) findViewById(R.id.ivEncrypt);
        ivEncrypt.setImageBitmap(BitmapFactory.decodeFile(imagePath));


    }
}
