package zero2unicorn.aaspaas.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;

import zero2unicorn.aaspaas.R;
import zero2unicorn.aaspaas.adapter.ListViewAdapter;
import zero2unicorn.aaspaas.classes.Item;
import zero2unicorn.aaspaas.classes.places;

public class NearbyPlaceDetails extends AppCompatActivity{

    private double lat, log;
    private Location loc;

    private double radius=5;
    private ListViewAdapter adapter;
    private int tab=2;

    private SharedPreferences Status;
    private SharedPreferences.Editor EStatus;
    private ImageView iv_bg;
    private ProgressBar pbar;

    private TextView tv_radius;
    private SeekBar sbar;

   // private InterstitialAd mInterstitialAd;

    public static final String API_KEY = "AIzaSyDd50Up6FsrwE_obbLdlza6HYc3KH2wLqs";
    public static String TAG="aruj";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_place_details);

        Status=getSharedPreferences("status",Context.MODE_PRIVATE);
        EStatus=Status.edit();
        pbar=findViewById(R.id.pbar);

        iv_bg = findViewById(R.id.iv_bg);

        loadImage();



        try {
            lat = Double.parseDouble(Status.getString("latitude", null));
            log = Double.parseDouble(Status.getString("longitude", null));
            loc=new Location("src");
            loc.setLatitude(lat);
            loc.setLongitude(log);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }

        String category_name = Status.getString("category_name",null);
        getSupportActionBar().setTitle(category_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);



        ListView l1=(ListView)findViewById(R.id.list_view);
        sbar=findViewById(R.id.seekBar);
        tv_radius=findViewById(R.id.tv_radius);

        ArrayList<places> arr = new ArrayList<places>();
        adapter = new ListViewAdapter(this, arr);

        l1.setAdapter(adapter);

        findPlaces();



        l1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(alerter()){
                    String place_id = ((places) parent.getItemAtPosition(position)).getPlaceId();
                    String place_name = ((places) parent.getItemAtPosition(position)).getName();
                    String place_address = ((places) parent.getItemAtPosition(position)).getAddress();
                    EStatus.putString("place_id", place_id);  EStatus.apply();
                    EStatus.putString("place_name", place_name);  EStatus.apply();
                    EStatus.putString("place_address", place_address);  EStatus.apply();

                    String place_isopen = ((places) parent.getItemAtPosition(position)).getIsopen();
                    if(!place_isopen.equals("null")) {
                        EStatus.putString("place_isopen", place_isopen);    EStatus.apply();
                    }

                    EStatus.putInt("counter",Status.getInt("counter",0)+1); EStatus.apply();

                    Intent intent = new Intent(NearbyPlaceDetails.this,FullDetails.class);
                    startActivity(intent);
                }
            }
        });

        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress==0 ? 0.5 : progress;
                tv_radius.setText("Distance Range: "+Double.toString(radius) + " km");
                findPlaces();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                findPlaces();
            }
        });
    }



    private void findPlaces(){
        String type = Status.getString("category_type", null);
        String googlePlacesUrl = getGooglePlacesUrl(type, tab);

        loadNearByPlaces(googlePlacesUrl);
    }

    private void loadNearByPlaces(final String googlePlacesUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, googlePlacesUrl,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject result) {
                String status = getStringValue(result,"status");
                switch (status){
                    case "ZERO_RESULTS":
                        Snackbar.make(findViewById(android.R.id.content), "Please increase the distance range", Snackbar.LENGTH_LONG).show();
                        break;
                    case "OK":
                        displayItems(result);
                        break;
                    default:
                        Snackbar.make(findViewById(android.R.id.content), "Sorry for inconvenience!", Snackbar.LENGTH_LONG).show();

                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }

    /*******************************************************************************/




    private void displayItems(JSONObject result){
        try{
            JSONArray array = result.getJSONArray("results");
            adapter.clear();
            for(int i=0;i<array.length();i++){
                JSONObject details = array.getJSONObject(i);

                String pref=null;
                String image;
                try {
                    JSONArray array1 = details.getJSONArray("photos");
                    JSONObject details1 = array1.getJSONObject(0);
                    pref = getStringValue(details1, "photo_reference");
                }
                catch (JSONException e){
                    //Log.d(TAG,"JSON1 " + e.getMessage());
                }

                if(pref==null) {
                    image = getStringValue(details,"icon");
                    //Log.d(TAG,"sending image1");
                }
                else {
                    image = getGooglePlacesUrl(pref, 3);
                    //Log.d(TAG,"sending image2");
                }

                String name = getStringValue(details,"name");
                String place_id = getStringValue(details,"place_id");
                String vicinity = getStringValue(details,"vicinity");

                JSONObject details1 = getJSONObject(details,"opening_hours");
                String isOpen = getStringValue(details1,"open_now");

                details1 = getJSONObject(details,"geometry");
                JSONObject details2 = getJSONObject(details1,"location");
                String lat = getStringValue(details2,"lat");
                String log = getStringValue(details2,"lng");

                Location loc1=new Location("Dest");
                double lat1=Double.parseDouble(lat);
                double log1=Double.parseDouble(log);
                loc1.setLatitude(lat1);
                loc1.setLongitude(log1);
                double distance=loc.distanceTo(loc1);


                adapter.add(new places(image,name,vicinity,isOpen,distance,place_id));
                pbar.setVisibility(View.GONE);
            }
        }

        catch (JSONException e){

        }
    }

    private String getStringValue(JSONObject details,String value){
        try{
            if(details!=null)
                return details.getString(value);
        }
        catch (Exception e){

        }
        return "null";
    }

    private JSONObject getJSONObject(JSONObject details,String value){
        try{
            if(details!=null)
                return details.getJSONObject(value);
        }
        catch (Exception e){

        }
        return null;
    }

    private String getGooglePlacesUrl(String type,int a) {
        if(a==1) {
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=").append(Double.toString(lat)).append(",").append(Double.toString(log));
            googlePlacesUrl.append("&rankby=distance");
            googlePlacesUrl.append("&types=").append(type);
            googlePlacesUrl.append("&key=").append(API_KEY);
            googlePlacesUrl.append("&sensor=true");
            return googlePlacesUrl.toString();
        }
        else if(a==2) {
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=").append(Double.toString(lat)).append(",").append(Double.toString(log));
            googlePlacesUrl.append("&radius=").append(radius * 1000);
            googlePlacesUrl.append("&types=").append(type);
            googlePlacesUrl.append("&key=").append(API_KEY);
            googlePlacesUrl.append("&sensor=true");
            return googlePlacesUrl.toString();
        }

        // https://maps.googleapis.com/maps/api/place/search/
        // json?keyword=coffee&location=37.787930,-122.4074990&radius=5000&sensor=false&key=AIzaSyDd50Up6FsrwE_obbLdlza6HYc3KH2wLqs
        else {
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
            googlePlacesUrl.append("&maxwidth=80");
            googlePlacesUrl.append("&photoreference=").append(type);
            googlePlacesUrl.append("&key=").append(API_KEY);
            googlePlacesUrl.append("&sensor=true");
            return googlePlacesUrl.toString();
        }
    }

    private void loadImage(){
        try {
            File f=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Aaspaas", "bg.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
           // iv_bg.setImageBitmap(b);

            if(Status.getString("bgcolor", null)!=null)
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(Status.getString("bgcolor", null))));
        }
        catch (Exception e) {
            Log.d(TAG,e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sort, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.nav_distance) {
            tab=1;
            item.setChecked(true);
            sbar.setVisibility(View.INVISIBLE);
            tv_radius.setVisibility(View.INVISIBLE);
            findPlaces();
        }
        else if (id == R.id.nav_rating) {
            tab=2;
            item.setChecked(true);
            sbar.setVisibility(View.VISIBLE);
            tv_radius.setVisibility(View.VISIBLE);
            findPlaces();
        }
        else if (id != R.id.menu_item_add) {
            EStatus.putInt("counter1",Status.getInt("counter1",0)+1); EStatus.apply();
            Intent i1 = new Intent(NearbyPlaceDetails.this, MainActivity.class);
            startActivity(i1);
        }
        return true;
    }


    private boolean alerter(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NearbyPlaceDetails.this);
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
        alerter();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
