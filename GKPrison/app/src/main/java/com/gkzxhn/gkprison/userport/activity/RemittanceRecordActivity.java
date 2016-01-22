package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.bean.Remittance;

import java.util.ArrayList;
import java.util.List;

public class RemittanceRecordActivity extends BaseActivity {
    private ListView lv_remittance;
    private RemittanceAdapter adapter;
    private SharedPreferences sp;
    private String prisonernum = "";
    private List<Remittance> remittances = new ArrayList<Remittance>();
    private TextView noonerecode;
    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);


    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(),R.layout.activity_remittance,null);
        lv_remittance = (ListView)view.findViewById(R.id.lv_remittance);
        noonerecode = (TextView)view.findViewById(R.id.tv_recode);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("汇款记录");
        setBackVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        prisonernum = sp.getString("prisoner_number","1");
        getDate();
        if (remittances.size() == 0){
            noonerecode.setVisibility(View.VISIBLE);
        }else {
            noonerecode.setVisibility(View.GONE);
        }
        adapter = new RemittanceAdapter();
        lv_remittance.setAdapter(adapter);
    }

    private void getDate() {
        String sql = "select distinct Cart.id,Cart.time,Cart.total_money from Cart,line_items where line_items.Items_id = 9999 and Cart.isfinish = 1,remittance = 1";
        Cursor cursor = db.rawQuery(sql,null);
        while (cursor.moveToNext()){
            Remittance remittance = new Remittance();
            remittance.setPrice(cursor.getString(cursor.getColumnIndex("total_money")));
            remittance.setTimes(cursor.getString(cursor.getColumnIndex("time")));
            remittances.add(remittance);
        }
    }

    private class RemittanceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return remittances.size();
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
                viewHolder.tv_payment = (TextView)convertView.findViewById(R.id.tv_transtype_name);
                viewHolder.tv_money = (TextView)convertView.findViewById(R.id.tv_pay_money);
                viewHolder.tv_prisonnernum = (TextView)convertView.findViewById(R.id.tv_prisonnernum);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.tv_paytime.setText(remittances.get(position).getTimes());
            viewHolder.tv_payment.setText("支付宝");
            viewHolder.tv_money.setText(remittances.get(position).getPrice());
            viewHolder.tv_prisonnernum.setText(prisonernum);
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
