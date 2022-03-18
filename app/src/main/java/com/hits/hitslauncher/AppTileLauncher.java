package com.hits.hitslauncher;

import android.graphics.drawable.Drawable;

/**
 * Created by charleston on 30/11/15.
 */
public class AppTileLauncher extends TileLauncher {

    public String name;
    public int touchCount;
    public int position;
    public boolean fitered;
    public boolean readyToRemove;

    public AppTileLauncher(String label, String name, Drawable icon, int touchCount){
        super(label, icon);
        this.name = name;
        this.touchCount = touchCount;
        this.position = 0;
        this.fitered = false;
        this.readyToRemove = false;
    }
    public AppTileLauncher(String label, String name, Drawable icon, int touchCount, int position){
        this(label, name, icon, touchCount);
        this.position = position;
    }
}
