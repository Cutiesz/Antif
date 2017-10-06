package com.korsolution.antif;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pixplicity.easyprefs.library.Prefs;


/**
 * A simple {@link Fragment} subclass.
 */
public class VehiclePhoto3Fragment extends Fragment {


    private ImageView imgVehicle;

    public VehiclePhoto3Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vehicle_photo3, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgVehicle = (ImageView) view.findViewById(R.id.imgVehicle);

        String _imgVehicle = Prefs.getString("CAR_IMAGE_LEFT","");
        Glide.with(this)
                .load(_imgVehicle)
                .error(R.drawable.blank_img)
                .into(imgVehicle);
    }

}
