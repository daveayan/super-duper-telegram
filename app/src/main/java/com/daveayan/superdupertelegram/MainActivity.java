package com.daveayan.superdupertelegram;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String SDT_SMS_PERMISSION_GRANTED = "SDT_SMS_PERMISSION_GRANTED";
    private static final String SDT_MESSAGES = "SDT_MESSAGES";

    private static final String SDT_FROM_NUMBER = "SDT_FROM_PHONE";
    private static final String SDT_TO_NUMBER = "SDT_TO_PHONE";

    List<String> messagesToSend = new ArrayList<String>();

    private void save(String key, int value) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void save(String key, String value) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private int get(String key, int defaultValue) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }

    private String get(String key, String defaultValue) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, defaultValue);
    }

    private List<String> getAsList(String key) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String str = sharedPref.getString(key, "");
        List<String> stringAsList = new ArrayList<>();
        for(String msg: str.split(",")) {
            if(str.trim().length() != 0) {
                stringAsList.add(msg);
            }
        }
        return stringAsList;
    }

    private void save(String key, List<String> strings) {
        String messages = "";
        for(String message: strings) {
            if(message.trim().length() != 0) {
                messages = messages + "," + message;
            }
        }

        save(key, messages);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int sms_permission_granted = get(SDT_SMS_PERMISSION_GRANTED, 0);

        if(sms_permission_granted == 0) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
            save(SDT_SMS_PERMISSION_GRANTED, 1);
        }

        refreshViews();
    }

    private void refreshViews() {
        String fromNumber = get(SDT_FROM_NUMBER, "From Number");
        String toNumber = get(SDT_TO_NUMBER, "To Number");

        final TextView fromTextView = (TextView) findViewById(R.id.textFromNumber);
        final TextView toTextView = (TextView) findViewById(R.id.textToPhoneNumber);

        if(fromNumber.trim().length() != 0) {
            fromTextView.setText(fromNumber);
        } else {
            fromTextView.setText("From Number");
        }

        if(toNumber.trim().length() != 0) {
            toTextView.setText(toNumber);
        } else {
            toTextView.setText("To Number");
        }

        List<String> messages = getAsList(SDT_MESSAGES);
        LinearLayout ll = (LinearLayout)findViewById(R.id.llMessages);
        ll.removeAllViewsInLayout();
        for(int i = 0; i < messages.size(); i++) {
            String m = messages.get(i);
            if(m.trim().length() != 0) {

                CheckBox checkBox = new CheckBox(this);
                checkBox.setId(i);
                checkBox.setText(m);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean checked = ((CheckBox) view).isChecked();
                        if(checked) {
                            System.out.println("Checked " + ((CheckBox) view).getText());
                            messagesToSend.add(((CheckBox) view).getText().toString());
                        } else {
                            System.out.println("UnChecked " + ((CheckBox) view).getText());
                            messagesToSend.remove(((CheckBox) view).getText().toString());
                        }
                    }
                });

                TableRow row = new TableRow(this);
                row.setId(i);
                row.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));

                row.addView(checkBox);

                ll.addView(row);
            }
        }
    }

    protected void onSaveSettings(View v) {
        final TextView toTextView = (TextView) findViewById(R.id.textToPhoneNumber);
        final TextView fromTextView = (TextView) findViewById(R.id.textFromNumber);
        final TextView message = (TextView) findViewById(R.id.textMessage);

        final String strMessage = message.getText().toString().trim();
        final String toPhoneNumber = toTextView.getText().toString().trim();
        final String fromPhoneNumber = fromTextView.getText().toString().trim();

        List<String> messages = getAsList(SDT_MESSAGES);
        if(strMessage.trim().length() != 0 && ! messages.contains(strMessage.trim())) {
            messages.add(strMessage.trim());
        }

        save(SDT_FROM_NUMBER, fromPhoneNumber);
        save(SDT_TO_NUMBER, toPhoneNumber);
        save(SDT_MESSAGES, messages);

        refreshViews();
    }

    protected void onSendMessageClicked(View v) {
        final TextView toTextView = (TextView) findViewById(R.id.textToPhoneNumber);
        final TextView fromTextView = (TextView) findViewById(R.id.textFromNumber);

        final String toPhoneNumber = toTextView.getText().toString();
        final String fromPhoneNumber = fromTextView.getText().toString();

        LinearLayout ll = (LinearLayout)findViewById(R.id.llMessages);

        for(String message: messagesToSend) {
            System.out.println("Send to number " + toPhoneNumber + ", message: " + message);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("tel:" + toPhoneNumber, null, message, null, null);
        }
    }
}