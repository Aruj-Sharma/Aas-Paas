package zero2unicorn.aaspaas.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

import zero2unicorn.aaspaas.R;
import zero2unicorn.aaspaas.classes.places;

import static android.view.View.GONE;

public class FullDetails extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences Status;
    private SharedPreferences.Editor EStatus;



    private ImageView iv_bg;

    public String url = "null",website="null",call="null",share="",place_id,place_name,place_address;
    public static final String API_KEY = "AIzaSyDd50Up6FsrwE_obbLdlza6HYc3KH2wLqs";
    public static String TAG = "aruj";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_details);

        final TextView tv_name = findViewById(R.id.tv_name);
        final TextView tv_category_name = findViewById(R.id.tv_category_name);
        final TextView tv_address = findViewById(R.id.tv_address);
        final TextView tv_open = findViewById(R.id.tv_open);
        final SwipeRefreshLayout sr =findViewById(R.id.swiperefresh);

        final RelativeLayout layout_open = findViewById(R.id.layout_open);

        Status = getSharedPreferences("status", Context.MODE_PRIVATE);
        EStatus = Status.edit();
        iv_bg = findViewById(R.id.iv_bg);

        place_name = Status.getString("place_name", null);
        tv_name.setText(place_name);

        place_id = Status.getString("place_id", null);
        place_address = Status.getString("place_address", null);
        tv_address.setText(place_address);

        String category_name = Status.getString("category_name", null);
        tv_category_name.setText(category_name);

        String place_isopen = Status.getString("place_isopen", null);
        if (place_isopen == null)
            layout_open.setVisibility(GONE);
        else {
            if (place_isopen.equals("true")) {
                tv_open.setText("OPEN-NOW");
                tv_open.setTextColor(Color.parseColor("#058f09"));
            } else if (place_isopen.equals("false")) {
                tv_open.setText("CLOSED");
                tv_open.setTextColor(Color.RED);
            }
        }

        loadPlaceDetails(place_id);

        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPlaceDetails(place_id);
                sr.setRefreshing(false);
            }
        });
    }



    private void loadPlaceDetails(String place_id) {
        final String googlePlacesUrl = getGooglePlacesUrl(place_id,1);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, googlePlacesUrl,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject result) {
                String status = getStringValue(result,"status");
                switch (status){
                    case "OK":
                        Log.i(TAG,result.toString());
                        displayItems(result);
                        break;
                    default:
                        Snackbar.make(findViewById(android.R.id.content), "Sorry for inconvenience! Swipe down to refresh again", Snackbar.LENGTH_LONG).show();

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
        final ImageView iv_image = findViewById(R.id.iv_image);
        final RatingBar rating_bar=findViewById(R.id.rating_bar);
        final TextView tv_rating_bar=findViewById(R.id.tv_rating_bar);
        final TextView tv_call=findViewById(R.id.tv_call);              tv_call.setOnClickListener(this);
        final TextView tv_website=findViewById(R.id.tv_website);        tv_website.setOnClickListener(this);
        final ImageButton ib_navigate=findViewById(R.id.ib_navigate);   ib_navigate.setOnClickListener(this);
        final ImageButton ib_share=findViewById(R.id.ib_share);         ib_share.setOnClickListener(this);
        final ImageButton ib_back=findViewById(R.id.ib_back);           ib_back.setOnClickListener(this);

        final RelativeLayout layout_call=findViewById(R.id.layout_call);
        final RelativeLayout layout_website=findViewById(R.id.layout_website);
        ProgressBar pbar=findViewById(R.id.pbar);

        JSONObject details = getJSONObject(result,"result");

        String image;
        try {
            JSONArray array = details.getJSONArray("photos");
            JSONObject details1 = array.getJSONObject(0);
            String pref = getStringValue(details1, "photo_reference");

            if(pref!=null) {
                image = getGooglePlacesUrl(pref, 2);
                Glide.with(getApplicationContext()).load(image).into(iv_image);
            }
        }
        catch (JSONException e){
            Log.d(TAG,"JSON1 " + e.getMessage());
        }

        String rating = getStringValue(details,"rating");
        call = getStringValue(details,"formatted_phone_number");
        url = getStringValue(details,"url");
        website = getStringValue(details,"website");

        if(rating.equals("null")){
            tv_rating_bar.setText("No ratings");
            rating_bar.setVisibility(GONE);
        }
        else {
            if(rating.length()==1)
                rating += ".0";
            tv_rating_bar.setText(rating);
            rating_bar.setRating(Float.parseFloat(rating));
        }

        if(call.equals("null"))
            layout_call.setVisibility(GONE);
        else
            tv_call.setText(call);

        if(website.equals("null"))
            layout_website.setVisibility(GONE);
        else
            tv_website.setText(website);

        pbar.setVisibility(GONE);
    }

    private String getGooglePlacesUrl(String place_id,int a) {
        if(a==1) {
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
            googlePlacesUrl.append("placeid=").append(place_id);
            googlePlacesUrl.append("&key=").append(API_KEY);
            googlePlacesUrl.append("&sensor=true");
            return googlePlacesUrl.toString();
        }
        else {
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
            googlePlacesUrl.append("&maxheight=140");
            googlePlacesUrl.append("&photoreference=").append(place_id);
            googlePlacesUrl.append("&key=").append(API_KEY);
            googlePlacesUrl.append("&sensor=true");
            return googlePlacesUrl.toString();
        }
    }

    private String getStringValue(JSONObject details,String value){
        try{
            if(details!=null)
                return details.getString(value);
        }
        catch (Exception e){
            Log.d(TAG,"GETS " + e.getMessage());
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



    @Override
    public void onClick(View v) {
        Intent i1;
        switch (v.getId())
        {
            case R.id.ib_navigate:
                if(!url.equals("null"))
                    i1=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                else
                    i1=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/"));
                startActivity(i1);
                break;
            case R.id.ib_share:
                share=place_name + "\n\n" + place_address + "\n\n";
                if(!call.equals("null"))
                    share=share+call+"\n";
                if(!website.equals("null"))
                    share=share+website+"\n";
                share+= "\nhttps://www.google.com/maps/search/?api=1&query=Google&query_place_id="+place_id;

                i1=new Intent();
                i1.setAction(Intent.ACTION_SEND);
                i1.putExtra(Intent.EXTRA_TEXT,share);
                i1.setType("text/plain");
                startActivity(i1);
                break;
            case R.id.ib_back:
                EStatus.putInt("counter1",Status.getInt("counter1",0)+1); EStatus.apply();
                i1=new Intent(FullDetails.this,NearbyPlaceDetails.class);
                startActivity(i1);
                break;
            case R.id.tv_website:
                if(!website.equals("null")) {
                    i1 = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                    startActivity(i1);
                }
                break;
            case R.id.tv_call:
                if(!call.equals("null")) {
                    i1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + call));
                    startActivity(i1);
                }
                break;
        }
    }

    /*******************************************************************************/
    //Internet Connection Alert

    private boolean alerter(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FullDetails.this);
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

    /*******************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();
        alerter();
    }

    @Override
    public void onBackPressed() {
        EStatus.putInt("counter1",Status.getInt("counter1",0)+1); EStatus.apply();
        super.onBackPressed();
    }
}
