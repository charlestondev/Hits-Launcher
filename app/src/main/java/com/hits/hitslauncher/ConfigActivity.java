package com.hits.hitslauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class ConfigActivity extends AppCompatActivity {

    public static String tilesBGColor = "#50000000";
    public static String filteredColor = "#50FF4081";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ((CheckBox)findViewById(R.id.check_show_touch_count)).setChecked(sharedPrefs.getBoolean("show_touch_count", false));
        ((CheckBox)findViewById(R.id.check_show_app_name)).setChecked(sharedPrefs.getBoolean("show_app_name", false));



        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        Spinner spinner = (Spinner) findViewById(R.id.spinner_qtd_recentes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.apps_recentes_home_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                saveRecentesHomeQTD(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setSelection(adapter.getPosition(getRecentesHomeQTD(this)+""));
    }
    public void saveRecentesHomeQTD(String qtd){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("recentes_home_qtd", Integer.parseInt(qtd));
        editor.commit();
        Launcher.configChanged = true;
    }
    public static int getRecentesHomeQTD(Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getInt("recentes_home_qtd", 5);
    }
    public void saveShowTouchCount(View view){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("show_touch_count", ((CheckBox)view).isChecked());
        editor.commit();
        Launcher.configChanged = true;
    }
    public static boolean showTouchCount(Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("show_touch_count", false);
    }
    public void saveShowAppName(View view){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("show_app_name", ((CheckBox)view).isChecked());
        editor.commit();
        Launcher.configChanged = true;
    }
    public static boolean showAppName(Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("show_app_name", false);
    }
}
