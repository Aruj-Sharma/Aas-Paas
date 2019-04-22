package zero2unicorn.aaspaas.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import zero2unicorn.aaspaas.R;
import zero2unicorn.aaspaas.adapter.categoryGridViewAdapter;
import zero2unicorn.aaspaas.classes.Item;
import zero2unicorn.aaspaas.classes.LocationService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private ImageView iv_bg;


    private SharedPreferences Status;
    private SharedPreferences.Editor EStatus;


    LatLng lg;
    private String latestVersion;
    public static String TAG = "aruj";
    String  lat;
    String longi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Status = getSharedPreferences("status", Context.MODE_PRIVATE);
        EStatus = Status.edit();
        iv_bg = findViewById(R.id.iv_bg);
        final ProgressBar pbar=findViewById(R.id.pbar);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
         lat = Status.getString("latitude",null);
         longi = Status.getString("longitude",null);


    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        final GridView gridView = (GridView) findViewById(R.id.gridview);
        final ArrayList<Item> item = new ArrayList<Item>();

        myDatabase.child("category").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                item.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String category_name = snapshot.getKey();
                    String url_val = snapshot.getValue(String.class);
                    item.add(new Item(url_val, category_name));
                }
                if(item.size()>0)
                    pbar.setVisibility(View.GONE);
                gridView.setAdapter(new categoryGridViewAdapter(MainActivity.this, R.layout.category_grid, item));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String category_name = ((Item) parent.getItemAtPosition(position)).getTitle();
                EStatus.putString("category_name", category_name);  EStatus.apply();
                if(category_name != null) {
                    String type = findType(category_name.toLowerCase());
                    if (type != null) {
                        EStatus.putString("category_type", type);   EStatus.apply();
                        Intent i1 = new Intent(MainActivity.this, NearbyPlaceDetails.class);
                        startActivity(i1);
                    }
                    else
                        Snackbar.make(findViewById(android.R.id.content), "An unexpected error has occurred! Please try again later", Snackbar.LENGTH_LONG).show();
                }
                else
                    Snackbar.make(findViewById(android.R.id.content), "An unexpected error has occurred! Please try again later", Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private String findType(String category_name) {
        String type;
        if (category_name.startsWith("atm"))
            type = "atm";
        else if (category_name.startsWith("bank"))
            type = "bank";
        else if (category_name.startsWith("movie") || category_name.startsWith("cinema") || category_name.startsWith("theat"))
            type = "movie_theater";
        else if (category_name.startsWith("hospital") || category_name.startsWith("clinic") || category_name.startsWith("health"))
            type = "hospital";
        else if (category_name.startsWith("parking"))
            type = "parking";
        else if (category_name.startsWith("pharmacy") || category_name.startsWith("medic"))
            type = "pharmacy";
        else if (category_name.startsWith("hotel") || category_name.startsWith("resta") || category_name.startsWith("food"))
            type = "restaurant";
        else if (category_name.startsWith("shopping") || category_name.startsWith("mall"))
            type = "shopping_mall";
        else if (category_name.startsWith("shop") || category_name.startsWith("store"))
            type = "store";
        else
            type = category_name;
        return type;
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent intent = new Intent(getApplicationContext(),aboutUs.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_contact) {
            Intent intent = new Intent(getApplicationContext(),contactUs.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_rate) {
            Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=zero2unicorn.aaspaas"));
            startActivity(i1);
        }
        else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"YOUR DESIDERATUM JUST AT ONE CLICK! Download the Aaspaas application from Google Playstore: https://play.google.com/store/apps/details?id=zero2unicorn.aaspaas");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void alerter(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
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
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void forceUpdate(){
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo =  packageManager.getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String currentVersion = packageInfo.versionName;
        new ForceUpdateAsync(currentVersion,MainActivity.this).execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public class ForceUpdateAsync extends AsyncTask<String, String, JSONObject> {


        private String currentVersion;
        private Context context;

        public ForceUpdateAsync(String currentVersion, Context context) {
            this.currentVersion = currentVersion;
            this.context = context;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName()+ "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                        .first()
                        .ownText();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (latestVersion != null) {
                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    if (!(context instanceof HomeActivity)) {
                        if (!((Activity) context).isFinishing()) {
                            showForceUpdateDialog();
                        }
                    }
                }
            }
            super.onPostExecute(jsonObject);
        }
    }

    public void showForceUpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder.setTitle("Update Available!");
        alertDialogBuilder.setMessage("A new version of Aas-paas is available. Please update to version " + latestVersion + " now");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        alerter();
        forceUpdate();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.current_loc) {


            EStatus.putString("latitude", lat);
            EStatus.apply();
            EStatus.putString("longitude", longi);
            EStatus.apply();
            item.setChecked(true);

        }
        else if (id == R.id.choose_loc) {



            item.setChecked(true);


            try {
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), "AIzaSyDd50Up6FsrwE_obbLdlza6HYc3KH2wLqs");
                }




                List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);


                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this);
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                Log.e(TAG, e.getStackTrace().toString());
            }
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                lg = place.getLatLng();
                Log.i(TAG,String.valueOf(lg.latitude) );
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());

                EStatus.putString("latitude", Double.toString(place.getLatLng().latitude));
                EStatus.apply();
                EStatus.putString("longitude", Double.toString(place.getLatLng().longitude));
                EStatus.apply();


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            }
            else if (resultCode == RESULT_CANCELED)
            {
            }
        }
    }

}