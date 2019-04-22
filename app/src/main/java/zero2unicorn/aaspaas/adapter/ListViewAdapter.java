package zero2unicorn.aaspaas.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import java.text.DecimalFormat;
import java.util.ArrayList;

import zero2unicorn.aaspaas.R;
import zero2unicorn.aaspaas.classes.places;

public class ListViewAdapter extends ArrayAdapter<places> {

    public ListViewAdapter(Context context, ArrayList<places> places){
        super(context, 0, places);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        places p= getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.places_list_view, parent, false);
        }

        ImageView iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        TextView tv_address = (TextView) convertView.findViewById(R.id.tv_address);
        TextView tv_isopen = (TextView) convertView.findViewById(R.id.tv_isopen);
        TextView tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);

        Glide.with(getContext()).load(p.getImage()).apply(new RequestOptions().error(R.drawable.unknown)).into(iv_image);

        tv_name.setText(p.getName());
        tv_address.setText(p.getAddress());
        if (p.getIsopen().equals("true")) {
            tv_isopen.setText("OPEN-NOW");
            tv_isopen.setTextColor(Color.parseColor("#058f09"));
        }
        else if (p.getIsopen().equals("false")) {
            tv_isopen.setText("CLOSED");
            tv_isopen.setTextColor(Color.RED);
        }
        else {
            tv_isopen.setText("");
            tv_isopen.setVisibility(View.INVISIBLE);
        }
        tv_distance.setText(new DecimalFormat("##.#").format(p.getDistance()/1000) + " KM");

        return convertView;
    }
}