package com.gkzxhn.gkprison.userport.activity;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.keda.sky.app.PcAppStackManager;

public class RemittanceWaysActivity extends BaseActivity {

    private ListView lv_remittance_way;
    private String[] remittance_ways = {"银行卡支付", "支付宝支付", "微信支付"};
    private int[] remittance_way_icons = {R.drawable.pay_way_bank_card, R.drawable.pay_way_zhifubao, R.drawable.pay_way_weixin};
    private boolean[] ischeckeds = {true, false, false, false};
    private MyAdapter myAdapter;
    private Button bt_next;
    private String money;
    private String times;
    private String TradeNo;

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(this, R.layout.activity_remittance_ways, null);
        lv_remittance_way = (ListView) view.findViewById(R.id.lv_remittance_way);
        bt_next = (Button) view.findViewById(R.id.bt_next);
        return view;
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    @Override
    protected void initData() {
        setTitle("汇款");
        setBackVisibility(View.VISIBLE);
        money = getIntent().getStringExtra("money");
        times = getIntent().getStringExtra("times");
        TradeNo = getIntent().getStringExtra("TradeNo");
        myAdapter = new MyAdapter();
        lv_remittance_way.setAdapter(myAdapter);
        lv_remittance_way.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < ischeckeds.length; i++) {
                    if (i == position) {
                        ischeckeds[i] = true;
                    } else {
                        ischeckeds[i] = false;
                    }
                }
                myAdapter.notifyDataSetChanged();
            }
        });
        money = getIntent().getStringExtra("money");
        bt_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.bt_next:
                if (ischeckeds[0] == true){

                    Intent intent = new Intent(RemittanceWaysActivity.this,BankPayActivity.class);
                    RemittanceWaysActivity.this.startActivity(intent);
                }else if (ischeckeds[1] == true){
                    Intent intent = new Intent(RemittanceWaysActivity.this,ZhifubaoPayActivity.class);
                    intent.putExtra("price",money);
                    intent.putExtra("outorderno",TradeNo);
                    intent.putExtra("times",times);
                    RemittanceWaysActivity.this.startActivity(intent);
                }else if (ischeckeds[2] == true){

                    Intent intent = new Intent(RemittanceWaysActivity.this,WeixinPayActivity.class);
                    RemittanceWaysActivity.this.startActivity(intent);
                }
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return remittance_way_icons.length;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(RemittanceWaysActivity.this, R.layout.remittance_way_item, null);
                holder = new ViewHolder();
                holder.iv_remittance_way_icon = (ImageView) convertView.findViewById(R.id.iv_remittance_way_icon);
                holder.tv_remittance_way = (TextView) convertView.findViewById(R.id.tv_remittance_way);
                holder.cb_remittance_way = (CheckBox) convertView.findViewById(R.id.cb_remittance_way);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iv_remittance_way_icon.setImageResource(remittance_way_icons[position]);
            holder.tv_remittance_way.setText(remittance_ways[position]);
            holder.cb_remittance_way.setChecked(ischeckeds[position]);
            return convertView;
        }
    }

    private static class ViewHolder{
        ImageView iv_remittance_way_icon;
        TextView tv_remittance_way;
        CheckBox cb_remittance_way;
    }
}
