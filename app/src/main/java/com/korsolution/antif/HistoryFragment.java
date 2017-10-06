package com.korsolution.antif;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private String USER_ID;

    private AppLogClass appLog;


    public HistoryFragment() {
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

        return inflater.inflate(R.layout.fragment_history, container, false);
    }

}
