package com.hits.hitslauncher;

/**
 * Created by charleston on 30/11/15.
 */
public class CharButtonTileLauncher extends TileLauncher {

    public String name;
    public boolean active;
    public CharButtonTileLauncher(String label){
        super(label, null);
        this.active = false;
    }
}
