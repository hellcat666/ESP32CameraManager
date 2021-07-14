package com.catsoft.esp32camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.catsoft.esp32camera.ov2640.OV2640Camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.catsoft.esp32camera.ov2640.OV2640Constants.CAMERA_REFRESH;
import static com.catsoft.esp32camera.ov2640.OV2640Constants.CAMERA_STATUS;
import static com.catsoft.esp32camera.ov2640.OV2640Constants.DOCUMENT_READY;
import static com.catsoft.esp32camera.ov2640.OV2640Constants.SERVER_URL;
import static com.catsoft.esp32camera.ov2640.OV2640Constants.STATUS;

/**
 * Project: ESP32CAM
 * Package: com.catsoft.esp32cam
 * File:
 * Created by HellCat on 05.05.2021.
 */
public class ESP32CameraFragmentLight extends Fragment {


    private final static String TAG = "ESP32CameraFragmentLigh";

    private static final String HTML_BLANK = "about:blank";
    private static final String HTML_CAMERA_FILE = "file:///android_asset/HTMLVideoCamera.html";
    private static final String HTML_NO_CAMERA_FILE = "file:///android_asset/HTMLNoCamera.html";

    private static View mView;
    static private Context mContext;

    OV2640Camera mCamera = null;

    private ESP32CameraStatusBar mCameraStatusBar;
    private WebView mCameraWebView;
    private WebSettings mCameraWebSettings;
    private ESP32CameraSettingsDialog mSettingsDialog;

    private String mCameraStreamUrl = "";
    public String getCameraStreamUrl() { return mCameraStreamUrl; }
    public void setCameraStreamUrl(String cameraStreamUrl) { this.mCameraStreamUrl = cameraStreamUrl; }
    private boolean mCameraOnline = false;

    static private Context context;
    private IntentFilter intentFilter = null;
    boolean mInitialized = false;
    boolean mRefreshed = false;

    public interface OnESP32CameraFragmentInterface {
        void onESP32CameraFragmentComplete();
    }

    OnESP32CameraFragmentInterface mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        mContext = this.getContext();
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        mContext = container.getContext();
        mView = inflater.inflate(R.layout.frag_esp32_camera_light, container, false);
        mView.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mSettingsDialog!=null) {
                    mSettingsDialog.show();
                }
                else {
                    mSettingsDialog = new ESP32CameraSettingsDialog(getActivity());
                    mSettingsDialog.show();
                }
                return false;
            }
        } );

        mCameraStatusBar = new ESP32CameraStatusBar( mView );
        mCameraWebView = (WebView) mView.findViewById(R.id.cameraWebView);
        mCameraWebSettings = mCameraWebView.getSettings();
        mCameraWebSettings.setJavaScriptEnabled( true );
        mCameraWebView.addJavascriptInterface( new WebAppInterface( mContext ), "Android" );
        mCameraWebSettings.setUseWideViewPort( true );
        mCameraWebView.setBackgroundColor( 0x00000000 );
        mCameraWebView.setLayerType( WebView.LAYER_TYPE_SOFTWARE, null );
        mCameraWebView.clearCache( true );

        mCameraStatusBar = new ESP32CameraStatusBar( mView );

        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);

        mCameraOnline = false;

        mListener.onESP32CameraFragmentComplete();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach()");
        super.onAttach(context);
        mContext = context;

        try {
            mListener = (OnESP32CameraFragmentInterface)mContext;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnESP32CameraFragmentInterface");
        }
    }

    private void registerReceiver() {
        Log.i(TAG, "registerReceiver()");
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction(DOCUMENT_READY);
            intentFilter.addAction( CAMERA_STATUS );
            intentFilter.addAction(CAMERA_REFRESH);
            mContext.registerReceiver(messageReceiver, intentFilter);
        }
    }

    private void unregisterReceiver() {
        Log.i(TAG, "unregisterReceiver()");
        if(intentFilter!=null) {
            mContext.unregisterReceiver( messageReceiver );
            intentFilter = null;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        mInitialized = false;
        unregisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        mCameraStatusBar.unregisterReceiver();
        unregisterReceiver();
        mInitialized = false;
    }

    private void setCamera(boolean status) {
        mCameraOnline = status;
        try { Thread.sleep( 100 ); }
        catch(InterruptedException iex) { iex.printStackTrace();}
        if(mCameraOnline) {
            mRefreshed = false;
            mCameraWebView.loadUrl( HTML_CAMERA_FILE+"?url="+mCameraStreamUrl );
        }
        else {
            mCameraWebView.loadUrl( HTML_NO_CAMERA_FILE );
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent==null) return;

            switch (intent.getAction()) {
                case DOCUMENT_READY:
                    Log.i( TAG, "DOCUMENT_READY Received ..." );
                    break;
                case CAMERA_STATUS:
                    if(!mInitialized) {
                        mCameraStreamUrl = intent.getExtras().getString( SERVER_URL ) + "/mjpeg/1";
                        boolean status = intent.getExtras().getBoolean( STATUS );
                        setCamera(status);
                    }
                    break;
                case CAMERA_REFRESH:
                    Log.i( TAG, "CAMERA_REFRESH Received ..." );
                    mCameraStreamUrl = intent.getExtras().getString( SERVER_URL ) + "/mjpeg/1";
                    boolean status = intent.getExtras().getBoolean( STATUS );
                    if( (mCameraOnline != status) || (mRefreshed==false)) { setCamera( status ); }
                    break;
            }
        }
    };

    /**
     * The WebInterface allows the HTML Document of the WebView
     * to communicate with Android native
     */
    public class WebAppInterface {
        Context context;

        /** Instantiate the interface and set the context
         * @param ctx
         **/
        WebAppInterface(Context ctx) {
            context = ctx;
        }

        /** Sent when the HTML Document is ready */
        @JavascriptInterface
        public void onDocumentReady() {
            Log.i(TAG, "WebAppInterface.onDocumentReady()");
            Intent anIntent = new Intent();
            anIntent.setAction(DOCUMENT_READY);
            context.sendBroadcast(anIntent);
        }
        /** Sent during HTML Body onLoad(), returning the current Server Url stored in localStorage */
        @JavascriptInterface
        public void onLoad() {
            Log.i(TAG, "WebAppInterface.onLoad(...)");
        }
    }


}
