package com.korsolution.antif;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anton46.stepsview.StepsView;

public class AddCarNumberActivity extends AppCompatActivity {

    private EditText edtEmergencyNumber1;
    private EditText edtEmergencyNumber2;
    private EditText edtEmergencyNumber3;
    private Button btnNext;
    private TextView txtAlert;

    private String IMEI;
    private String URI_CAR_FRONT;
    private String URI_CAR_BACK;
    private String URI_CAR_LEFT;
    private String URI_CAR_RIGHT;

    private StepsView mStepsView;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car_number);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        USER_ID = getIntent().getStringExtra("USER_ID");
        IMEI = getIntent().getStringExtra("IMEI");
        URI_CAR_FRONT = getIntent().getStringExtra("URI_CAR_FRONT");
        URI_CAR_BACK = getIntent().getStringExtra("URI_CAR_BACK");
        URI_CAR_LEFT = getIntent().getStringExtra("URI_CAR_LEFT");
        URI_CAR_RIGHT = getIntent().getStringExtra("URI_CAR_RIGHT");

        appLog = new AppLogClass(this);

        setupWidgets();

        // steps view
        String[] labels = {"Step 1", "Step 2", "Step 3", "Step 4"};
        mStepsView = (StepsView) findViewById(R.id.stepsView);
        mStepsView.setCompletedPosition(labels.length - 2)
                .setLabels(labels)
                .setBarColorIndicator(this.getResources().getColor(R.color.material_blue_grey_800))
                .setProgressColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .setLabelColorIndicator(this.getResources().getColor(R.color.colorAccent))
                .drawView();
    }

    private void setupWidgets() {

        edtEmergencyNumber1 = (EditText) findViewById(R.id.edtEmergencyNumber1);
        edtEmergencyNumber2 = (EditText) findViewById(R.id.edtEmergencyNumber2);
        edtEmergencyNumber3 = (EditText) findViewById(R.id.edtEmergencyNumber3);
        btnNext = (Button) findViewById(R.id.btnNext);
        txtAlert = (TextView) findViewById(R.id.txtAlert);

        edtEmergencyNumber1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (edtEmergencyNumber1.length() == 10) {
                    edtEmergencyNumber2.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtEmergencyNumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (edtEmergencyNumber2.length() == 10) {
                    edtEmergencyNumber3.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String _emergencyNumber1 = edtEmergencyNumber1.getText().toString();
                String _emergencyNumber2 = edtEmergencyNumber2.getText().toString();
                String _emergencyNumber3 = edtEmergencyNumber3.getText().toString();

                if (_emergencyNumber1.length() == 10) {

                    appLog.setLog("AddCarNumberActivity", "กดปุ่ม Next", USER_ID);

                    Intent intent = new Intent(getApplicationContext(), AddCarDetailActivity.class);
                    intent.putExtra("USER_ID", USER_ID);
                    intent.putExtra("IMEI", IMEI);
                    intent.putExtra("URI_CAR_FRONT", URI_CAR_FRONT);
                    intent.putExtra("URI_CAR_BACK", URI_CAR_BACK);
                    intent.putExtra("URI_CAR_LEFT", URI_CAR_LEFT);
                    intent.putExtra("URI_CAR_RIGHT", URI_CAR_RIGHT);
                    intent.putExtra("EMERGENCY_NUMBER_1", _emergencyNumber1);
                    intent.putExtra("EMERGENCY_NUMBER_2", _emergencyNumber2);
                    intent.putExtra("EMERGENCY_NUMBER_3", _emergencyNumber3);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(), "คุณต้องกำหนดหมายเลขฉุกเฉินอย่างน้อย 1 เบอร์!!", Toast.LENGTH_LONG).show();

                    txtAlert.setVisibility(View.VISIBLE);
                    txtAlert.setText("กรุณากำหนดหมายเลขฉุกเฉินอย่างน้อย 1 เบอร์!!");
                }
            }
        });

    }
}
