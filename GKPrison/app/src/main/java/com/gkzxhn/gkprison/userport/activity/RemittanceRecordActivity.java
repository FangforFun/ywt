package com.gkzxhn.gkprison.userport.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class RemittanceRecordActivity extends BaseActivity {
    private ListView lv_remittance;
    private RemittanceAdapter adapter;
    private List<String> paytime = new ArrayList<String>(){
        {
            add("2016年4月18日");
            add("2016年6月19日");
            add("2016年8月20日");
        }
    };
    private List<String> payment = new ArrayList<String>(){
        {
            add("支付宝");
            add("银行卡支付");
            add("微信支付");
        }
    };
    private List<String> money = new ArrayList<String>(){
        {
            add("1230.6");
            add("164.5");
            add("98.3");
        }
    };
    private List<String> prisonernum = new ArrayList<String>(){
        {
            add("囚号9527");
            add("囚号342133444");
            add("囚号813147");
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_remittance,null);
        lv_remittance = (ListView)view.findViewById(R.id.lv_remittance);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("汇款记录");
        setBackVisibility(View.VISIBLE);
        adapter = new RemittanceAdapter();
        lv_remittance.setAdapter(adapter);
    }
    private class RemittanceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return payment.size();
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
           ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(getApplicationContext(),R.layout.remittance_item,null);
                viewHolder = new ViewHolder();
                viewHolder.tv_paytime = (TextView)convertView.findViewById(R.id.tv_paytime);
                viewHolder.tv_payment = (TextView)convertView.findViewById(R.id.tv_transtype);
                viewHolder.tv_money = (TextView)convertView.findViewById(R.id.tv_pay_money);
                viewHolder.tv_prisonnernum = (TextView)convertView.findViewById(R.id.tv_prisonnernum);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.tv_paytime.setText(paytime.get(position));
            viewHolder.tv_payment.setText(payment.get(position));
            viewHolder.tv_money.setText(money.get(position));
            viewHolder.tv_prisonnernum.setText(prisonernum.get(position));
            return convertView;
        }
    }
    private class ViewHolder{
        TextView tv_paytime;
        TextView tv_payment;
        TextView tv_money;
        TextView tv_prisonnernum;
    }
}
