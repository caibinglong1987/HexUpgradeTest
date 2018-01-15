package com.hexing.upgrade;

import android.app.Application;
import android.os.Environment;


import java.io.File;


/**
 * Created by caibinglong
 * on 2017/12/20.
 */

public class App extends Application {
    private static String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String FILEPATH_ROOT = SDCARD_ROOT + File.separator + "Hex-Upgrade";
    public static final String FILEPATH_LOG = FILEPATH_ROOT + File.separator + "log";
    public static final String FILEPATH_UPGRADE_LOG = FILEPATH_ROOT + File.separator + "UpgradeLog";

    @Override
    public void onCreate() {
        super.onCreate();

        File dirFile = new File(FILEPATH_ROOT);

        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        dirFile = new File(FILEPATH_LOG);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        dirFile = new File(FILEPATH_UPGRADE_LOG);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

}
