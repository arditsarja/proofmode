package org.witness.proofmode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    CameraPhoto cameraPhoto;
    private ImageAdapter imageAdapter;
    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsselection;
    private String[] arrPath;
    ArrayList<String> fileList = new ArrayList<String>();// list of file paths
    File[] listFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraPhoto = new CameraPhoto(this);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        getFromSdcard();
        GridView imagegrid = (GridView) findViewById(R.id.surfaceView);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);


    }

    public void getFromSdcard() {
        File file = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //use this if Lollipop_Mr1 (API 22) or above
            file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {

            file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
//        File file= new File(android.os.Environment.getExternalStorageDirectory(),"Lexally");
        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                fileList.add(listFile[i].getAbsolutePath());
            }
        }
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
               imageAdapter.add(cameraPhoto.getPhotoPath());
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return fileList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        public void changeLayout() {
           runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }


        public void add(String s) {
//        this.values.add(0, message);
            fileList.add(s);
            changeLayout();

        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.gelleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Bitmap myBitmap = BitmapFactory.decodeFile(fileList.get(position));
            holder.imageview.setImageBitmap(myBitmap);

            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
    }
}
