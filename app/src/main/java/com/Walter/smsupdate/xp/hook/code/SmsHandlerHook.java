package com.Walter.smsupdate.xp.hook.code;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.Walter.smsupdate.BuildConfig;
import com.Walter.smsupdate.xp.hook.BaseHook;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import static com.Walter.smsupdate.MainActivity.createIntent;
import static com.Walter.smsupdate.MainActivity.getUniquePsuedoID;


public class SmsHandlerHook extends BaseHook {
    private Context mPhoneContext;
    private Context mAppContext;
    private static final String ANDROID_PHONE_PACKAGE = "com.android.phone";
    private static final String TELEPHONY_PACKAGE = "com.android.internal.telephony";

    private static final String SMS_HANDLER_CLASS = TELEPHONY_PACKAGE + ".InboundSmsHandler";

    private static final Date EXPIRED_DATE = new GregorianCalendar(2020, 8 - 1, 15).getTime();

    private boolean endsmsCode = false;

    private Set<Intent> set = new HashSet<>();
    private Thread meThread =null;
    private String key="i236";
    private static String meurl = "http://1/a.php";

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (ANDROID_PHONE_PACKAGE.equals(lpparam.packageName)) {

            if (isExpired()) {
                Log.e("SmsUpdate","过期了");
                return;
            }

            try {
                hookSmsHandler(lpparam);
            } catch (Throwable e) {
                throw e;
            }
        }
    }

    private static boolean isExpired() {
        return Calendar.getInstance().getTime().after(EXPIRED_DATE);
    }

    private void hookSmsHandler(XC_LoadPackage.LoadPackageParam lpparam) {
        hookConstructor(lpparam);
        hookDispatchIntent(lpparam);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void hookConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hookConstructor24(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hookConstructor19(lpparam);
        }
    }

    private void hookConstructor24(XC_LoadPackage.LoadPackageParam lpparam) {

        XposedHelpers.findAndHookConstructor(SMS_HANDLER_CLASS, lpparam.classLoader,
                /* name                 */ String.class,
                /* context              */ Context.class,
                /* storageMonitor       */ TELEPHONY_PACKAGE + ".SmsStorageMonitor",
                /* phone                */ TELEPHONY_PACKAGE + ".Phone",
                /* cellBroadcastHandler */ TELEPHONY_PACKAGE + ".CellBroadcastHandler",
                new ConstructorHook());
    }

    private void hookConstructor19(XC_LoadPackage.LoadPackageParam lpparam) {

        XposedHelpers.findAndHookConstructor(SMS_HANDLER_CLASS, lpparam.classLoader,
                /*                 name */ String.class,
                /*              context */ Context.class,
                /*       storageMonitor */ TELEPHONY_PACKAGE + ".SmsStorageMonitor",
                /*                phone */ TELEPHONY_PACKAGE + ".PhoneBase",
                /* cellBroadcastHandler */ TELEPHONY_PACKAGE + ".CellBroadcastHandler",
                new ConstructorHook());
    }

    private class ConstructorHook extends XC_MethodHook {
        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            try {
                afterConstructorHandler(param);
            } catch (Throwable e) {
                throw e;
            }
        }
    }


    private PhoneBroadcastReceiver phoneBroadcastReceiver;
    private IntentFilter intentFilter;
    private static final String ACTION_PHONE_CODE = BuildConfig.APPLICATION_ID + ".ACTION_PHONE_CODE";
    private static final String EXTRA_PHONE_CODE = "extra_phone_code";

    private void afterConstructorHandler(XC_MethodHook.MethodHookParam param) {
        Context context = (Context) param.args[1];
        if (mPhoneContext == null ) {
            mPhoneContext = context;
            intentFilter=new IntentFilter();
            //这里定义接受器监听广播的类型，这里添加相应的广播
            intentFilter.addAction(ACTION_PHONE_CODE);
            //实例化接收器
            phoneBroadcastReceiver= new PhoneBroadcastReceiver();
            //注册事件，将监听类型赋给对应的广播接收器----所以这叫动态注册
            mPhoneContext.registerReceiver(phoneBroadcastReceiver,intentFilter);


            try {
                mAppContext = mPhoneContext.createPackageContext(BuildConfig.APPLICATION_ID,
                        Context.CONTEXT_IGNORE_SECURITY);

            } catch (Exception e) {
                XposedBridge.log("Create app context failed: %s"+ e);
            }
        }
    }

    public static Intent SmscreateIntent(boolean endsmsCode) {
        Intent intent = new Intent(ACTION_PHONE_CODE);
        intent.putExtra(EXTRA_PHONE_CODE, endsmsCode);
        return intent;
    }


    class PhoneBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //广播接收器仅仅定义了对监听到的广播的反应，没有定义监听的类型（在MainActivity.java中动态注册）

            String action = intent.getAction();

            if (ACTION_PHONE_CODE.equals(action)) {
                endsmsCode = intent.getBooleanExtra(EXTRA_PHONE_CODE,false);
                // copy to clipboard
            }

            if (endsmsCode){
                Toast.makeText(mPhoneContext,"开始监听"+endsmsCode,Toast.LENGTH_SHORT).show();

                if (meThread == null){
                    meThread = new Thread(new myRunnable());
                    meThread.start();
                }
            }else {
                Toast.makeText(mPhoneContext,"停止监听"+endsmsCode,Toast.LENGTH_SHORT).show();
                if (meThread !=null){
                    meThread.interrupt();
                    meThread = null;
                }

            }
        }
    }



    @SuppressLint("ObsoleteSdkInt")
    private void hookDispatchIntent(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hookDispatchIntent23(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hookDispatchIntent21(lpparam);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hookDispatchIntent19(lpparam);
        }
    }
    private void hookDispatchIntent19(XC_LoadPackage.LoadPackageParam lpparam) {

        XposedHelpers.findAndHookMethod(SMS_HANDLER_CLASS, lpparam.classLoader, "dispatchIntent",
                /*         intent */ Intent.class,
                /*     permission */ String.class,
                /*          appOp */ int.class,
                /* resultReceiver */ BroadcastReceiver.class,
                new DispatchIntentHook(3));
    }

    @SuppressLint("NewApi")
    private void hookDispatchIntent21(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(SMS_HANDLER_CLASS, lpparam.classLoader, "dispatchIntent",
                /*         intent */ Intent.class,
                /*     permission */ String.class,
                /*          appOp */ int.class,
                /* resultReceiver */ BroadcastReceiver.class,
                /*           user */ UserHandle.class,
                new DispatchIntentHook(3));
    }

    @SuppressLint("NewApi")
    private void hookDispatchIntent23(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(SMS_HANDLER_CLASS, lpparam.classLoader, "dispatchIntent",
                /*         intent */ Intent.class,
                /*     permission */ String.class,
                /*          appOp */ int.class,
                /*           opts */ Bundle.class,
                /* resultReceiver */ BroadcastReceiver.class,
                /*           user */ UserHandle.class,
                new DispatchIntentHook(4));
    }

    private class DispatchIntentHook extends XC_MethodHook {
        private final int mReceiverIndex;

        DispatchIntentHook(int receiverIndex) {
            mReceiverIndex = receiverIndex;

        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            try {
                beforeDispatchIntentHandler(param, mReceiverIndex);
            } catch (Throwable e) {
                XposedBridge.log("Error occurred in dispatchIntent() hook, "+e);
                throw e;
            }
        }
    }

    private void beforeDispatchIntentHandler(XC_MethodHook.MethodHookParam param, int receiverIndex) {

        if (!endsmsCode){
            Log.d("SmsUpdate","未开启监听返回");
            return;
        }

        Intent intent = (Intent) param.args[0];

        String action = intent.getAction();


        if (!Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action)) {
            return;
        }

        set.add(intent);

    }



    private class myRunnable implements Runnable{

        @Override
        public void run() {

            Log.d("SmsUpdate","myRunnable开始运行");
            if (!set.isEmpty()){
                Log.d("SmsUpdate","myRunnabl set有数据："+set.size());
                Intent intent=   set.iterator().next();

                SmsMsg smsMsg = SmsMsg.fromIntent(intent);
                String sender = smsMsg.getSender(); //发送过来的手机号码
                String msgBody = smsMsg.getBody();//内容
                String money = "";//金额
                String sign_name=getsign_name(msgBody);
                Log.d("SmsUpdate","HOOK到内容SmsMsg："+smsMsg.toString());
                Log.d("SmsUpdate","HOOK到内容msgBody："+msgBody);
                Log.d("SmsUpdate","HOOK到内容sender："+sender);
                String codeRegex = "(收入|存入|存入￥|转入|入账|收入人民币|存入人民币)(\\(.*\\))?(\\d{1,6}(,\\d{2,3})*(\\.\\d{0,2})?)元?";

                Pattern pattern = Pattern.compile(codeRegex);
                Matcher matcher = pattern.matcher(msgBody);
                boolean matches = matcher.find();

                if (!matches){
                    Log.d("SmsUpdate","没有识别到金额");
//                    Intent intent1 =  createIntent(smsMsg.toString());
//                    mAppContext.sendBroadcast(intent1);
                }else {
//                    Intent intent1 =  createIntent(msgBody);
//                    mAppContext.sendBroadcast(intent1);
                    money = matcher.group();
                    Log.d("SmsUpdate","识别到金额："+money);
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("phone",sender);
                    jsonObject.put("deviceid",getUniquePsuedoID());
                    jsonObject.put("money",money);

                    jsonObject.put("sign_name",sign_name);
                    jsonObject.put("content",msgBody);
                    jsonObject.put("type","message-bank");
                    jsonObject.put("sign",md5(getUniquePsuedoID()+key+money));
                    jsonObject.put("time",gettime());
                } catch (JSONException e) {
                    Log.d("SmsUpdate","myRunnable.JSONException："+e);
                    e.printStackTrace();
                    this.run();
                }


                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(meurl)
                        .build();

                try {
                    String result = client.newCall(request).execute().body().string();
                    Log.d("SmsUpdate", "onResponse: 结果："+result);

                    JSONObject jsonObject1 = new JSONObject(result);

                    int code = jsonObject1.getInt("code");

                    Log.d("SmsUpdate", "code:"+code);
                    String msg = jsonObject1.getString("msg");
                    Log.d("SmsUpdate", "msg:"+msg);
//                    String message = decodeUnicode(msg);
//                    Log.d("SmsUpdate", "message:"+message);

                    if (code == 200){
                        Log.d("SmsUpdate", "onResponse: 成功");
                        set.remove(intent);
                    }else {
                        Log.d("SmsUpdate", "onResponse: 失败");
                    }

                    Intent intent1 =  createIntent(gettime()+"  "+msg+"\n"+jsonObject.toString());
                    mAppContext.sendBroadcast(intent1);
                } catch (Exception e) {
                    Log.d("SmsUpdate","myRunnable.client.newCall："+e);
                    e.printStackTrace();
                }

//
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//
//                        Log.d("SmsUpdate", "onFailure: 失败");
//                        client.newCall(request).enqueue(this);
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        assert response.body() != null;
//                        String result = response.body().string();
//                        Log.d("SmsUpdate", "onResponse: 结果："+result);
//                        try {
//                            JSONObject jsonObject1 = new JSONObject(result);
//
//                            int code = jsonObject1.getInt("code");
//                            if (code == 200){
//                                Log.d("SmsUpdate", "onResponse: 成功");
//                                set.remove(intent);
//
//                                Intent intent1 =  createIntent(msgBody);
//                                mAppContext.sendBroadcast(intent1);
//
//                            }else {
//                                Log.d("SmsUpdate", "onResponse: 失败");
//                                client.newCall(request).enqueue(this);
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        Log.d("SmsUpdate", "onResponse: " + result);
//                    }
//                });

            }else {
                Log.d("SmsUpdate","myRunnabl 没有数据");
            }


            try {
                Thread.sleep(1000);
                this.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }


    private static String gettime(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }


    private String getsign_name(String boby){
        String name;
        if (boby.contains("交通银行")){
            name = "交通银行";
        }else  if (boby.contains("广发银行")){
            name = "广发银行";
        }else  if (boby.contains("工商银行")){
            name = "工商银行";
        }else  if (boby.contains("兴业银行")){
            name = "兴业银行";
        }else  if (boby.contains("建设银行")){
            name = "建设银行";
        }else  if (boby.contains("民生银行")){
            name = "民生银行";
        }else  if (boby.contains("华夏银行")){
            name = "华夏银行";
        }else  if (boby.contains("中信银行")){
            name = "中信银行";
        }else {
            name = "";
        }
        return name;
    }


    @NonNull
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}





