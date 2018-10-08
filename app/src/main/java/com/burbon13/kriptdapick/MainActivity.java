package com.burbon13.kriptdapick;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


//TODO: See where to move the interfaces & change the fucking name :))))
interface AsyncPhotoAsyncResponse {
    void onFinishEncryption(String output);
    void onFinishDecryption(String output);
    void onFinishSaving(ImageSavedDTO result);
}

public class MainActivity extends AppCompatActivity implements AsyncPhotoAsyncResponse {
    private static final int PHOTO_PICKER_CODE = 123;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 69;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 73;
    public static final int MAX_TEXT_LENGTH = 30000;
    private static final String TAG = "MainActivity";
    private Bitmap imageBitmap = null;
    private ImageView ivEncrypt = null;
    private EditText etData = null;
    private boolean imageIsSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.pbAsync).setVisibility(View.GONE);
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

        findViewById(R.id.ivDownload).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        saveImagePermission();
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void prepareForAsync() {
        findViewById(R.id.pbAsync).setVisibility(View.VISIBLE);
        findViewById(R.id.buDecrypt).setEnabled(false);
        findViewById(R.id.buEncrypt).setEnabled(false);
        findViewById(R.id.buSelect).setEnabled(false);
        findViewById(R.id.etData).setEnabled(false);
        findViewById(R.id.ivDownload).setEnabled(false);
    }

    private void restoreAfterAsync() {
        findViewById(R.id.pbAsync).setVisibility(View.GONE);
        findViewById(R.id.buDecrypt).setEnabled(true);
        findViewById(R.id.buEncrypt).setEnabled(true);
        findViewById(R.id.buSelect).setEnabled(true);
        findViewById(R.id.etData).setEnabled(true);
        findViewById(R.id.ivDownload).setEnabled(true);
    }

    private void saveImagePermission() {
        //TODO: Save image on phone
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return;
        }
        saveImageToPhone();
    }

    private void saveImageToPhone() {
        prepareForAsync();
        ImageDTO imgDTO = new ImageDTO(imageBitmap, null, this);
        AsyncPhotoSaver myThread = new AsyncPhotoSaver(imgDTO);
        myThread.execute(null,null,null);
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
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveImageToPhone();
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
        //TODO: Stop if the picture is too small
        //Make a thread here boss!
        if(imageBitmap == null) {
            Toast.makeText(getApplicationContext(), R.string.no_selection, Toast.LENGTH_LONG).show();
            return;
        }

        if(etData.getText().length() > MAX_TEXT_LENGTH) {
            Toast.makeText(getApplicationContext(), R.string.text_too_big, Toast.LENGTH_LONG).show();
            return;
        }

        if(etData.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.text_non_existent, Toast.LENGTH_LONG).show();
            return;
        }

        //TODO:String length max 30000
        ImageDTO imageDTO = new ImageDTO(imageBitmap, etData.getText().toString(),this);

        prepareForAsync();

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

        etData.setText("");

        ImageDTO imageDTO = new ImageDTO(imageBitmap, null, this);

        prepareForAsync();

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
        restoreAfterAsync();
    }

    @Override
    public void onFinishDecryption(String output) {
        if(output.length() == 0)
            Toast.makeText(getApplicationContext(), R.string.sth_wrong, Toast.LENGTH_LONG).show();
        else
            etData.setText(output);
        restoreAfterAsync();
    }

    @Override
    public void onFinishSaving(ImageSavedDTO result) {
        restoreAfterAsync();
        if(result.getReturnCode() == -1) {
            Log.w(TAG, "Error on saving image: " + result.getData());
            return;
        }
        //TODO: Make it work...?
        notifyImageSaved(result.getData());
    }

    private void notifyImageSaved(String path) {
        Log.d(TAG, "Scanning");

        ArrayList<String> toBeScanned = new ArrayList<String>();
        toBeScanned.add(path);
        String[] toBeScannedStr = new String[toBeScanned.size()];
        toBeScannedStr = toBeScanned.toArray(toBeScannedStr);
        MediaScannerConnection.scanFile(this, toBeScannedStr, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.d(TAG, "SCAN COMPLETED: " + path);
            }
        });
    }
}








