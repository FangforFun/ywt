package com.gkzxhn.gkprison.userport.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.activity.ShoppingRecoderActivity;
import com.gkzxhn.gkprison.userport.bean.Commodity;

import java.util.List;

/**
 * Created by admin on 2016/1/14.
 */
public class CommidtyAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Commodity> list;
    ShoppingAdapter adapter;
    int cartPosition;
    ShoppingRecoderActivity context;

    public CommidtyAdapter(ShoppingAdapter adapter, int cartPosition, ShoppingRecoderActivity context, List<Commodity> list) {
        this.adapter = adapter;
        this.cartPosition = cartPosition;
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder1 holder1;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.recoding_items,null);
            holder1 = new ViewHolder1();
            holder1.description = (TextView)convertView.findViewById(R.id.tv_shopping_desciption);
            holder1.price = (TextView)convertView.findViewById(R.id.tv_shopping_mongey);
            holder1.qty = (TextView)convertView.findViewById(R.id.tv_shopping_qty);
            convertView.setTag(holder1);
        }else {
            holder1 = (ViewHolder1)convertView.getTag();
        }
        final Commodity commodity = list.get(position);
        holder1.description.setText(commodity.getTitle());
        holder1.price.setText(commodity.getPrice());
        holder1.qty.setText(commodity.getQty()+"");
        return convertView;
    }
    private class ViewHolder1{
        TextView description;
        TextView price;
        TextView qty;
    }
}
