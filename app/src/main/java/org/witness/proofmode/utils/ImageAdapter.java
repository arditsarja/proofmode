package org.witness.proofmode.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.witness.proofmode.FullscreenActivity;
import org.witness.proofmode.R;

import java.io.File;
import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    Activity activity;
    ArrayList<String> fileList = new ArrayList<String>();// list of file paths

    public ImageAdapter(Activity activity) {
        this.activity = activity;
        getFromSdcard();
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }


    public void add(String s) {
        fileList.add(0, s);
        changeLayout();

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final int thePosition = position;
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
        holder.imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = fileList.get(thePosition);
                Intent intent = new Intent(activity, FullscreenActivity.class);
                intent.putExtra("path",path);
//                File file = new File(path);
////                Uri uri = EasyImageFiles.getUriToFile(activity, file);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //use this if Lollipop_Mr1 (API 22) or above
//                    intent.setDataAndType(FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file), "image/*");
//                } else {
//                    intent.setDataAndType(Uri.fromFile(file), "image/*");
//                }
                activity.startActivity(intent);
            }
        });
        return convertView;
    }

    public void getFromSdcard() {
        File[] listFile;
        String path = Environment.getExternalStorageDirectory().toString() + "/Pictures/Lexally";
        File file = new File(path);
//        File file= new File(android.os.Environment.getExternalStorageDirectory(),"Lexally");
        if (file.isDirectory()) {
            listFile = file.listFiles();

            for (int i = listFile.length - 1; i >= 0; i--) {
                fileList.add(listFile[i].getAbsolutePath());
            }
        }
    }

    class ViewHolder {
        ImageView imageview;
    }

}

