package com.hits.hitslauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by charleston on 17/08/16.
 */

public class PackageChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
            Log.d("teste", "removed");

        else if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
            Log.d("teste", "added");

    }
}
