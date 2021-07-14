package com.catsoft.esp32camera;

import android.app.Application;
import android.content.Context;

/**
 * Project: ESP32Camera
 * Package: com.catsoft.esp32camera
 * File:
 * Created by HellCat on 01.07.2021.
 */
public class ESP32Camera extends Application {


    public static final String ACTIVITY_READY = "activity-ready";
    public static final String COMMUNICATION_READY = "communication-ready";
    public static  final String SELECTED_CAMERA = "selected-camera";
    public static  final String SELECTED_CAMERA_NAME = "selected-camera-name";
    public static  final String SELECTED_CAMERA_IP_ADDRESS = "selected-camera-ip-address";

    // Constants for Message Broadcasting
    public static final String ITEM_SELECTED = "item-selected";
    public static final String ITEM = "item";

    public static final String EXIT = "exit";

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

}
