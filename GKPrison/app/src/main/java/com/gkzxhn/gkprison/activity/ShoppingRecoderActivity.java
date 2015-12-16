package com.gkzxhn.gkprison.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

import java.util.ArrayList;
import java.util.List;

public class ShoppingRecoderActivity extends BaseActivity {
    private ListView lv_shoppingrecoder;
    private ShoppingAdapter adapter;
    private List<String> money_count = new ArrayList<String>(){
        {
            add("¥ 59.80");
            add("¥ 59.80");
        }
    };
    private List<String> recodertime = new ArrayList<String>(){
        {
            add("2015年5月4日");
            add("2015年6月3日");
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_shopping_recoder,null);
        lv_shoppingrecoder = (ListView)view.findViewById(R.id.lv_shopping_recode);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("购物记录");
        setBackVisibility(View.VISIBLE);
        adapter = new ShoppingAdapter();
        lv_shoppingrecoder.setAdapter(adapter);
    }
    private class ShoppingAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return money_count.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          ViewHoler viewHoler;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.shoppingrecode_item,null);
                viewHoler = new ViewHoler();
                viewHoler.time = (TextView)convertView.findViewById(R.id.tv_paytime);
                viewHoler.tv_money = (TextView)convertView.findViewById(R.id.tvshopping_money);
                convertView.setTag(viewHoler);
            }else {
                viewHoler = (ViewHoler)convertView.getTag();
            }
            viewHoler.time.setText(recodertime.get(position));
            viewHoler.tv_money.setText(money_count.get(position));
            return convertView;
        }
        private class ViewHoler{
            TextView tv_money;
            TextView time;
        }
    }
}
