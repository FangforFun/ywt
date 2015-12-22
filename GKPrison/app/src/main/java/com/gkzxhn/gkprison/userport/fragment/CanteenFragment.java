package com.gkzxhn.gkprison.userport.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.userport.activity.PaymentActivity;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.event.ClickEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by zhengneng on 2015/12/21.
 */
public class CanteenFragment extends BaseFragment {

    private SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.gkzxhn.gkprison/files/chaoshi.db", null, SQLiteDatabase.OPEN_READWRITE);
    private RelativeLayout rl_allclass;
    private RelativeLayout rl_sales;
    private RelativeLayout rl_zhineng;
    private float total ;
    private String send;
    private Button settlement;
    private TextView tv_allclass;
    private TextView tv_sales;
    private TextView tv_zhineng;
    private Spinner sp_allclass;
    private Spinner sp_sales;
    private Spinner sp_zhineng;
    private TextView tv_total_money;
    private List<Commodity> commodities = new ArrayList<Commodity>();
    AllClassificationFragment allclass;
    SalesPriorityFragment sales;
    IntellingentSortingFragment zhineng;

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_canteen, null);
        settlement = (Button)view.findViewById(R.id.bt_shopping_cart_commit);
        rl_allclass = (RelativeLayout)view.findViewById(R.id.rl_allclass);
        rl_sales = (RelativeLayout)view.findViewById(R.id.rl_sales);
        rl_zhineng = (RelativeLayout)view.findViewById(R.id.rl_zhineng);
        tv_allclass = (TextView)view.findViewById(R.id.tv_allclass);
        tv_sales = (TextView)view.findViewById(R.id.tv_sales);
        tv_zhineng = (TextView)view.findViewById(R.id.tv_zhineng);
        sp_allclass = (Spinner)view.findViewById(R.id.sp_allclass);
        sp_sales = (Spinner)view.findViewById(R.id.sp_sales);
        sp_zhineng = (Spinner)view.findViewById(R.id.sp_zhineng);
        tv_total_money = (TextView)view.findViewById(R.id.tv_total_money);
        return view;
    }

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
        allclass = new AllClassificationFragment();
        tv_allclass.setTextColor(Color.parseColor("#6495ed"));
        sp_allclass.setBackgroundResource(R.drawable.spinner_down);
        ((BaseActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity,allclass).commit();
        rl_allclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_allclass.setTextColor(Color.parseColor("#6495ed"));
                tv_sales.setTextColor(context.getResources().getColor(R.color.tv_bg));
                tv_zhineng.setTextColor(context.getResources().getColor(R.color.tv_bg));
                sp_allclass.setBackgroundResource(R.drawable.spinner_down);
                sp_sales.setBackgroundResource(R.drawable.spinner);
                sp_zhineng.setBackgroundResource(R.drawable.spinner);
                allclass = new AllClassificationFragment();
                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
            }
        });
        rl_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_sales.setTextColor(Color.parseColor("#6495ed"));
                tv_allclass.setTextColor(Color.parseColor("#333333"));
                tv_zhineng.setTextColor(Color.parseColor("#333333"));
                sp_sales.setBackgroundResource(R.drawable.spinner_down);
                sp_allclass.setBackgroundResource(R.drawable.spinner);
                sp_zhineng.setBackgroundResource(R.drawable.spinner);
                sales = new SalesPriorityFragment();
                ((BaseActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity,sales).commit();
            }
        });
        rl_zhineng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_zhineng.setTextColor(Color.parseColor("#6495ed"));
                tv_sales.setTextColor(Color.parseColor("#333333"));
                tv_allclass.setTextColor(Color.parseColor("#333333"));
                sp_zhineng.setBackgroundResource(R.drawable.spinner_down);
                sp_sales.setBackgroundResource(R.drawable.spinner);
                sp_allclass.setBackgroundResource(R.drawable.spinner);
                zhineng = new IntellingentSortingFragment();
                ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, zhineng).commit();
            }
        });
        settlement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PaymentActivity.class);
                intent.putExtra("totalmoney",send);
                context.startActivity(intent);
            }
        });
    }

    public View getView(){
        return view;
    }

    public void onEvent(ClickEvent event) {
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        String i = (String)msg.obj;
                        tv_total_money.setText(i);
                        break;
                }
            }
        };
        // 从事件中获得参数值
        Toast.makeText(context, "点我，点我", Toast.LENGTH_SHORT).show();
        commodities.clear();
        String sql = "select distinct * from commodity";
        Cursor cursor = db.rawQuery(sql,null);
        total = 0;
        if (cursor.getCount() == 0){
            tv_total_money.setText("0.0");
        }else {
            while (cursor.moveToNext()){
                Commodity commodity = new Commodity();
                commodity.setId(cursor.getInt(cursor.getColumnIndex("commodity_id")));
                commodity.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                commodity.setNum(cursor.getInt(cursor.getColumnIndex("commodity_num")));
                commodities.add(commodity);
            }
        }
        for (int i = 0;i < commodities.size();i++){
            String t = commodities.get(i).getPrice();
            float p = Float.parseFloat(t);
            int n = commodities.get(i).getNum();
            total += p * n ;
        }
        DecimalFormat fnum = new DecimalFormat("####0.0");
        send = fnum.format(total);
        Message msg = handler.obtainMessage();
        msg.obj = send;
        msg.what = 1;
        handler.sendMessage(msg);
    }
}
