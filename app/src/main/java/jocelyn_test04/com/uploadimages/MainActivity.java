package jocelyn_test04.com.uploadimages;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_PERMISSION_CODE = 1;
    public static final int REQUEST_RESULT_CODE = 0;
    public static final int GALLERY_REQUEST_CODE = 2;

    public static int img_Count = 0;

    final String url = "http://192.1**.*.*/myphp/androidApp/uploadimage.php";

    private ImageView ivCamera, ivGallery, ivUpload;
    private LinearLayout linearMain;
    private Button btnRemove;

    protected ImageView imageView;

    private ArrayList<ImageView> imgViewList = new ArrayList<>();

    private String mImageLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ivCamera = (ImageView)findViewById(R.id.iVcamera);
        ivGallery = (ImageView)findViewById(R.id.iVgallery);
        ivUpload = (ImageView)findViewById(R.id.iVupload);

        btnRemove = (Button)findViewById(R.id.btnMove);

        linearMain = (LinearLayout)findViewById(R.id.linearMain);

        ivCamera.setOnClickListener(this);
        ivGallery.setOnClickListener(this);
        ivUpload.setOnClickListener(this);
        btnRemove.setOnClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iVcamera:
                imageView = new ImageView(this.getApplicationContext());
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(0,0,0,10);
                imageView.setAdjustViewBounds(true);

                takephoho();

                break;
            case R.id.iVgallery:
                selectImageByGallery();
                break;
            case R.id.iVupload:
                if(imgViewList.size() != 0) {
                    Uploader uploader = new Uploader(MainActivity.this, imgViewList, url);
                    uploader.execute();

                }else {
                    Toast.makeText(this.getApplicationContext(),
                            "Add Images First",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnMove:
                if(imgViewList.size() != 0) {
                    linearMain.removeAllViews();
                    imgViewList.clear();
                    img_Count = 0;

                }else {
                    Toast.makeText(this.getApplicationContext(),
                            "NO More Images",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void selectImageByGallery(){
        Intent selectIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(selectIntent, GALLERY_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void takephoho(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            callCameraAct();
        }else{

           if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this,
                        "External storage permission required to save images",
                        Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                callCameraAct();
            }else{
                Toast.makeText(this,
                        "External write permission has not been granted,Cannot saved images",
                                Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void callCameraAct() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImgFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(photoFile != null){
            Uri photo_URI = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                     photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo_URI);
            startActivityForResult(cameraIntent, REQUEST_RESULT_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_RESULT_CODE && resultCode == RESULT_OK){

            rotateImage(setReducedImageSize());
            linearMain.addView(imageView);

            if(img_Count == 0){
                imgViewList.clear();
            }
            imgViewList.add(imageView);
            img_Count++;
        }
        else if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri selectImageUri = data.getData();
            imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(0,0,0,10);
            imageView.setAdjustViewBounds(true);

            imageView.setImageURI(selectImageUri);
            linearMain.addView(imageView);

            if(img_Count == 0){
                imgViewList.clear();
            }
            imgViewList.add(imageView);
            img_Count++;
        }

    }

    private File createImgFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMAGE_" + timeStamp +".jpg";
        File image = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + imageName);

        mImageLocation = image.getAbsolutePath();

        return image;
    }

    private Bitmap setReducedImageSize(){
        int targetW = imageView.getLayoutParams().width;
        int targetH = imageView.getLayoutParams().height;

        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageLocation,bfOptions);

        int newImgW = bfOptions.outWidth;
        int newImgH = bfOptions.outHeight;

        int scaleFactor = Math.min(newImgW/targetW, newImgH/targetH);
        bfOptions.inSampleSize = scaleFactor;
        bfOptions.inJustDecodeBounds = false;

        Bitmap smallImage = BitmapFactory.decodeFile(mImageLocation,bfOptions);
        return smallImage;
    }

    private void rotateImage(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(mImageLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90 :
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180 :
                matrix.setRotate(180);
                break;
            default:
                break;
        }

        Bitmap rotateImage = Bitmap.createBitmap(bitmap,0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix,true);

        imageView.setImageBitmap(rotateImage);
    }
}
