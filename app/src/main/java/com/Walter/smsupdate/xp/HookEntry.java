package com.Walter.smsupdate.xp;


import com.Walter.smsupdate.xp.hook.BaseHook;
import com.Walter.smsupdate.xp.hook.code.SmsHandlerHook;
import com.Walter.smsupdate.xp.hook.me.ModuleUtilsHook;
import com.Walter.smsupdate.xp.hook.permission.PermissionGranterHook;
import java.util.ArrayList;
import java.util.List;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private List<BaseHook> mHookList;

    {
        mHookList = new ArrayList<>();
        mHookList.add(new SmsHandlerHook()); // InBoundsSmsHandler Hook
        mHookList.add(new ModuleUtilsHook()); // ModuleUtils Hook
        mHookList.add(new PermissionGranterHook()); // PackageManagerService Hook
    }
    @Override
    public void initZygote(StartupParam startupParam) {
        for (BaseHook hook : mHookList) {
            if (hook.hookInitZygote()) {
                hook.initZygote(startupParam);
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        for (BaseHook hook : mHookList) {
            if (hook.hookOnLoadPackage()) {
                hook.onLoadPackage(lpparam);
            }
        }
    }


}
