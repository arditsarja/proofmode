package org.witness.proofmode;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraPhoto {
    private String photoPath;
    private Context context;
    private File photoFile;
    private String appName = "Lexally";


    public String getPhotoPath() {
        return this.photoPath;
    }

    public CameraPhoto(Context context) {
        this.context = context;
    }

    public Intent takePhotoIntent() {
        Intent in = new Intent("android.media.action.IMAGE_CAPTURE");
        if (in.resolveActivity(this.context.getPackageManager()) != null) {
            photoFile = this.createImageFile();
            if (photoFile != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //use this if Oreo (API 26) or above
//                    in.putExtra("output", Uri.fromFile(photoFile));
//                }
//                else
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //use this if Lollipop_Mr1 (API 22) or above
                    in.putExtra("output", FileProvider.getUriForFile(context, context.getPackageName() + ".provider", photoFile));
                } else {
                    in.putExtra("output", Uri.fromFile(photoFile));
                }
//                    in.putExtra("output", Uri.fromFile(photoFile));
            }
        }

        return in;
    }

    private File createImageFile() {
        String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //use this if Lollipop_Mr1 (API 22) or above
            storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {

            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
//        File fileTemp = new File(storageDir, appName);
//        fileTemp.mkdirs();     //if not, create it
//        File image = new File(fileTemp, imageFileName + ".jpg");
        File image = new File(storageDir, imageFileName + ".jpg");
        this.photoPath = image.getAbsolutePath();
        return image;
    }

    public void addToGallery() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(this.photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.context.sendBroadcast(mediaScanIntent);
    }

    public ArrayList<Byte> convertFileToByteArrayString() {
        byte[] byteArray = null;
        try {
            InputStream inputStream = new FileInputStream(photoFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        return new String(byteArray, StandardCharsets.UTF_8);

        return getList(byteArray);
    }

    private ArrayList<Byte> getList(byte[] byteArray) {
        ArrayList<Byte> list = new ArrayList<>();
        for (byte theByte : byteArray) {
            list.add(theByte);
        }
        return list;
    }

    public ArrayList<String> getData() {
        ArrayList<String> result = new ArrayList<String>();
        result.add("photo");
        result.add(getPhotoPath());
//        result.add(convertFileToByteArrayString());
        return result;
    }


}
