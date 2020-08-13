package com.Walter.smsupdate;


public class ModuleUtils {

    private ModuleUtils() {
    }


    public static int getModuleVersion() {
        return -1;
    }

    public static boolean isModuleEnabled() {
        return getModuleVersion() > 0;
    }
}
