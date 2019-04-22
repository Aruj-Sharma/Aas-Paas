package zero2unicorn.aaspaas.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import zero2unicorn.aaspaas.classes.Item;
import zero2unicorn.aaspaas.R;

public class categoryGridViewAdapter extends ArrayAdapter<Item> {
    private Context mContext;

    int layoutResourceId;
    ArrayList<Item> data = new ArrayList<Item>();

    public categoryGridViewAdapter(Context c, int layoutResourceId, ArrayList<Item> data){
        super(c, layoutResourceId, data);

        mContext = c;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=convertView;
        ViewHolder holder=null;

        if(view==null) {
            view=((Activity) mContext).getLayoutInflater().inflate(layoutResourceId, parent,false);

            holder = new ViewHolder();
            holder.tv_category_name = (TextView) view.findViewById(R.id.tv_category_name);
            holder.iv_category_image = (ImageView) view.findViewById(R.id.tv_category_image);

            view.setTag(holder);
        }
        else
            holder=(ViewHolder) view.getTag();

        Item i=data.get(position);
        holder.tv_category_name.setText(i.getTitle());
        Glide.with(mContext).load(i.getImage()).apply(new RequestOptions().error(R.drawable.unknown)).into(holder.iv_category_image);

        return view;
    }

    public static class ViewHolder {
        TextView tv_category_name;
        ImageView iv_category_image;
    }
}