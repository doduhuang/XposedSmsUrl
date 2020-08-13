package com.Walter.smsupdate;

import android.Manifest;
import android.os.Build;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PermConst {

    public final static Map<String, List<String>> PACKAGE_PERMISSIONS;

    static {
        PACKAGE_PERMISSIONS = new HashMap<>();

        List<String> smsCodePermissions = new ArrayList<>();

        smsCodePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        smsCodePermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        String smsCodePackage = BuildConfig.APPLICATION_ID;
        PACKAGE_PERMISSIONS.put(smsCodePackage, smsCodePermissions);

        List<String> phonePermissions = new ArrayList<>();

        phonePermissions.add("android.permission.INJECT_EVENTS");

        phonePermissions.add(Manifest.permission.KILL_BACKGROUND_PROCESSES);

        phonePermissions.add(Manifest.permission.READ_SMS);

        phonePermissions.add("android.permission.WRITE_SMS");

        if (Build.VERSION.SDK_INT >= 28) {
            phonePermissions.add("android.permission.MANAGE_APP_OPS_MODES");
        } else {
            phonePermissions.add("android.permission.UPDATE_APP_OPS_STATS");
        }

        PACKAGE_PERMISSIONS.put("com.android.phone", phonePermissions);
    }

}
