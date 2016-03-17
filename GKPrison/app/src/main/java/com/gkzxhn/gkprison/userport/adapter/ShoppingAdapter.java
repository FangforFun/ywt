package com.gkzxhn.gkprison.userport.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.userport.activity.ShoppingRecoderActivity;
import com.gkzxhn.gkprison.userport.bean.Cart;
import com.gkzxhn.gkprison.userport.view.CustomListView;
import com.gkzxhn.gkprison.utils.ListViewParamsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/1/14.
 */
public class ShoppingAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Cart> list;
    List<CommidtyAdapter> commodityAdapterList = new ArrayList<CommidtyAdapter>();
    ShoppingRecoderActivity context;

    public ShoppingAdapter(ShoppingRecoderActivity context, List<Cart> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
        for (int i = 0; i < list.size(); i++) {
            CommidtyAdapter commidtyAdapter = new CommidtyAdapter(this,i,context,
                     list.get(i).getCommodityList());
            commodityAdapterList.add(commidtyAdapter);
        }
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
        ViewHoler viewHoler;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.shoppingrecode_item,null);
            viewHoler = new ViewHoler();
            viewHoler.tv_paytime = (TextView)convertView.findViewById(R.id.tv_paytime);
            viewHoler.tv_qty = (TextView)convertView.findViewById(R.id.tv_qty);
            viewHoler.tvshopping_money = (TextView)convertView.findViewById(R.id.tvshopping_money);
            viewHoler.lv_recode = (CustomListView)convertView.findViewById(R.id.lv_recode);
            viewHoler.tv_alipay_trading_num = (TextView) convertView.findViewById(R.id.tv_alipay_trading_num);
            viewHoler.tv_transact_state = (TextView) convertView.findViewById(R.id.tv_transact_state);
            convertView.setTag(viewHoler);
        }else {
            viewHoler = (ViewHoler)convertView.getTag();
        }
        viewHoler.lv_recode.setAdapter(commodityAdapterList.get(position));
        ListViewParamsUtils.setListViewHeightBasedOnChildren(viewHoler.lv_recode);
        final Cart cart = list.get(position);
        viewHoler.tv_paytime.setText(cart.getTime());
        viewHoler.tv_alipay_trading_num.setText(cart.getOut_trade_no());
        viewHoler.tvshopping_money.setText(cart.getTotal_money());
        viewHoler.tv_qty.setText(cart.getCount()+"");

       //
        return convertView;
    }
    private class ViewHoler{
        TextView tv_alipay_trading_num;
        TextView tvshopping_money;
        TextView tv_paytime;
        TextView tv_transact_state;
        TextView tv_qty;
        CustomListView lv_recode;
    }

}
