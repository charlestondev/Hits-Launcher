package com.hits.hitslauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by charleston on 17/08/16.
 */

public class WallPaperChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            Launcher.wallPaperChanged = true;
    }
}
