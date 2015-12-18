package com.gkzxhn.gkprison.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物记录
 */
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
                viewHoler.tv_paytime = (TextView)convertView.findViewById(R.id.tv_paytime);
                viewHoler.tvshopping_money = (TextView)convertView.findViewById(R.id.tvshopping_money);
                viewHoler.tv_alipay_trading_num = (TextView) convertView.findViewById(R.id.tv_alipay_trading_num);
                viewHoler.tv_transact_state = (TextView) convertView.findViewById(R.id.tv_transact_state);
                viewHoler.bt_shopping_record_operate = (Button) convertView.findViewById(R.id.bt_shopping_record_operate);
                convertView.setTag(viewHoler);
            }else {
                viewHoler = (ViewHoler)convertView.getTag();
            }
            viewHoler.tv_paytime.setText(recodertime.get(position));
            viewHoler.tvshopping_money.setText(money_count.get(position));
            viewHoler.bt_shopping_record_operate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToastMsgShort("申请退款...");
                }
            });
            return convertView;
        }
        private class ViewHoler{
            TextView tv_alipay_trading_num;
            TextView tvshopping_money;
            TextView tv_paytime;
            TextView tv_transact_state;
            Button bt_shopping_record_operate;
        }
    }
}
