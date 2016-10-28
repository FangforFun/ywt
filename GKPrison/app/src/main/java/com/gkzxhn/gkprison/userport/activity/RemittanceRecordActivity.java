package com.gkzxhn.gkprison.userport.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.bean.Remittance;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.keda.sky.app.PcAppStackManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RemittanceRecordActivity extends BaseActivity {
    private ListView lv_remittance;
    private RemittanceAdapter adapter;
    private SharedPreferences sp;
    private String prisonernum = "";
    private List<Remittance> remittances = new ArrayList<Remittance>();
    private ImageView iv_recode;
    private  String prisonname;
    private SQLiteDatabase db = StringUtils.getSQLiteDB(this);


    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(getApplicationContext(),R.layout.activity_remittance,null);
        lv_remittance = (ListView)view.findViewById(R.id.lv_remittance);
        iv_recode = (ImageView) view.findViewById(R.id.iv_recode);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("汇款记录");
        setBackVisibility(View.VISIBLE);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        prisonernum = sp.getString("prisoner_number", "1");
        prisonname = sp.getString("prisonname","德山监狱");
        getDate();
        if (remittances.size() == 0){
            iv_recode.setVisibility(View.VISIBLE);
        }else {
            iv_recode.setVisibility(View.GONE);
        }
        adapter = new RemittanceAdapter();
        lv_remittance.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    private void getDate() {
        remittances.clear();
        String sql = "select distinct Cart.id,Cart.time,Cart.total_money,Cart.payment_type from Cart,line_items where line_items.Items_id = 9999 and Cart.isfinish = 1 and remittance = 1";
        Cursor cursor = db.rawQuery(sql,null);
        while (cursor.moveToNext()){
            Remittance remittance = new Remittance();
            remittance.setPrice(cursor.getString(cursor.getColumnIndex("total_money")));
            remittance.setTimes(cursor.getString(cursor.getColumnIndex("time")));
            remittance.setCart_id(cursor.getInt(cursor.getColumnIndex("id")));
            remittance.setPayment_type(cursor.getString(cursor.getColumnIndex("payment_type")));
            remittances.add(remittance);
        }
        Collections.sort(remittances, new Comparator<Remittance>() {
            @Override
            public int compare(Remittance lhs, Remittance rhs) {
                int heat1 = lhs.getCart_id();
                int heat2 = rhs.getCart_id();
                if (heat1 < heat2){
                    return 1;
                }
                return -1;
            }
        });
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
                viewHolder.tv_prisonname = (TextView)convertView.findViewById(R.id.tv_prison_name);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.tv_paytime.setText(remittances.get(position).getTimes());
            viewHolder.tv_payment.setText(remittances.get(position).getPayment_type());
            viewHolder.tv_money.setText(remittances.get(position).getPrice());
            viewHolder.tv_prisonnernum.setText(prisonernum);
            viewHolder.tv_prisonname.setText(prisonname);
            return convertView;
        }
    }
    private class ViewHolder{
        TextView tv_paytime;
        TextView tv_payment;
        TextView tv_money;
        TextView tv_prisonnernum;
        TextView tv_prisonname;
    }
}
