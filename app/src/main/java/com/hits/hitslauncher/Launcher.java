package com.hits.hitslauncher;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Launcher extends AppCompatActivity {
    private RecyclerView launcherRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemLauncherAdapter mAdapter;
    private PackageManager manager;
    private List<TileLauncher> allTiles;
    private List<TileLauncher> allApps;
    private List<TileLauncher> alphaSortedApps;
    private List<CharButtonTileLauncher> characterButtons;
    private boolean sorting = false;
    public static boolean configChanged = false;
    public String letras[] = new String[]{"ABC","DEF","GHIJk","LMNO", "PQR","STU","VWXYZ"};
    public BitmapDrawable wallPaperBD;
    public static boolean wallPaperChanged = true;
    public static int linhas_recentes = 5;
    private final BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                addAppToList(intent.getData().toString().substring(8));
            }
            else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                removeAppFromList(intent.getData().toString().substring(8));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        //findViewById(R.id.launcher).setBackgroundDrawable(wallpaperDrawable);

        //findViewById(R.id.background_image).setBackgroundDrawable(wallpaperDrawable);

        //Bitmap wallpapaer = BitmapFactory.de

        launcherRecyclerView = (RecyclerView) findViewById(R.id.launcher_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        launcherRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager = new GridLayoutManager(this, 210);
        launcherRecyclerView.setLayoutManager(mLayoutManager);


        allTiles = new ArrayList();
        characterButtons = new ArrayList();
        allApps = new ArrayList<>();
        allTiles.add(new TileLauncher("",null));

        sorting = true;
        new LoadApps().execute();

        // specify an adapter (see also next example)
        mAdapter = new ItemLauncherAdapter(allTiles);

        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position==0)
                    return 210;
                else if(position<10)
                    return 42;
                else if(position<22)
                    return 35;
                else
                    return 30;
            }
        };
        ((GridLayoutManager)mLayoutManager).setSpanSizeLookup(spanSizeLookup);

        launcherRecyclerView.setAdapter(mAdapter);

        IntentFilter ift = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        ift.addAction(Intent.ACTION_PACKAGE_REMOVED);
        ift.addDataScheme("package");
        registerReceiver(packageChangedReceiver, ift);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(configChanged){
            mAdapter.notifyDataSetChanged();
            configChanged = false;
            linhas_recentes = ConfigActivity.getRecentesHomeQTD(this);
        }

        if(wallPaperChanged){
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            wallPaperBD = (BitmapDrawable) wallpaperDrawable;
            ((ImageView)findViewById(R.id.background_image)).setImageBitmap(wallPaperBD.getBitmap());
            wallPaperChanged = false;
        }

        if(!sorting)
            new SortApps().execute();
        mLayoutManager.smoothScrollToPosition(launcherRecyclerView, null,0);
    }
    private class LoadApps extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            manager = getPackageManager();
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
            SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
            for(ResolveInfo ri:availableActivities){
                String packageName = ri.activityInfo.packageName;
                String label = ri.loadLabel(manager).toString();
                int touchCount = sharedPref.getInt(packageName+"count", 0);

                AppTileLauncher app = new AppTileLauncher(label, packageName,ri.activityInfo.loadIcon(manager), touchCount);
                allApps.add(app);
            }

            for(String letra:letras)
                allTiles.add(new CharButtonTileLauncher(letra));
            for(int j = 0; j < 22; j++)
                allTiles.add(j+1,allApps.get(j));
            for(int j = 22; j < allApps.size(); j++)
                allTiles.add(j+7+1, allApps.get(j));

            return "Executed";

        }

        @Override
        protected void onPostExecute(String result) {
            new SortApps().execute();
        }

        @Override
        protected void onPreExecute() {}
        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    private class SortApps extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            touchSortApps();
            alphaSortApps();
            CharButtonTileLauncher buttonActive = null;
            for(CharButtonTileLauncher button: characterButtons){
                if(button.active){
                    buttonActive = button;
                    break;
                }
            }
            if(buttonActive!=null)
                filterApps(buttonActive.label);

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //launcherRecyclerView.setAdapter(new ItemLauncherAdapter(allTiles));
            mAdapter.notifyDataSetChanged();
            if(findViewById(R.id.load_apps)!=null)
                findViewById(R.id.load_apps).setVisibility(View.GONE);
            sorting = false;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    public void touchSortApps(){
        Collections.sort(allApps, new Comparator<TileLauncher>() {
            @Override
            public int compare(TileLauncher appTileLauncher, TileLauncher t1) {
                if(((AppTileLauncher) appTileLauncher).touchCount == ((AppTileLauncher)t1).touchCount)
                    //return 0;
                    return appTileLauncher.label.compareTo(t1.label);
                else if(((AppTileLauncher) appTileLauncher).touchCount > ((AppTileLauncher)t1).touchCount)
                    return -1;
                else
                    return 1;
            }
        });
        for(int j = 0; j < 22;j ++)
            allTiles.set(j+1, allApps.get(j));

    }
    public void alphaSortApps(){
        int length = allTiles.size();
        for(int j = 30; j < length; j++)
            allTiles.remove(30);

        alphaSortedApps = new ArrayList<>();
        for(int j = 22; j < allApps.size(); j++){
            alphaSortedApps.add(allApps.get(j));
        }

        Collections.sort(alphaSortedApps, new Comparator<TileLauncher>() {
            @Override
            public int compare(TileLauncher appTileLauncher, TileLauncher t1) {
                return appTileLauncher.label.compareTo(t1.label);
            }
        });
        int len = alphaSortedApps.size();
        for(int j = 0; j < len; j++){
            allTiles.add(23+7, alphaSortedApps.get(len-1-j));
        }


        //mLayoutManager.smoothScrollToPosition(launcherRecyclerView, null,allTiles.size());
    }
    public void filterApps(String filter){
            String chars[] = new String[filter.length()];
            for(int i = 0; i < filter.length(); i++)
                chars[i] = filter.substring(i,i+1);

            for(int j = 0; j < allApps.size(); j++){
                ((AppTileLauncher)allApps.get(j)).fitered = false;
                for(int i = 0; i < chars.length; i++){
                    String firstChar = allApps.get(j).label.substring(0,1).toUpperCase();
                    if(chars[i].equals(firstChar)){
                        ((AppTileLauncher)allApps.get(j)).fitered = true;
                    }
                }
            }
    }
    public void removeAppFromList(String packageName){
        for(int i = 0; i < allApps.size(); i++){
            if(((AppTileLauncher)allApps.get(i)).name.equals(packageName)){
                allApps.remove(i);
                break;
            }
        }
        touchSortApps();
        alphaSortApps();
        mAdapter.notifyDataSetChanged();
    }
    public void addAppToList(String packageName){
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        final Drawable applicationIcon = (Drawable) (ai != null ? pm.getApplicationIcon(ai) : "(unknown)");
        allApps.add(new AppTileLauncher(applicationName, packageName,applicationIcon,0));
        alphaSortApps();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    /*public static class PackageChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
                Log.d("teste", "removed");
                //removeAppFromList(intent.getData().toString().substring(8));
            else if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
                Log.d("teste", "added");
                //addAppToList(intent.getData().toString().substring(8));
        }
    }*/
}
