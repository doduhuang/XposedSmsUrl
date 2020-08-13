package com.Walter.smsupdate.xp.hook.code;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SmsMessageUtils {

    private static final int SMS_CHARACTER_LIMIT = 160;

    private SmsMessageUtils() {
    }

    @SuppressLint("NewApi")
    public static SmsMessage[] fromIntent(Intent intent) {
        return Telephony.Sms.Intents.getMessagesFromIntent(intent);
    }

    public static String getMessageBody(SmsMessage[] messageParts) {
        if (messageParts.length == 1) {
            return messageParts[0].getDisplayMessageBody();
        } else {
            StringBuilder sb = new StringBuilder(SMS_CHARACTER_LIMIT * messageParts.length);
            for (SmsMessage messagePart : messageParts) {
                sb.append(messagePart.getDisplayMessageBody());
            }
            return sb.toString();
        }
    }

}
