package com.Walter.smsupdate.xp.hook.permission;

import android.os.Build;

import androidx.annotation.RequiresApi;


import com.Walter.smsupdate.xp.hook.BaseSubHook;
import com.Walter.smsupdate.xp.hook.helper.MethodHookWrapper;
import com.Walter.smsupdate.xp.hook.helper.XposedWrapper;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static com.Walter.smsupdate.PermConst.PACKAGE_PERMISSIONS;


/**
 * Since Android P(API 28)<br/>
 * Hook com.android.server.pm.permission.PermissionManagerService
 */
public class PermissionManagerServiceHook extends BaseSubHook {

    // for Android 28+
    private static final String CLASS_PERMISSION_MANAGER_SERVICE = "com.android.server.pm.permission.PermissionManagerService";
    private static final String CLASS_PERMISSION_CALLBACK = "com.android.server.pm.permission.PermissionManagerInternal.PermissionCallback";
    private static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";

    // for MIUI 10 Android Q
    private static final String CLASS_PERMISSION_CALLBACK_Q = "com.android.server.pm.permission.PermissionManagerServiceInternal.PermissionCallback";

    public PermissionManagerServiceHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Override
    public void startHook() {
        try {
            hookGrantPermissions();
        } catch (Throwable ignored) {

        }
    }

    private void hookGrantPermissions() {

        Method method = findTargetMethod();
        XposedBridge.hookMethod(method, new MethodHookWrapper() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) throws Throwable {
                afterGrantPermissionsSinceP(param);
            }
        });
    }

    private Method findTargetMethod() {
        Class<?> pmsClass = XposedHelpers.findClass(CLASS_PERMISSION_MANAGER_SERVICE, mClassLoader);
        Class<?> packageClass = XposedHelpers.findClass(CLASS_PACKAGE_PARSER_PACKAGE, mClassLoader);
        Class<?> callbackClass = XposedHelpers.findClassIfExists(CLASS_PERMISSION_CALLBACK, mClassLoader);
        if (callbackClass == null) {
            // Android Q PermissionCallback 不一样
            callbackClass = XposedWrapper.findClass(CLASS_PERMISSION_CALLBACK_Q, mClassLoader);
        }

        Method method = XposedHelpers.findMethodExactIfExists(pmsClass, "grantPermissions",
                /* PackageParser.Package pkg   */ packageClass,
                /* boolean replace             */ boolean.class,
                /* String packageOfInterest    */ String.class,
                /* PermissionCallback callback */ callbackClass);

        if (method == null) { // method grantPermissions() not found
            // Android Q
            method = XposedHelpers.findMethodExactIfExists(pmsClass, "restorePermissionState",
                    /* PackageParser.Package pkg   */ packageClass,
                    /* boolean replace             */ boolean.class,
                    /* String packageOfInterest    */ String.class,
                    /* PermissionCallback callback */ callbackClass);
            if (method == null) { // method restorePermissionState() not found
                Method[] _methods = XposedHelpers.findMethodsByExactParameters(pmsClass, Void.TYPE,
                        /* PackageParser.Package pkg   */ packageClass,
                        /* boolean replace             */ boolean.class,
                        /* String packageOfInterest    */ String.class,
                        /* PermissionCallback callback */ callbackClass);
                if (_methods != null && _methods.length > 0) {
                    method = _methods[0];
                }
            }
        }
        return method;
    }

    @SuppressWarnings("unchecked")
    private void afterGrantPermissionsSinceP(XC_MethodHook.MethodHookParam param) {
        // android.content.pm.PackageParser.Package 对象
        Object pkg = param.args[0];

        final String _packageName = (String) XposedHelpers.getObjectField(pkg, "packageName");

        Set<String> packageSet = PACKAGE_PERMISSIONS.keySet();
        for (String packageName : packageSet) {
            if (packageName.equals(_packageName)) {

                // PackageParser$Package.mExtras 实际上是 com.android.server.pm.PackageSetting mExtras 对象
                final Object extras = XposedHelpers.getObjectField(pkg, "mExtras");
                // com.android.server.pm.permission.PermissionsState 对象
                final Object permissionsState = XposedHelpers.callMethod(extras, "getPermissionsState");

                // Manifest.xml 中声明的permission列表
                final List<String> requestedPermissions = (List<String>)
                        XposedHelpers.getObjectField(pkg, "requestedPermissions");

                // com.android.server.pm.permission.PermissionSettings mSettings 对象
                final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                // ArrayMap<String, com.android.server.pm.permission.BasePermission> mPermissions 对象
                final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

                List<String> permissionsToGrant = PACKAGE_PERMISSIONS.get(packageName);
                for (String permissionToGrant : permissionsToGrant) {
                    if (!requestedPermissions.contains(permissionToGrant)) {
                        boolean granted = (boolean) XposedHelpers.callMethod(
                                permissionsState, "hasInstallPermission", permissionToGrant);
                        // grant permissions
                        if (!granted) {
                            // com.android.server.pm.permission.BasePermission bpToGrant
                            final Object bpToGrant = XposedHelpers.callMethod(permissions, "get", permissionToGrant);
                            int result = (int) XposedHelpers.callMethod(permissionsState, "grantInstallPermission", bpToGrant);

                        } else {

                        }
                    }
                }
            }
        }
    }


}
