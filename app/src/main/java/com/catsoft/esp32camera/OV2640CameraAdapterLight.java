package com.catsoft.esp32camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.catsoft.esp32camera.ov2640.OV2640Camera;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import static com.catsoft.esp32camera.ESP32Camera.ITEM;
import static com.catsoft.esp32camera.ESP32Camera.ITEM_SELECTED;
import static com.catsoft.esp32camera.ov2640.OV2640Constants.CAMERA_REFRESH;

/**
 * Project: AndroidDialogTest
 * Package: com.catsoft.androiddialogtest
 * File:
 * Created by HellCat on 25.06.2021.
 */
public class OV2640CameraAdapterLight extends ArrayAdapter<OV2640Camera> {

    private static final String TAG = "OV2640CameraAdapter";

    // Web Pages
    private static final String HTML_CAMERA_FILE = "file:///android_asset/HTMLVideoCamera.html";
    private static final String HTML_NO_CAMERA_FILE = "file:///android_asset/HTMLNoCamera.html";

    Context mContext;

    IntentFilter intentFilter;

    OV2640Camera mCamera;
    String mCameraStreamUrl;
    Boolean done = false;


//    WebView wvCameraStream;
//    WebSettings wsWebSettings;
    TextView txtCameraName;
    TextView txtCameraIPAddress;
    TextView txtCameraStatus;

    public OV2640CameraAdapterLight(Context context, ArrayList<OV2640Camera> ov2640Cameras) {
        super(context, 0, ov2640Cameras);
        mContext = context;
        registerReceiver();
    }

    private void registerReceiver() {
        if(intentFilter==null) {
            intentFilter = new IntentFilter();
//            intentFilter.addAction( DOCUMENT_READY );
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

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView(" + String.valueOf( position ) +")");

        // Get the data item for this position
        mCamera = getItem( position );
        if(mCamera!=null) mCameraStreamUrl = mCamera.getCameraUrl() + "/mjpeg/1";

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from( getContext() ).inflate( R.layout.item_ov2640_camera_light, parent, false );
        }

        // Lookup view for data population
        txtCameraName = (TextView) convertView.findViewById( R.id.tvCameraName );
        txtCameraIPAddress = (TextView) convertView.findViewById( R.id.tvCameraIPAddress );
        txtCameraStatus = (TextView) convertView.findViewById( R.id.tvCameraStatus );
        /*
        wvCameraStream = (WebView) convertView.findViewById( R.id.wvCameraStream );
        wsWebSettings = wvCameraStream.getSettings();
        wsWebSettings.setJavaScriptEnabled(true);
//        wsWebSettings.setDomStorageEnabled(true);
        wvCameraStream.addJavascriptInterface( new WebAppInterface(mContext), "Android" );
//        wsWebSettings.setAllowFileAccess(true);
//        wsWebSettings.setAppCacheEnabled( true );
//        wsWebSettings.setAllowUniversalAccessFromFileURLs(true);
//        wsWebSettings.setLoadsImagesAutomatically(true);
//        wsWebSettings.setBuiltInZoomControls(true);
//        wsWebSettings.setDisplayZoomControls(false);
//        wsWebSettings.setLoadWithOverviewMode(true);
        wsWebSettings.setUseWideViewPort(true);
        wvCameraStream.setBackgroundColor( 0x00000000 );
        wvCameraStream.setLayerType( WebView.LAYER_TYPE_SOFTWARE, null );
        wvCameraStream.clearCache( true );
        */
        // Populate the data into the template view using the data object
        txtCameraName.setText( mCamera.getCameraName() );
        txtCameraIPAddress.setText( mCamera.getIpAddress() );
        txtCameraStatus.setTextColor( mCamera.isCameraOnline() ? Color.GREEN : Color.RED );
        txtCameraStatus.setText( mCamera.isCameraOnline() ? "Online" : "Offline" );
        /*
        if(mCamera.isCameraOnline()) {
            mCameraStreamUrl = mCamera.getCameraUrl() + MJPEG;
            Log.i(TAG, "LoadUrl with Camera IP " + mCameraStreamUrl);
            wvCameraStream.loadUrl( HTML_CAMERA_FILE+"?url="+mCameraStreamUrl);
        }
        else {
            wvCameraStream.loadUrl( HTML_NO_CAMERA_FILE );
        }
         */
        convertView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent anIntent = new Intent();
                anIntent.setAction(ITEM_SELECTED);
                anIntent.putExtra( ITEM, position );
                mContext.sendBroadcast( anIntent );
            }
        } );

        // Return the completed view to render on screen
        return convertView;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent == null) { return; }


            switch(intent.getAction()) {
//                case DOCUMENT_READY:
//                    break;
                case CAMERA_REFRESH:
                    break;
                default:
                    throw new IllegalStateException( "Unexpected value: " + intent.getAction() );
            }
        }
    };

    /**
     * setCameraVideoStreamUrl
     * @param srvUrl
     *
     * Set the url of the Camera, calling JavaScript function setServerUrl(...)
     */
    private void setCameraVideoStreamUrl(String srvUrl) {
        Log.i(TAG, "setCameraVideoStreamUrl(" + srvUrl + ")");
//        wvCameraStream.evaluateJavascript("javascript:setCameraStreamUrl('" + srvUrl + "')", null);
    }

    public class WebAppInterface {
        Context context;

        /**
         * Instantiate the interface and set the context
         *
         * @param ctx
         **/
        WebAppInterface(Context ctx) {
            context = ctx;
        }

        /**
         * Sent when the HTML Document is ready
         */
        @JavascriptInterface
        public void onDocumentReady() {
            Log.i( TAG, "WebAppInterface.onDocumentReady()" );
//            Intent anIntent = new Intent();
//            anIntent.setAction( DOCUMENT_READY );
//            context.sendBroadcast( anIntent );
        }

        /**
         * Sent during HTML Body onLoad(), returning the current Server Url stored in localStorage
         */
        @JavascriptInterface
        public void onLoad() {
            Log.i( TAG, "WebAppInterface.onLoad(...)" );
        }
    }
}
