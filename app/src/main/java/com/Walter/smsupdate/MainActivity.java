package com.Walter.smsupdate;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.Walter.smsupdate.ModuleUtils.isModuleEnabled;
import static com.Walter.smsupdate.PrefConst.KEY_ENABLE;
import static com.Walter.smsupdate.xp.hook.code.SmsHandlerHook.SmscreateIntent;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> data = new ArrayList<String>();
    private ListView listView = null;
    private ArrayAdapter<String> adapter = null;
    private SharedPreferences sp=null;
    private MyBroadcastReceiver myBroadcastReceiver;
    private IntentFilter intentFilter;
    private static final String ACTION_COPY_CODE = BuildConfig.APPLICATION_ID + ".ACTION_LOG_CODE";
    private static final String EXTRA_KEY_CODE = "extra_key_code";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_COPY_CODE);
        myBroadcastReceiver= new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver,intentFilter);

        sp= getPreferences(Context.MODE_PRIVATE);

        listView = findViewById(R.id.loglist);
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);

        TextView textView1 = findViewById(R.id.text1);
        TextView textView = findViewById(R.id.dvicedid);
        textView.setText("当前设备id: "+getUniquePsuedoID());


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final ClipboardManager clipboard = (ClipboardManager)v.getContext().getSystemService(v.getContext().CLIPBOARD_SERVICE);
                    ClipData textCd = ClipData.newPlainText("data", getUniquePsuedoID());
                    clipboard.setPrimaryClip(textCd);
                    Toast.makeText(v.getContext(),"内容已复制："+getUniquePsuedoID(),Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (isModuleEnabled())
            textView1.setText("激活状态: 已激活");
        else
            textView1.setText("激活状态: 未激活");


        final Button button = findViewById(R.id.start);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if  (button.getText().equals("开启监听")){
                    button.setText("正在监听");
                    sp.edit().putBoolean(KEY_ENABLE,false).apply();
                    Intent intent =   SmscreateIntent(true);
                    sendBroadcast(intent);
                } else{
                    button.setText("开启监听");
                    sp.edit().putBoolean(KEY_ENABLE,true).apply();
                    Intent intent =   SmscreateIntent(false);
                    sendBroadcast(intent);
                }
            }
        });
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(myBroadcastReceiver);
    }



    @SuppressLint("NewApi")
    public static String getUniquePsuedoID() {
        String serial = null;
        String m_szDevIDShort = "35" +  Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ; //13 位
        try {
            serial = Objects.requireNonNull(Build.class.getField("SERIAL").get(null)).toString();

            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {

        }
        assert serial != null;
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();

    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        public MyBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();
            String smsCode = "";
            if (ACTION_COPY_CODE.equals(action)) {
                smsCode = intent.getStringExtra(EXTRA_KEY_CODE);

            }

            if (data.size()>1000){
                data.clear();
            }
            data.add(0,smsCode);
            adapter.notifyDataSetChanged();
        }
    }


    public static Intent createIntent(String smsCode) {
        Intent intent = new Intent(ACTION_COPY_CODE);
        intent.putExtra(EXTRA_KEY_CODE, smsCode);
        return intent;
    }

}
