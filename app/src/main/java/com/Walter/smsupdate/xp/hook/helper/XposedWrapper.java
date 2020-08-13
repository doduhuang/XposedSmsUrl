package com.Walter.smsupdate.xp.hook.helper;



import java.lang.reflect.Member;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


public class XposedWrapper {

    private XposedWrapper() {
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            return XposedHelpers.findClass(className, classLoader);
        } catch (Throwable t) {

            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {

            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {

            return null;
        }
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        try {
            return XposedBridge.hookMethod(hookMethod, callback);
        } catch (Throwable t) {

            return null;
        }
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        try {
            return XposedBridge.hookAllConstructors(hookClass, callback);
        } catch (Throwable t) {

            return null;
        }
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        try {
            return XposedBridge.hookAllMethods(hookClass, methodName, callback);
        } catch (Throwable t) {

            return null;
        }
    }

}
