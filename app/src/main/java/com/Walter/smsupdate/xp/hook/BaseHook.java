package com.Walter.smsupdate.xp.hook;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BaseHook implements IHook {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
    }

    public boolean hookInitZygote() {
        return false;
    }

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

    }

    public boolean hookOnLoadPackage() {
        return true;
    }


}
