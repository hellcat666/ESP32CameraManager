package com.catsoft.esp32camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.catsoft.esp32camera.ov2640.OV2640Camera;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static com.catsoft.esp32camera.ESP32Camera.EXIT;
import static com.catsoft.esp32camera.ESP32Camera.ITEM;
import static com.catsoft.esp32camera.ESP32Camera.ITEM_SELECTED;
import static com.catsoft.esp32camera.ESP32Camera.SELECTED_CAMERA_IP_ADDRESS;
import static com.catsoft.esp32camera.ESP32Camera.SELECTED_CAMERA_NAME;
import static com.catsoft.esp32camera.ov2640.OV2640Constants.CAMERA_REFRESH;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    final static String[] mIPAddresses = { "192.168.1.11", "192.168.1.12" } ;

    Context mContext;

    private ListView mListView;
    private Button mBtnRefresh;

    private OV2640CameraAdapterLight mOV2640CameraArrayAdapter;
    private ArrayList<OV2640Camera> mArrayOfCameras;
    private OV2640Camera mSelectedCamera = null;

    private boolean mCommunicationChecked;
    ViewDialogSpinningWheel mSpinningWheelDialog;

    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mContext = this;

        initToolBar();

        initCameraList();

        // Register
        registerReceiver();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    private void initToolBar() {
        if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable( new ColorDrawable( ContextCompat.getColor(this, R.color.black ) ) );
        }
        else {
            getSupportActionBar().setBackgroundDrawable( new ColorDrawable( getColor( R.color.black ) ) );
        }
    }

    public boolean OnCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.menu_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                startSettings();
                break;
            // action with ID action_settings was selected
            case R.id.menu_exit:
                Toast.makeText(this, "Exit selected", Toast.LENGTH_SHORT).show();
                exit();
                break;
            default:
                break;
        }

        return true;
    }

    private void initCameraList() {
        // Initialize ListView
        mListView = (ListView) findViewById(R.id.listView);

        // Initialize Array of Cameras ...
        mArrayOfCameras = new ArrayList<OV2640Camera>();
        mArrayOfCameras.add( new OV2640Camera( "Camera #1", mIPAddresses[0] ) );
        mArrayOfCameras.add( new OV2640Camera( "Camera #2", mIPAddresses[1] ) );

        // ... and set ListView Adapter ...
        mOV2640CameraArrayAdapter = new OV2640CameraAdapterLight( this, mArrayOfCameras );

        // ... then attach the Adapter to the ListView
        mListView.setAdapter(mOV2640CameraArrayAdapter);

        // Finally, set the Camera List Refresh Button
        mBtnRefresh = (Button) findViewById(R.id.btnRefresh);
        if (mBtnRefresh != null) {
            mBtnRefresh.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mListView.invalidateViews();
                }
            });
        }

    }

    private void registerReceiver() {
        if(intentFilter==null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction( ITEM_SELECTED );
            intentFilter.addAction( CAMERA_REFRESH );
            mContext.registerReceiver( mMessageReceiver, intentFilter );
        }
    }

    private void unregisterReceiver() {
        if(intentFilter!=null) {
            mContext.unregisterReceiver( mMessageReceiver );
            intentFilter = null;
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent == null) { return; }

            switch(intent.getAction()) {
                case ITEM_SELECTED:
                    int position = intent.getExtras().getInt(ITEM);
                    mSelectedCamera = mArrayOfCameras.get(position);
                    Log.i(TAG,  mSelectedCamera.getCameraName() + " Selected");
                    Intent anIntent = new Intent(mContext, ESP32CameraActivity.class);
                    anIntent.putExtra( SELECTED_CAMERA_NAME, mSelectedCamera.getCameraName());
                    anIntent.putExtra( SELECTED_CAMERA_IP_ADDRESS, mSelectedCamera.getIpAddress() );
                    startActivity( anIntent );
                    break;
                case CAMERA_REFRESH:
                    mListView.invalidateViews();
                    break;
                default:
                    throw new IllegalStateException( "Unexpected Action: " + intent.getAction() );
            }
        }
    };

    private void startSettings() {
        /*
        Intent anIntent = new Intent(this, AppSettings.class);
        anIntent.putExtra(AppSettings.APP_SETTINGS, mAppSettings);
        anIntent.putExtra("parent", this.getClass());
        this.startActivity(anIntent);
         */
    }

    private void exit() {
        Intent anIntent = new Intent();
        anIntent.setAction(EXIT);
        mContext.sendBroadcast(anIntent);
        finish();
    }

    private void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler( Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
