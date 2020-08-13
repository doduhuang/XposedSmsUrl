package com.Walter.smsupdate.xp.hook.me;


import android.util.Log;
import com.Walter.smsupdate.BuildConfig;
import com.Walter.smsupdate.ModuleUtils;
import com.Walter.smsupdate.xp.hook.BaseHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class ModuleUtilsHook extends BaseHook {

    private static final String SMSCODE_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final int MODULE_VERSION = BuildConfig.MODULE_VERSION;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        if (SMSCODE_PACKAGE.equals(lpparam.packageName)) {
            try {

                hookModuleUtils(lpparam);
            } catch (Throwable e) {
                Log.e("SmsUpdate","Failed to hook current Xposed module status.");
            }
        }

    }

    private void hookModuleUtils(XC_LoadPackage.LoadPackageParam lpparam) {
        String className = ModuleUtils.class.getName();

        XposedHelpers.findAndHookMethod(className, lpparam.classLoader,
                "getModuleVersion",
                XC_MethodReplacement.returnConstant(MODULE_VERSION));
    }

}
