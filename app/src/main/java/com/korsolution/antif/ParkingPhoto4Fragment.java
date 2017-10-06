package com.korsolution.antif;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class ParkingPhoto4Fragment extends Fragment {

    public ImageView imgParking;
    private ImageView imgDelImg;

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private String USER_ID;


    public ParkingPhoto4Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(USER_ID == null && getArguments() != null) {
            String userID = getArguments().getString("USER_ID");
            this.USER_ID = userID;
        }

        return inflater.inflate(R.layout.fragment_parking_photo4, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        imgParking = (ImageView) view.findViewById(R.id.imgParking);
        imgDelImg = (ImageView) view.findViewById(R.id.imgDelImg);

    }

    @Override
    public void onStart() {
        super.onStart();

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            final File[] files = storageDir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (i == 3) {
                        Glide.with(this)
                                .load(files[i])
                                .error(R.drawable.image_car)
                                .into(imgParking);

                        imgDelImg.setVisibility(View.VISIBLE);
                        imgDelImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //files[0].delete();
                                dialogAlertDeleteAllImage();
                            }
                        });
                    }
                }
            }
        }
    }

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private void deleteImage() {

        File storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
        if (storageDir.exists()) {
            final File[] files = storageDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (i == 3) {
                    files[i].delete();

                    Intent intent = new Intent(getActivity(), ParkingPhotoNewActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    startActivity(intent);
                }
            }
        }
    }

    public void dialogAlertDeleteAllImage(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(getActivity());

        mDialog.setTitle("Are you sure to delete this image?");
        mDialog.setIcon(R.drawable.ic_action_close);
        mDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //appLog.setLog("ParkingPhotoActivity", "กดปุ่ม Clear all image Parking", USER_ID);

                deleteImage();


            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //Toast.makeText(getBaseContext(), "Fail", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        mDialog.show();
    }

}
