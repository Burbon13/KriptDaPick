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

//--------------IDEAS--------------
//1) Make encryption and decryption on threads ^^
//---------------------------------

public class MainActivity extends AppCompatActivity implements AsyncPhotoAsyncResponse {
    private static final int PHOTO_PICKER_CODE = 123;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 69;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 73;
    public static final int MAX_TEXT_LENGTH = 30000;
    private static final String TAG = "MainActivity";
    private Bitmap imageBitmap = null;
    private ImageView ivEncrypt = null;
    private EditText etData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ivFullScreen).setVisibility(View.GONE);
        findViewById(R.id.pbAsync).setVisibility(View.GONE);
        findViewById(R.id.ivBack).setVisibility(View.GONE);
        ivEncrypt = findViewById(R.id.ivEncrypt);
        etData = findViewById(R.id.etData);
        setListeners();
    }

    private void setListeners() {
        //Image view to select image to encrypt
        findViewById(R.id.ivEncrypt).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(imageBitmap == null) {
                    Toast.makeText(getApplicationContext(), R.string.no_selection,
                            Toast.LENGTH_LONG).show();
                    return;
                }

//                Intent intent = new Intent(getApplicationContext(),FullScreenImage.class);
//                intent.putExtra("bitmapImage", imageBitmap);
//                startActivity(intent);
                makePictureFullScreen();
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

        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePictureDisappear();
            }
        });
    }

    private void makePictureFullScreen() {
        findViewById(R.id.ivEncrypt).setEnabled(false);
        findViewById(R.id.buDecrypt).setEnabled(false);
        findViewById(R.id.buEncrypt).setEnabled(false);
        findViewById(R.id.buSelect).setEnabled(false);
        findViewById(R.id.etData).setEnabled(false);
        findViewById(R.id.ivDownload).setEnabled(false);
        findViewById(R.id.ivEncrypt).setVisibility(View.GONE);
        findViewById(R.id.buDecrypt).setVisibility(View.GONE);
        findViewById(R.id.buEncrypt).setVisibility(View.GONE);
        findViewById(R.id.buSelect).setVisibility(View.GONE);
        findViewById(R.id.etData).setVisibility(View.GONE);
        findViewById(R.id.ivDownload).setVisibility(View.GONE);
        ImageView ivEncrypt = (ImageView) findViewById(R.id.ivFullScreen);
        ivEncrypt.setImageBitmap(imageBitmap);
        ivEncrypt.setVisibility(View.VISIBLE);
        findViewById(R.id.ivBack).setEnabled(true);
        findViewById(R.id.ivBack).setVisibility(View.VISIBLE);
    }

    private void makePictureDisappear() {
        findViewById(R.id.ivEncrypt).setEnabled(true);
        findViewById(R.id.buDecrypt).setEnabled(true);
        findViewById(R.id.buEncrypt).setEnabled(true);
        findViewById(R.id.buSelect).setEnabled(true);
        findViewById(R.id.etData).setEnabled(true);
        findViewById(R.id.ivDownload).setEnabled(true);
        findViewById(R.id.ivEncrypt).setVisibility(View.VISIBLE);
        findViewById(R.id.buDecrypt).setVisibility(View.VISIBLE);
        findViewById(R.id.buEncrypt).setVisibility(View.VISIBLE);
        findViewById(R.id.buSelect).setVisibility(View.VISIBLE);
        findViewById(R.id.etData).setVisibility(View.VISIBLE);
        findViewById(R.id.ivDownload).setVisibility(View.VISIBLE);
        ImageView ivEncrypt = (ImageView) findViewById(R.id.ivFullScreen);
        ivEncrypt.setImageBitmap(imageBitmap);
        ivEncrypt.setVisibility(View.GONE);
        findViewById(R.id.ivBack).setVisibility(View.GONE);
        findViewById(R.id.ivBack).setEnabled(false);
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
        Log.v(TAG, "Image loaded from memory: " + pickedImageUri);
        try {
            if(MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImageUri).getWidth() < 100
                    || MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImageUri).getHeight() < 100) {
                Toast.makeText(getApplicationContext(), R.string.image_too_small, Toast.LENGTH_LONG).show();
                Log.v(TAG, "Image " + pickedImageUri + " too small, not saving it");
                return;
            }

            imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImageUri)
            .copy(Bitmap.Config.ARGB_8888, true);

            ImageView ivEncrypt = (ImageView) findViewById(R.id.ivEncrypt);
            ivEncrypt.setImageBitmap(imageBitmap);
            // the setPixel() method was multiplying the red,green, and blue values
            // by the alpha, then only setting r,g,b.
            imageBitmap.setHasAlpha(true); //Huh
        } catch (IOException e) {
            Log.w(TAG, "Loading image did not work: " + e.getMessage());
        }

    }

    public void onEncryptEvent(View view) {
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

        ImageDTO imageDTO = new ImageDTO(imageBitmap, etData.getText().toString(),this);

        prepareForAsync();

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
    public void onFinishEncryption(ImageSavedDTO result) {
        if(result.getReturnCode() == 0)
            ivEncrypt.setImageBitmap(imageBitmap);
        Toast.makeText(getApplicationContext(), result.getData(), Toast.LENGTH_LONG).show();
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








