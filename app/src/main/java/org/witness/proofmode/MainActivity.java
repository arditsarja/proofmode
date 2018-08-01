package org.witness.proofmode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.witness.proofmode.crypto.PgpUtils;
import org.witness.proofmode.util.GPSTracker;
import org.witness.proofmode.utils.DefaultCallback;
import org.witness.proofmode.utils.EasyImage;
import org.witness.proofmode.utils.ImageAdapter;
import org.witness.proofmode.utils.ImagesAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mPrefs;

    private final static int REQUEST_CODE_INTRO = 9999;

    private PgpUtils mPgpUtils;
    private static final String PHOTOS_KEY = "easy_image_photos_list";

    protected RecyclerView recyclerView;

    protected View galleryButton;

    private ImagesAdapter imagesAdapter;

    private ArrayList<File> photos = new ArrayList<>();
    CameraPhoto cameraPhoto;
    private ImageAdapter imageAdapter;


    GridView imagegrid;
    View contentMain, contentMedia;
    ImageButton showContentMediaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        contentMain = (View) findViewById(R.id.content_main);
        contentMedia = (View) findViewById(R.id.content_media);
        showContentMediaButton = (ImageButton) findViewById(R.id.show_content_media_btn);
        showContentMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePermisions();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        cameraPhoto = new CameraPhoto(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SwitchCompat switchProof = findViewById(R.id.switchProof);
        switchProof.setChecked(mPrefs.getBoolean("doProof", true));

        switchProof.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mPrefs.edit().putBoolean("doProof", isChecked).commit();
                if (isChecked) {
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
                }
            }
        });

        SwitchCompat switchLocation = (SwitchCompat) findViewById(R.id.switchLocation);
        switchLocation.setChecked(mPrefs.getBoolean("trackLocation", true));
        switchLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mPrefs.edit().putBoolean("trackLocation", isChecked).commit();

                if (isChecked) {
                    askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 1);
                    refreshLocation();
                }
            }
        });

        SwitchCompat switchMobile = (SwitchCompat) findViewById(R.id.switchCellInfo);
        switchMobile.setChecked(mPrefs.getBoolean("trackMobileNetwork", false));
        switchMobile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mPrefs.edit().putBoolean("trackMobileNetwork", isChecked).commit();

                if (isChecked) {
                    askForPermission(Manifest.permission.READ_PHONE_STATE, 1);
                }
            }
        });

        SwitchCompat switchDevice = (SwitchCompat) findViewById(R.id.switchDevice);
        switchDevice.setChecked(mPrefs.getBoolean("trackDeviceId", true));
        switchDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mPrefs.edit().putBoolean("trackDeviceId", isChecked).commit();

            }
        });

        SwitchCompat switchNotarize = (SwitchCompat) findViewById(R.id.switchNotarize);
        switchNotarize.setChecked(mPrefs.getBoolean("autoNotarize", true));
        switchNotarize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mPrefs.edit().putBoolean("autoNotarize", isChecked).commit();

            }
        });

        if (mPrefs.getBoolean("firsttime", true)) {
            startActivityForResult(new Intent(this, PMAppIntro.class), REQUEST_CODE_INTRO);
            mPrefs.edit().putBoolean("firsttime", false).commit();
        } else {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 1);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        12345);
            }
        }

        imagegrid = (GridView) findViewById(R.id.surfaceView);
        imageAdapter = new ImageAdapter(this);
        imagegrid.setAdapter(imageAdapter);
        Nammu.init(this);

        recyclerView = findViewById(R.id.recycler_view);
        galleryButton = findViewById(R.id.gallery_button);

        if (savedInstanceState != null) {
            photos = (ArrayList<File>) savedInstanceState.getSerializable(PHOTOS_KEY);
        }

        imagesAdapter = new ImagesAdapter(this, photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imagesAdapter);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    //Nothing, this sample saves to Public gallery so it needs permission
                }

                @Override
                public void permissionRefused() {
                    finish();
                }
            });
        }

        EasyImage.configuration(this)
                .setImagesFolderName(getString(R.string.app_name))
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        checkGalleryAppAvailability();


        findViewById(R.id.gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
                EasyImage.openGallery(MainActivity.this, 0);
            }
        });


        findViewById(R.id.camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCameraForImage(MainActivity.this, 0);
            }
        });

        findViewById(R.id.camera_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCameraForVideo(MainActivity.this, 0);
            }
        });

        findViewById(R.id.documents_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */

                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    EasyImage.openDocuments(MainActivity.this, 0);
                } else {
                    Nammu.askForPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                        @Override
                        public void permissionGranted() {
                            EasyImage.openDocuments(MainActivity.this, 0);
                        }

                        @Override
                        public void permissionRefused() {

                        }
                    });
                }
            }
        });

        findViewById(R.id.chooser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openChooserWithDocuments(MainActivity.this, "Pick source", 0);
            }
        });


        findViewById(R.id.chooser_button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openChooserWithGallery(MainActivity.this, "Pick source", 0);
            }
        });

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
    }

    private void checkGalleryAppAvailability() {
        if (!EasyImage.canDeviceHandleGallery(this)) {
            //Device has no app that handles gallery intent
            galleryButton.setVisibility(View.GONE);
        }
    }







    @Override
    protected void onResume() {
        super.onResume();

        SwitchCompat switchProof = (SwitchCompat) findViewById(R.id.switchProof);
        switchProof.setChecked(mPrefs.getBoolean("doProof", true));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //Location
            case 1:
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 2);
                break;
            //Call
            case 2:
                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 3);

                break;

            case 3:
                askForPermission(Manifest.permission.ACCESS_NETWORK_STATE, 4);

                break;

            case 4:
                askForPermission(Manifest.permission.READ_PHONE_STATE, 5);
                break;

        }

    }

    public void openCamera(View view) {
        EasyImage.openCameraForImage(MainActivity.this, 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {

            startActivity(new Intent(this, PMAppIntro.class));

            return true;
        } else if (id == R.id.action_publish_key) {

            publishKey();

            return true;
        } else if (id == R.id.action_share_key) {

            shareKey();

            return true;
        } else if (id == R.id.change_permissions) {

            changePermisions();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changePermisions() {
        if (contentMain.getVisibility() == View.GONE) {
            contentMedia.setVisibility(View.GONE);
            contentMain.setVisibility(View.VISIBLE);
            showContentMediaButton.setVisibility(View.VISIBLE);
        } else {
            contentMain.setVisibility(View.GONE);
            showContentMediaButton.setVisibility(View.GONE);
            contentMedia.setVisibility(View.VISIBLE);
        }


    }

    public void goOnTest(View v) {
        goOnTest();
    }

    public void goOnTest2(View v) {
        goOnTest2();
    }

    private void goOnTest() {
        startActivity(new Intent(this, AddImageActivity.class));
    }

    private void goOnTest2() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private boolean askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }

            return true;
        } else {

            return false;
        }
    }

    private void publishKey() {

        try {
            if (mPgpUtils == null)
                mPgpUtils = PgpUtils.getInstance(this, mPrefs.getString("password", PgpUtils.DEFAULT_PASSWORD));

            mPgpUtils.publishPublicKey();
            String fingerprint = mPgpUtils.getPublicKeyFingerprint();

            Toast.makeText(this, R.string.open_public_key_page, Toast.LENGTH_LONG).show();

            openUrl(PgpUtils.URL_LOOKUP_ENDPOINT + fingerprint);
        } catch (IOException ioe) {
            Log.e("Proofmode", "error publishing key", ioe);
        }
    }

    private void shareKey() {


        try {

            if (mPgpUtils == null)
                mPgpUtils = PgpUtils.getInstance(this, mPrefs.getString("password", PgpUtils.DEFAULT_PASSWORD));

            mPgpUtils.publishPublicKey();
            String pubKey = mPgpUtils.getPublicKey();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, pubKey);
            startActivity(intent);
        } catch (IOException ioe) {
            Log.e("Proofmode", "error publishing key", ioe);
        }
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_INTRO) {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 1);
        }else{
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });}
//        if (requestCode == 12345) {
//            cameraPhoto.addToGallery();
//            imageAdapter.add(cameraPhoto.getPhotoPath());
//        }
    }

    private void refreshLocation() {
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {
            gpsTracker.getLocation();
        }
    }


    private void unregisterManagers() {
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        EasyImage.clearConfiguration(this);
        super.onDestroy();
        unregisterManagers();
    }
    private void onPhotosReturned(List<File> returnedPhotos) {
        for (File file : returnedPhotos){
            imageAdapter.add(file.getAbsolutePath());
        }
        photos.addAll(returnedPhotos);
        imagesAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(photos.size() - 1);
    }
}
