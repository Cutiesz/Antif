package com.korsolution.antif;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class VehicleDetailActivity extends AppCompatActivity {

    private String vehicleName;

    private VehicleDBClass VehicleDB;

    private ImageView imgProfile;
    private TextView txtName;
    private TextView txtPlace;
    private TextView txtDate;
    private TextView txtVehicleSpeed;
    private TextView txtStatus;
    private RelativeLayout layoutAuthen;
    private RelativeLayout layoutCutEngine;
    //private RelativeLayout layoutRefresh;
    private RelativeLayout layoutSetNumber1;
    private RelativeLayout layoutSetNumber2;
    private RelativeLayout layoutSetNumber3;

    protected ArrayList<JSONObject> feedDataList;
    protected ArrayList<JSONObject> feedDataListAuthen;
    protected ArrayList<JSONObject> feedDataListCutEngine;
    protected ArrayList<JSONObject> feedDataListChengeNumber;

    String IMEI;
    String LATITUDE;
    String LONGITUDE;
    String IS_CUT_ENGINE;
    String SIM;
    String TEL_EMERGING_1;
    String TEL_EMERGING_2;
    String TEL_EMERGING_3;

    private String USER_ID;

    private AppLogClass appLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        USER_ID = getIntent().getStringExtra("USER_ID");
        vehicleName = getIntent().getStringExtra("vehicleName");
        //Toast.makeText(getApplicationContext(), vehicleName, Toast.LENGTH_LONG).show();

        VehicleDB = new VehicleDBClass(this);

        appLog = new AppLogClass(this);

        setupWidgets();

        if (isOnline()) {
            loadData();
        } else {
            //Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
            dialogAlertLoadData();
        }

        Glide.with(this)
                .load(R.drawable.image_car)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(imgProfile);
    }

    private void loadData() {
        new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_VEHICLE_TEST", "LYd162fYt", vehicleName);
    }

    public void dialogAlertLoadData(){
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("No Internet signal, Please try agian!!");
        completeDialog.setIcon(R.drawable.ic_action_error);
        completeDialog.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //Toast.makeText(getBaseContext(), "No data! Please syn data before.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), NaviDrawerActivity.class);
                //Intent intent = new Intent(getApplicationContext(), MainAntifActivity.class);
                //Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
                intent.putExtra("USER_ID", USER_ID);
                startActivity(intent);

            }
        });
        completeDialog.show();
    }

    private void setupWidgets() {

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        txtName = (TextView) findViewById(R.id.txtName);
        txtPlace = (TextView) findViewById(R.id.txtPlace);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtVehicleSpeed = (TextView) findViewById(R.id.txtVehicleSpeed);
        txtStatus = (TextView) findViewById(R.id.txtStatus);

        layoutAuthen = (RelativeLayout) findViewById(R.id.layoutAuthen);
        layoutCutEngine = (RelativeLayout) findViewById(R.id.layoutCutEngine);
        //layoutRefresh = (RelativeLayout) findViewById(R.id.layoutRefresh);
        layoutSetNumber1 = (RelativeLayout) findViewById(R.id.layoutSetNumber1);
        layoutSetNumber2 = (RelativeLayout) findViewById(R.id.layoutSetNumber2);
        layoutSetNumber3 = (RelativeLayout) findViewById(R.id.layoutSetNumber3);

        layoutAuthen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appLog.setLog("VehicleDetailActivity", "กดปุ่ม Authen", USER_ID);

                dialogAlertAuthen();

            }
        });

        layoutCutEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);

                appLog.setLog("VehicleDetailActivity", "กดปุ่ม Cut Engine", USER_ID);

                if (IS_CUT_ENGINE.equals("NULL")) {
                    dialogAlertDeviceNotAvailable();
                } else {
                    //dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);

                    int randomPIN = (int)(Math.random()*9000)+1000;
                    dialogAlertRandomNumber(String.valueOf(randomPIN), "cut_engine");
                }
            }
        });

        /*layoutRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline()) {
                    new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/TEST_GET_VEHICLE_A", "LYd162fYt", vehicleName);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });*/

        layoutSetNumber1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appLog.setLog("VehicleDetailActivity", "กดปุ่ม Set Number 1", USER_ID);

                dialogAlertEmergencyNumbers(SIM, TEL_EMERGING_1, "1");
            }
        });

        layoutSetNumber2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appLog.setLog("VehicleDetailActivity", "กดปุ่ม Set Number 2", USER_ID);

                dialogAlertEmergencyNumbers(SIM, TEL_EMERGING_2, "2");
            }
        });

        layoutSetNumber3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appLog.setLog("VehicleDetailActivity", "กดปุ่ม Set Number 3", USER_ID);

                dialogAlertEmergencyNumbers(SIM, TEL_EMERGING_3, "3");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, NaviDrawerActivity.class);
                //Intent homeIntent = new Intent(this, MainAntifActivity.class);
                startActivity(homeIntent);
                overridePendingTransition(R.anim.slide_left_to_right, R.anim.no_change);
        }
        return super.onOptionsItemSelected(item);
    }

    public void dialogAlertRandomNumber(final String _randomNumber, final String _goPage) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(VehicleDetailActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_random_number, (ViewGroup) findViewById(R.id.layout_dialog));

        final TextView txtRandomNumber = (TextView) Viewlayout.findViewById(R.id.txtRandomNumber);
        final EditText edtRandomNumber = (EditText) Viewlayout.findViewById(R.id.edtRandomNumber);

        txtRandomNumber.setText(_randomNumber);

        CheckDialog.setTitle("Please enter the number to match the above number.");
        CheckDialog.setIcon(R.drawable.ic_action_edit);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String randomnumber = edtRandomNumber.getText().toString();

                appLog.setLog("NaviDrawerActivity", "Random number (" + _randomNumber + "/" + randomnumber + ")", USER_ID);

                if (randomnumber.equals(_randomNumber)) {
                    if (_goPage.equals("cut_engine")) {

                        dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);

                    }/* else if (_goPage.equals("share")) {



                    }*/
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter the number to match the above number!!", Toast.LENGTH_LONG).show();
                }


            }
        })
                // Button Cancel
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });
        CheckDialog.create();
        CheckDialog.show();
    }

    public void dialogAlertDeviceNotAvailable() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("อุปกรณ์ไม่พร้อมใช้งาน!!");
        completeDialog.setMessage("กรุณาติดต่อเจ้าหน้าที่เพื่อแก้ไข");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("VehicleDetailActivity", "อุปกรณ์ไม่พร้อมใช้งาน", USER_ID);

                dialog.dismiss();

            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthen() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("คุณต้องการ authen ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    appLog.setLog("VehicleDetailActivity", "Authen " + IMEI, USER_ID);

                    new FeedAsynTaskAuthen().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_AUTHEN", "LYd162fYt", IMEI);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineBySMS(final String _vehicleName, final String _sim, final String _smsCutEngine, String _isCut) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        String statusEngine = null;
        if (_isCut.equals("0")) {
            statusEngine = "ต่อ";
        } else if (_isCut.equals("1")) {
            statusEngine = "ตัด";
        }

        completeDialog.setTitle("คุณต้องการ" + statusEngine + "สตาร์ท(" + _vehicleName + ")ด้วย SMS ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        final String finalStatusEngine = statusEngine;

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                appLog.setLog("VehicleDetailActivity", finalStatusEngine + "สตาร์ท " + _vehicleName +  " by sms", USER_ID);

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(_sim, null, _smsCutEngine, null, null);

                // Refresh List
                if (isOnline()) {
                    new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/GET_VEHICLE_TEST", "LYd162fYt", vehicleName);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineByInternet(final String _vehicleName, final String _isCut) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        String statusEngine = null;
        if (_isCut.equals("0")) {
            statusEngine = "ต่อ";
        } else if (_isCut.equals("1")) {
            statusEngine = "ตัด";
        }

        completeDialog.setTitle("คุณต้องการ" + statusEngine + "สตาร์ท(" + _vehicleName + ")ด้วย internet ใช่หรือไม่?");
        //completeDialog.setMessage("");
        completeDialog.setIcon(R.drawable.ic_action_error);

        final String finalStatusEngine = statusEngine;
        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    appLog.setLog("VehicleDetailActivity", finalStatusEngine + "สตาร์ท " + _vehicleName +  " by Internet", USER_ID);

                    new FeedAsynTaskCutEngine().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_CUTENGIN", "LYd162fYt", IMEI, _isCut);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertEmergencyNumbers(final String _SIM, final String _TEL_EMERGING, final String emerNo) {
        AlertDialog.Builder CheckDialog = new AlertDialog.Builder(VehicleDetailActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View Viewlayout = inflater.inflate(R.layout.dialog_emergency_tel_change, (ViewGroup) findViewById(R.id.layout_dialog));

        final EditText txtEmergencyNumbers = (EditText) Viewlayout.findViewById(R.id.txtEmergencyNumbers);


        CheckDialog.setTitle("เปลี่ยนเบอร์ฉุกเฉิน(" + _TEL_EMERGING + ")");
        CheckDialog.setIcon(R.drawable.ic_action_error);
        CheckDialog.setView(Viewlayout);

        // Button OK
        CheckDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String emergencyNumbers = txtEmergencyNumbers.getText().toString();
                //Toast.makeText(getApplicationContext(), emergencyNumbers, Toast.LENGTH_LONG).show();
                //dialogAlertSendSMSEmergencyTel(_SIM, _TEL_EMERGING, emergencyNumbers, emerNo);

                if (isOnline()) {
                    appLog.setLog("VehicleDetailActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _TEL_EMERGING + ") เป็น " + emergencyNumbers, USER_ID);

                    new FeedAsynTaskChangeNumber().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_TEL_EMERGING", "LYd162fYt", IMEI, emergencyNumbers, emerNo, USER_ID);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        })
                // Button Cancel
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });
        CheckDialog.create();
        CheckDialog.show();
    }

    public void dialogAlertChangeNumberSuccess(final String _emergencyNumber, final String _number) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("คุณได้เปลี่ยนเบอร์ฉุกเฉินที่" + _number + " เป็นเบอร์ " + _emergencyNumber);
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertChangeNumberFail(String _imei, final String _emergencyNumber, final String _number) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("เปลี่ยนเบอร์ไม่สำเร็จ!");
        completeDialog.setMessage("คุณต้องการเปลี่ยนเบอร์ที่ " + _number + "เป็นเบอร์ (" + _emergencyNumber + ") อีกครั้งหรือไม่");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    new FeedAsynTaskChangeNumber().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_TEL_EMERGING", "LYd162fYt", IMEI, _emergencyNumber, _number, "2");
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineSuccess(String _statusEngine) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        String status;
        // 1 = ตัด , 0 = ต่อ
        if (_statusEngine.equals("1")) {
            status = "ตัด";
        } else {
            status = "ต่อ";
        }

        completeDialog.setTitle(status + "สตาร์ท สำเร็จ");
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertCutEngineFail(String _statusEngine) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        String status;
        // 1 = ตัด , 0 = ต่อ
        if (_statusEngine.equals("1")) {
            status = "ตัด";
        } else {
            status = "ต่อ";
        }

        completeDialog.setTitle(status + "สตาร์ท ไม่สำเร็จ!");
        completeDialog.setMessage("คุณต้องการ " + status + "สตาร์ท อีกครั้งหรือไม่");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                dialogAlertSendSMSCutEngine(vehicleName, SIM, IS_CUT_ENGINE);
            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertSendSMSCutEngine(final String _VEHICLE_NAME, final String _SIM, final String _IS_CUT_ENGINE) {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        String strCutEngineTitle = null;
        String smsCutEngine = null;
        String statusEngine = null;

        //if (_IS_CUT_ENGINE.equals("true")) {
        if (_IS_CUT_ENGINE.equals("1")) {
            strCutEngineTitle = "ต่อ";
            smsCutEngine = "at$out1=0";
            statusEngine = "0";
            //} else if (_IS_CUT_ENGINE.equals("false")) {
        } else if (_IS_CUT_ENGINE.equals("0")) {
            strCutEngineTitle = "ตัด";
            smsCutEngine = "at$out1=1";
            statusEngine = "1";
        }

        completeDialog.setTitle("ส่ง " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME);
        completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_error);

        final String finalSmsCutEngine = smsCutEngine;
        final String finalStatusEngine = statusEngine;

        completeDialog.setPositiveButton(/*android.R.string.yes*/strCutEngineTitle + "สตาร์ทด้วย SMS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //SmsManager smsManager = SmsManager.getDefault();
                //smsManager.sendTextMessage(_SIM, null, finalSmsCutEngine, null, null);

                // Refresh List
                /*if (isOnline()) {
                    new FeedAsynTask().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/TEST_GET_VEHICLE_A", "LYd162fYt", vehicleName);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }*/

                dialogAlertCutEngineBySMS(_VEHICLE_NAME, _SIM, finalSmsCutEngine, finalStatusEngine);

            }
        }).setNegativeButton(/*android.R.string.no*/strCutEngineTitle + "สตาร์ทด้วย Internet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();

                // 1 = ตัด , 0 = ต่อ
                /*if (isOnline()) {
                    new FeedAsynTaskCutEngine().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/D_SET_CUTENGIN", "LYd162fYt", IMEI, finalStatusEngine);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }*/

                dialogAlertCutEngineByInternet(_VEHICLE_NAME, finalStatusEngine);
            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthenSuccess() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("Authen Success.");
        //completeDialog.setMessage("คุณต้องการ " + strCutEngineTitle + "สตาร์ท " + _VEHICLE_NAME + " ใช่หรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_accept);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public void dialogAlertAuthenFail() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("Authen Fail!");
        completeDialog.setMessage("คุณต้องการลอง authen ใหม่อีกครั้งหรือไม่?");
        completeDialog.setIcon(R.drawable.ic_action_cancel);

        completeDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if (isOnline()) {
                    new FeedAsynTaskAuthen().execute("http://202.183.192.165/ws_antit/WebServiceANTIT.asmx/SET_AUTHEN", "LYd162fYt", IMEI);
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }

            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Fail!!" ,Toast.LENGTH_LONG).show();
            }
        });
        completeDialog.show();
    }

    public void dialogAlertVehicleNotUpdate() {
        AlertDialog.Builder completeDialog = new AlertDialog.Builder(VehicleDetailActivity.this);

        completeDialog.setTitle("รถของคุณไม่อัพเดท!!");
        completeDialog.setMessage("กรุณาลองใหม่อีกครั้ง");
        completeDialog.setIcon(R.drawable.ic_action_error);

        completeDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (isOnline()) {
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet signal, Please try agian!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        completeDialog.show();
    }

    public class FeedAsynTaskChangeNumber extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        private String _IMEI;
        private String _TEL_EMERGING;
        private String _NUMBER;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleDetailActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _IMEI = params[2];
                _TEL_EMERGING = params[3];
                _NUMBER = params[4];

                // 1. connect server with okHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        //.readTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(300, TimeUnit.SECONDS)
                        .build();


                // 2. assign post data
                RequestBody postData = new FormBody.Builder()
                        //.add("username", "admin")
                        //.add("password", "password")
                        .add("CODE_API", params[1])
                        .add("IMEI", params[2])
                        .add("TEL_EMERGING", params[3])
                        .add("NUMBER", params[4])
                        .add("UPDATE_BY", params[5])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(postData)
                        .build();

                // 3. transport request to server
                okhttp3.Response response = client.newCall(request).execute();
                String result = response.body().string();

                return result;

            } catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                s = s.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                s = s.replace("<string xmlns=\"http://kontin.co.th\">", "");
                s = s.replace("</string>", "");

                feedDataListChengeNumber = CuteFeedJsonUtil.feed(s);
                if (feedDataListChengeNumber != null) {
                    for (int i = 0; i <= feedDataListChengeNumber.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListChengeNumber.get(i).getString("STATUS"));

                            if (STATUS.contains("Success")) {
                                appLog.setLog("VehicleDetailActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _NUMBER + ") เป็น " + _TEL_EMERGING + " Success", USER_ID);

                                dialogAlertChangeNumberSuccess(_TEL_EMERGING, _NUMBER);
                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("VehicleDetailActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _NUMBER + ") เป็น " + _TEL_EMERGING + " รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("VehicleDetailActivity", "เปลี่ยนเบอร์ฉุกเฉิน(" + _NUMBER + ") เป็น " + _TEL_EMERGING + " Fail", USER_ID);

                                dialogAlertChangeNumberFail(_IMEI, _TEL_EMERGING, _NUMBER);
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public class FeedAsynTaskCutEngine extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        private String statusEngine;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleDetailActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                statusEngine = params[3];

                // 1. connect server with okHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        //.readTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(300, TimeUnit.SECONDS)
                        .build();


                // 2. assign post data
                RequestBody postData = new FormBody.Builder()
                        //.add("username", "admin")
                        //.add("password", "password")
                        .add("CODE_API", params[1])
                        .add("IMEI", params[2])
                        .add("STATUS", params[3])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(postData)
                        .build();

                // 3. transport request to server
                okhttp3.Response response = client.newCall(request).execute();
                String result = response.body().string();

                return result;

            } catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                s = s.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                s = s.replace("<string xmlns=\"http://kontin.co.th\">", "");
                s = s.replace("</string>", "");

                feedDataListCutEngine = CuteFeedJsonUtil.feed(s);
                if (feedDataListCutEngine != null) {
                    for (int i = 0; i <= feedDataListCutEngine.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListCutEngine.get(i).getString("STATUS"));

                            String status = null;
                            if (statusEngine.equals("0")) {
                                status = "ต่อ";
                            } else if (statusEngine.equals("1")) {
                                status = "ตัด";
                            }

                            if (STATUS.contains("Success")) {
                                appLog.setLog("VehicleDetailActivity", status + "สตาร์ท Success", USER_ID);

                                dialogAlertCutEngineSuccess(statusEngine);
                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("VehicleDetailActivity", status + "สตาร์ท รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("VehicleDetailActivity", status + "สตาร์ท Fail", USER_ID);

                                dialogAlertCutEngineFail(statusEngine);
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public class FeedAsynTaskAuthen extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;
        String _imei;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleDetailActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                _imei = params[2];

                // 1. connect server with okHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        //.readTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(300, TimeUnit.SECONDS)
                        .build();


                // 2. assign post data
                RequestBody postData = new FormBody.Builder()
                        //.add("username", "admin")
                        //.add("password", "password")
                        .add("CODE_API", params[1])
                        .add("IMEI", params[2])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(postData)
                        .build();

                // 3. transport request to server
                okhttp3.Response response = client.newCall(request).execute();
                String result = response.body().string();

                return result;

            } catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                s = s.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                s = s.replace("<string xmlns=\"http://kontin.co.th\">", "");
                s = s.replace("</string>", "");

                feedDataListAuthen = CuteFeedJsonUtil.feed(s);
                if (feedDataListAuthen != null) {
                    for (int i = 0; i <= feedDataListAuthen.size(); i++) {
                        try {

                            String STATUS = String.valueOf(feedDataListAuthen.get(i).getString("STATUS"));

                            if (STATUS.contains("Success")) {

                                appLog.setLog("VehicleDetailActivity", "Authen " + _imei + " Success", USER_ID);

                                dialogAlertAuthenSuccess();

                                /*final ProgressDialog mDialog = new ProgressDialog(VehicleDetailActivity.this);
                                mDialog.setMessage("Loading..");
                                mDialog.setIndeterminate(false);
                                mDialog.setCancelable(false);
                                mDialog.show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        dialogAlertAuthenSuccess();
                                        mDialog.dismiss();

                                    }
                                }, 1000 * 10);*/

                            } else if (STATUS.contains("NotUp")) {
                                appLog.setLog("VehicleDetailActivity", "Authen " + _imei + " รถไม่อัพเดท", USER_ID);

                                dialogAlertVehicleNotUpdate();
                            } else {
                                appLog.setLog("VehicleDetailActivity", "Authen " + _imei + " Fail", USER_ID);

                                dialogAlertAuthenFail();
                            }


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public class FeedAsynTask extends AsyncTask<String, Void, String> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(VehicleDetailActivity.this);
            nDialog.setMessage("Loading..");
            //nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {

            try{

                // 1. connect server with okHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();


                // 2. assign post data
                RequestBody postData = new FormBody.Builder()
                        //.add("username", "admin")
                        //.add("password", "password")
                        .add("CODE_API", params[1])
                        .add("VEHICLE_ID", params[2])
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(postData)
                        .build();

                // 3. transport request to server
                okhttp3.Response response = client.newCall(request).execute();
                String result = response.body().string();

                return result;

            } catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                s = s.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                s = s.replace("<string xmlns=\"http://kontin.co.th\">", "");
                s = s.replace("</string>", "");

                feedDataList = CuteFeedJsonUtil.feed(s);
                if (feedDataList != null) {
                    for (int i = 0; i <= feedDataList.size(); i++) {
                        try {

                            String strIMEI = String.valueOf(feedDataList.get(i).getString("IMEI"));
                            String strVEHICLE_DISPLAY = String.valueOf(feedDataList.get(i).getString("VEHICLE_DISPLAY"));
                            String strVEHICLE_NAME = String.valueOf(feedDataList.get(i).getString("VEHICLE_NAME"));
                            String strHISTORY_DATETIME = String.valueOf(feedDataList.get(i).getString("HISTORY_DATETIME"));
                            String strLATITUDE = String.valueOf(feedDataList.get(i).getString("LATITUDE"));
                            String strLONGITUDE = String.valueOf(feedDataList.get(i).getString("LONGITUDE"));
                            String strPLACE = String.valueOf(feedDataList.get(i).getString("PLACE"));
                            String strANGLE = String.valueOf(feedDataList.get(i).getString("ANGLE"));
                            String strSPEED = String.valueOf(feedDataList.get(i).getString("SPEED"));
                            String strSTATUS = String.valueOf(feedDataList.get(i).getString("STATUS"));
                            String strIS_CUT_ENGINE = String.valueOf(feedDataList.get(i).getString("IS_CUT_ENGINE"));
                            String strIS_IQNITION = String.valueOf(feedDataList.get(i).getString("IS_IQNITION"));
                            String strSIM = String.valueOf(feedDataList.get(i).getString("SIM"));
                            String strTEL_EMERGING_1 = String.valueOf(feedDataList.get(i).getString("TEL_EMERGING_1"));
                            String strTEL_EMERGING_2 = String.valueOf(feedDataList.get(i).getString("TEL_EMERGING_2"));
                            String strTEL_EMERGING_3 = String.valueOf(feedDataList.get(i).getString("TEL_EMERGING_3"));
                            String strIS_UNPLUG_GPS = String.valueOf(feedDataList.get(i).getString("IS_UNPLUG_GPS"));
                            String strGSM_SIGNAL = String.valueOf(feedDataList.get(i).getString("GSM_SIGNAL"));
                            String strNUM_SAT = String.valueOf(feedDataList.get(i).getString("NUM_SAT"));

                            // Cut String Date Time
                            String[] separated = strHISTORY_DATETIME.split("-");
                            String[] day = separated[2].split("T");
                            String dateTime = day[0] + "/" + separated[1] + "/" + separated[0] + " " + day[1];

                            txtName.setText(strVEHICLE_NAME);
                            txtPlace.setText(strPLACE);
                            txtDate.setText("Time : " + dateTime);
                            txtVehicleSpeed.setText("Speed : " + strSPEED + " km/h");

                            if (strSTATUS.equals("1")) {
                                txtStatus.setTextColor(Color.parseColor("#04B404"));
                                txtStatus.setText("รถวิ่ง");
                            } else if (strSTATUS.equals("2")) {
                                txtStatus.setTextColor(Color.parseColor("#FFFF00"));
                                txtStatus.setText("จอดรถติดเครื่อง");
                            } else if (strSTATUS.equals("3")) {
                                txtStatus.setTextColor(Color.parseColor("#A4A4A4"));
                                txtStatus.setText("จอดดับเครื่อง");
                            } else if (strSTATUS.equals("4")) {
                                txtStatus.setTextColor(Color.parseColor("#4000FF"));
                                txtStatus.setText("ความเร็วเกิน");
                            } else  if (strSTATUS.equals("0")) {
                                txtStatus.setTextColor(Color.parseColor("#4000FF"));
                                txtStatus.setText("เคลื่อนที่ขณะดับเครื่อง");
                            }

                            //VEHICLE_NAME = strVEHICLE_NAME;
                            IMEI = strIMEI;
                            LATITUDE = strLATITUDE;
                            LONGITUDE = strLONGITUDE;
                            IS_CUT_ENGINE = strIS_CUT_ENGINE;
                            SIM = strSIM;
                            TEL_EMERGING_1 = strTEL_EMERGING_1;
                            TEL_EMERGING_2 = strTEL_EMERGING_2;
                            TEL_EMERGING_3 = strTEL_EMERGING_3;

                            appLog.setLog("VehicleDetailActivity", "Load Vehicle Data", USER_ID);


                        } catch (Exception e) {

                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No Data!!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
            }

            nDialog.dismiss();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
