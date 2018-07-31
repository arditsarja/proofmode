package org.witness.proofmode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class Main2Activity extends AppCompatActivity {
    CameraPhoto cameraPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraPhoto = new CameraPhoto(getApplicationContext());
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);


    }


    public void openCamera(View view) {
        try {
            startActivityForResult(cameraPhoto.takePhotoIntent(), 12345);
        } catch (Exception e) {
            Log.v("Message error", e.getMessage());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 12345) {
                cameraPhoto.addToGallery();
            }
        }
    }

}
