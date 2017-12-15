package com.korsolution.antif;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.anton46.stepsview.StepsView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddCarPictureActivity extends AppCompatActivity {

    private ImageView imgCarFront;
    private ImageView imgCarBack;
    private ImageView imgCarLeft;
    private ImageView imgCarRight;
    private Button btnNext;

    private String IMEI;

    private String photoSelect = "Front";

    private PicturePathDBClass PicturePathDB;
    private PictureNameDBClass PictureNameDB;

    private StepsView mStepsView;

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


    private final static int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    protected static Uri photoUri = null;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car_picture);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        USER_ID = getIntent().getStringExtra("USER_ID");
        IMEI = getIntent().getStringExtra("IMEI");

        PicturePathDB = new PicturePathDBClass(this);
        PicturePathDB.Delete();
        PicturePathDB.Insert("0", "0", "0", "0", "0");

        PictureNameDB = new PictureNameDBClass(this);
        PictureNameDB.Delete();
        PictureNameDB.Insert("0", "0", "0", "0", "0");

        appLog = new AppLogClass(this);

        setupWidgets();

        // steps view
        String[] labels = {"Step 1", "Step 2", "Step 3", "Step 4"};
        mStepsView = (StepsView) findViewById(R.id.stepsView);
        mStepsView.setCompletedPosition(labels.length - 3)
                .setLabels(labels)
                .setBarColorIndicator(this.getResources().getColor(R.color.material_blue_grey_800))
                .setProgressColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .setLabelColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .drawView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }

    private void setupWidgets() {

        imgCarFront = (ImageView) findViewById(R.id.imgCarFront);
        imgCarBack = (ImageView) findViewById(R.id.imgCarBack);
        imgCarLeft = (ImageView) findViewById(R.id.imgCarLeft);
        imgCarRight = (ImageView) findViewById(R.id.imgCarRight);
        btnNext = (Button) findViewById(R.id.btnNext);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            imgCarFront.setEnabled(false);
            imgCarBack.setEnabled(false);
            imgCarLeft.setEnabled(false);
            imgCarRight.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        imgCarFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarPictureActivity", "กดปุ่ม ถ่ายรูป Car Front", USER_ID);

                photoSelect = "Front";
                dialogAlertSelectPhoto();

            }
        });

        imgCarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarPictureActivity", "กดปุ่ม ถ่ายรูป Car Back", USER_ID);

                photoSelect = "Back";
                dialogAlertSelectPhoto();

            }
        });

        imgCarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarPictureActivity", "กดปุ่ม ถ่ายรูป Car Left", USER_ID);

                photoSelect = "Left";
                dialogAlertSelectPhoto();

            }
        });

        imgCarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("AddCarPictureActivity", "กดปุ่ม ถ่ายรูป Car Right", USER_ID);

                photoSelect = "Right";
                dialogAlertSelectPhoto();

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[][] arrData = PicturePathDB.SelectAll();
                if (arrData != null) {
                    String _font = arrData[0][1].toString();
                    String _back = arrData[0][2].toString();
                    String _left = arrData[0][3].toString();
                    String _right = arrData[0][4].toString();

                    if (!_font.equals("0")) {

                        String[][] arrDataName = PictureNameDB.SelectAll();
                        if (arrDataName != null) {
                            String _fontName = arrDataName[0][1].toString();
                            String _backName = arrDataName[0][2].toString();
                            String _leftName = arrDataName[0][3].toString();
                            String _rightName = arrDataName[0][4].toString();

                            appLog.setLog("AddCarPictureActivity", "กดปุ่ม Next", USER_ID);

                            Intent intent = new Intent(getApplicationContext(), AddCarNumberActivity.class);
                            intent.putExtra("USER_ID", USER_ID);
                            intent.putExtra("IMEI", IMEI);
                            intent.putExtra("URI_CAR_FRONT", _font);
                            intent.putExtra("URI_CAR_BACK", _back);
                            intent.putExtra("URI_CAR_LEFT", _left);
                            intent.putExtra("URI_CAR_RIGHT", _right);
                            intent.putExtra("NAME_CAR_FRONT", _fontName);
                            intent.putExtra("NAME_CAR_BACK", _backName);
                            intent.putExtra("NAME_CAR_LEFT", _leftName);
                            intent.putExtra("NAME_CAR_RIGHT", _rightName);
                            startActivity(intent);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "ถ่ายรูปหน้ารถ!!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "ถ่ายรูปรถ!!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void dialogAlertSelectPhoto() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(AddCarPictureActivity.this);

        completeDialog.setTitle("Choose photo from?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(/*android.R.string.yes*/"Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("AddCarPictureActivity", "กดปุ่ม Choose photo from Camera", USER_ID);

                dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);

            }
        }).setNegativeButton(/*android.R.string.no*/"Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("AddCarPictureActivity", "กดปุ่ม Choose photo from Gallery", USER_ID);

                startGalleryIntent();

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }


    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    /*f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));*/

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                        // Do something for lollipop and above versions

                        //Uri photoURI = Uri.fromFile( f);
                        Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createImageFile());
                        mCurrentPhotoPath = photoURI.getPath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    } else{
                        // do something for phones running an SDK before lollipop

                        f = setUpPhotoFile();
                        mCurrentPhotoPath = f.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name_add_car);
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            galleryAddPic();
            mCurrentPhotoPath = null;
        }

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            //Toast.makeText(getApplicationContext(), contentUri.toString(), Toast.LENGTH_SHORT).show();
            contentUri = Uri.parse(String.valueOf(contentUri).replace("external_files", "storage/emulated/0"));
            //Toast.makeText(getApplicationContext(), contentUri.toString(), Toast.LENGTH_SHORT).show();
        }

        // get file name
        String path = f.getAbsolutePath();
        String filename = path.substring(path.lastIndexOf("/") + 1);

        //Log.d("URI", String.valueOf(contentUri));
        switch (photoSelect) {
            case "Front":

                appLog.setLog("AddCarPictureActivity", "ถ่ายรูป Car Front " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarFront);

                PicturePathDB.UpdateDataFront(contentUri.getPath(), "0");

                PictureNameDB.UpdateDataFront(filename, "0");

                break;
            case "Back":

                appLog.setLog("AddCarPictureActivity", "ถ่ายรูป Car Back " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarBack);

                PicturePathDB.UpdateDataBack(contentUri.getPath(), "0");

                PictureNameDB.UpdateDataBack(filename, "0");

                break;
            case "Left":

                appLog.setLog("AddCarPictureActivity", "ถ่ายรูป Car Left " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarLeft);

                PicturePathDB.UpdateDataLeft(contentUri.getPath(), "0");

                PictureNameDB.UpdateDataLeft(filename, "0");

                break;
            case "Right":

                appLog.setLog("AddCarPictureActivity", "ถ่ายรูป Car Right " + filename, USER_ID);

                Glide.with(this)
                        .load(new File(contentUri.getPath()))
                        .into(imgCarRight);

                PicturePathDB.UpdateDataRight(contentUri.getPath(), "0");

                PictureNameDB.UpdateDataRight(filename, "0");

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE: {
                try {
                    photoUri = data.getData();
                    String selectedImagePath = getImageFilePath(photoUri, this);
                    Log.d(getClass().getName(), selectedImagePath);

                    // get file name
                    //String path = f.getAbsolutePath();
                    String filename = selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);

                    switch (photoSelect) {
                        case "Front":

                            appLog.setLog("AddCarPictureActivity", "เลือกรูปจาก Gallery Car Front " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarFront);

                            PicturePathDB.UpdateDataFront(selectedImagePath, "0");

                            PictureNameDB.UpdateDataFront(filename, "0");

                            break;
                        case "Back":

                            appLog.setLog("AddCarPictureActivity", "เลือกรูปจาก Gallery Car Back " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarBack);

                            PicturePathDB.UpdateDataBack(selectedImagePath, "0");

                            PictureNameDB.UpdateDataBack(filename, "0");

                            break;
                        case "Left":

                            appLog.setLog("AddCarPictureActivity", "เลือกรูปจาก Gallery Car Left " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarLeft);

                            PicturePathDB.UpdateDataLeft(selectedImagePath, "0");

                            PictureNameDB.UpdateDataLeft(filename, "0");

                            break;
                        case "Right":

                            appLog.setLog("AddCarPictureActivity", "เลือกรูปจาก Gallery Car Right " + filename, USER_ID);

                            Glide.with(this)
                                    .load(new File(selectedImagePath))
                                    .into(imgCarRight);

                            PicturePathDB.UpdateDataRight(selectedImagePath, "0");

                            PictureNameDB.UpdateDataRight(filename, "0");

                            break;
                    }

                } catch (Exception e) {

                }
                break;

            }   // Gallery

        } // switch
    }

    // Gallery
    private void startGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public static String getImageFilePath(Uri originalUri, Activity activity) {
        // get file path in string
        String selectedImagePath = null;
        String[] projection = { MediaStore.Images.ImageColumns.DATA };
        Cursor cursor = activity.managedQuery(originalUri, projection, null,
                null, null);
        if (cursor != null) {
            int index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            selectedImagePath = cursor.getString(index);
            if (selectedImagePath == null) {

                String id = originalUri.getLastPathSegment().split(":")[1];
                final String[] imageColumns = { MediaStore.Images.Media.DATA };
                final String imageOrderBy = null;

                Uri uri = getUri();

                Cursor imageCursor = activity.managedQuery(uri, imageColumns,
                        MediaStore.Images.Media._ID + "=" + id, null,
                        imageOrderBy);

                if (imageCursor.moveToFirst()) {
                    selectedImagePath = imageCursor.getString(imageCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                }
                Log.e("path", selectedImagePath); // use selectedImagePath
            }
        }
        return selectedImagePath;
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private static Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                imgCarFront.setEnabled(true);
                imgCarBack.setEnabled(true);
                imgCarLeft.setEnabled(true);
                imgCarRight.setEnabled(true);
            }
        }
    }
}
