package zero2unicorn.aaspaas.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import zero2unicorn.aaspaas.R;
import zero2unicorn.aaspaas.classes.LocationService;

public class HomeActivity extends AppCompatActivity {

    private Location loc1, loc2;
    private DatabaseReference myDatabase;

    private SharedPreferences Status;
    private SharedPreferences.Editor EStatus;

    private BroadcastReceiver locationUpdateReceiver;
    public LocationService locationService;
    private ServiceConnection serviceConnection;
    private LocationManager locationManager;

    private ImageView iv_bg;
    public static String TAG = "aruj";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Status = getSharedPreferences("status", Context.MODE_PRIVATE);
        EStatus = Status.edit();
        iv_bg = findViewById(R.id.iv_bg);

        regiterCallback();
        final Intent locationService = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(locationService);
        this.getApplication().bindService(locationService, serviceConnection, Context.BIND_AUTO_CREATE);


    }


    public boolean checkGPS() {
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        Boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPS) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
            alertDialog.setTitle("GPS isn't Enabled!").setMessage("Please turn ON your GPS").setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }).show();
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 201);
            }
            else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                }
                loc1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc1 != null) {
                    EStatus.putString("latitude", Double.toString(loc1.getLatitude()));
                    EStatus.apply();
                    EStatus.putString("longitude", Double.toString(loc1.getLongitude()));
                    EStatus.apply();
                    EStatus.putString("latitudeC", Double.toString(loc1.getLatitude()));
                    EStatus.apply();
                    EStatus.putString("longitudeC", Double.toString(loc1.getLongitude()));
                    EStatus.apply();

                    if (locationUpdateReceiver != null)
                        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(locationUpdateReceiver);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i1 = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(i1);
                        }
                    }, 2500);
                }
            }
        }
        return isGPS;
    }

    private void regiterCallback() {
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                if (className.getClassName().endsWith("LocationService")) {
                    locationService = ((LocationService.LocationServiceBinder) service).getService();
                    locationService.startUpdatingLocation();
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                if (className.getClassName().equals("LocationService")) {
                    locationService.stopUpdatingLocation();
                    locationService = null;
                }
            }
        };

        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loc2 = intent.getParcelableExtra("location");
                if (loc2 != null) {
                    EStatus.putString("latitude", Double.toString(loc2.getLatitude()));
                    EStatus.apply();
                    EStatus.putString("longitude", Double.toString(loc2.getLongitude()));
                    EStatus.apply();

                    if (locationUpdateReceiver != null)
                        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(locationUpdateReceiver);

                    Intent i1 = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(i1);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter("LocationUpdated"));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(findViewById(android.R.id.content), "Please allow Aaspaas to access your GPS", Snackbar.LENGTH_LONG).show();
                }
                break;
            case 201:
                if(grantResults.length > 0){

                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(findViewById(android.R.id.content), "Please allow Aaspaas to access your GPS", Snackbar.LENGTH_LONG).show();
                    }
                    else{
                        loc1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc1 != null) {
                            EStatus.putString("latitude", Double.toString(loc1.getLatitude()));
                            EStatus.apply();
                            EStatus.putString("longitude", Double.toString(loc1.getLongitude()));
                            EStatus.apply();

                            if (locationUpdateReceiver != null)
                                LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(locationUpdateReceiver);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i1 = new Intent(HomeActivity.this, MainActivity.class);
                                    startActivity(i1);
                                }
                            }, 2500);
                        }
                    }
                }
                default:
                    if(grantResults.length > 0){

                        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(findViewById(android.R.id.content), "Please allow Aaspaas to access your GPS", Snackbar.LENGTH_LONG).show();
                        }
                        else{
                            loc1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc1 != null) {
                                EStatus.putString("latitude", Double.toString(loc1.getLatitude()));
                                EStatus.apply();
                                EStatus.putString("longitude", Double.toString(loc1.getLongitude()));
                                EStatus.apply();

                                if (locationUpdateReceiver != null)
                                    LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(locationUpdateReceiver);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i1 = new Intent(HomeActivity.this, MainActivity.class);
                                        startActivity(i1);
                                    }
                                }, 2500);
                            }
                        }
                    }
        }
    }


    private boolean alerter(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
            alertDialog.setTitle("Network Connection isn't Enabled!").setMessage("Please turn ON your Wifi or Mobile data").setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            try {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                                startActivity(intent);
                            }
                            catch (Exception e){
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                Log.d(TAG,e.getMessage());
                            }
                        }
                    }).show();
            return false;
        }
        return true;
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(alerter()) {
            if (checkGPS()) {
                RelativeLayout layout_pbar=findViewById(R.id.layout_pbar);
                layout_pbar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (locationUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
        }

        super.onDestroy();
    }
}