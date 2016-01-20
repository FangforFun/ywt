package com.gkzxhn.gkprison.userport.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("short")){
            Toast.makeText(context, "short alarm", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "repeating alarm", Toast.LENGTH_SHORT).show();
        }
    }
}
