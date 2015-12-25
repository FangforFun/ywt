package com.gkzxhn.gkprison.userport.fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.userport.activity.PaymentActivity;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.userport.bean.Commodity;
import com.gkzxhn.gkprison.userport.bean.Shoppinglist;
import com.gkzxhn.gkprison.userport.event.ClickEvent;
import com.google.gson.Gson;
import com.readystatesoftware.viewbadger.BadgeView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private Gson gson;
    private String url = "http://10.93.1.10:3000/api/v1/apply";
    private Spinner sp_sales;
    private Spinner sp_zhineng;
    private TextView tv_total_money;
    private Fragment fragment;
    private List<Shoppinglist> commodities = new ArrayList<Shoppinglist>();
    AllClassificationFragment allclass;
    SalesPriorityFragment sales;
    IntellingentSortingFragment zhineng;
    private FragmentManager fm;
    private Bundle data;


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
        long time = System.currentTimeMillis();
       fm  = ((BaseActivity) context).getSupportFragmentManager();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String times = format.format(date);
        String sql = "insert into Cart (time) values ('"+times+"')";
        db.execSQL(sql);
        data = new Bundle();
        data.putString("times",times);
        showFragment(1);
        View image_buycar = view.findViewById(R.id.image_buycar);
        EventBus.getDefault().register(this);

        tv_allclass.setTextColor(Color.parseColor("#6495ed"));
        sp_allclass.setBackgroundResource(R.drawable.spinner_down);

        rl_allclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_allclass.setTextColor(Color.parseColor("#6495ed"));
                tv_sales.setTextColor(context.getResources().getColor(R.color.tv_bg));
                tv_zhineng.setTextColor(context.getResources().getColor(R.color.tv_bg));
                sp_allclass.setBackgroundResource(R.drawable.spinner_down);
                sp_sales.setBackgroundResource(R.drawable.spinner);
                sp_zhineng.setBackgroundResource(R.drawable.spinner);
            //    allclass = new AllClassificationFragment();
          //      allclass.setArguments(data);
               // ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, allclass).commit();
               showFragment(1);
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
               // sales = new SalesPriorityFragment();
                //((BaseActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity,sales).commit();
              showFragment(2);
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
              //  zhineng = new IntellingentSortingFragment();
              //  ((BaseActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fl_commodity, zhineng).commit();
              showFragment(3);
            }
        });
        settlement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gson = new Gson();
                final String str = gson.toJson(commodities);
                final String countmoney = tv_total_money.getText().toString();
                new Thread(){
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost post = new HttpPost(url);
                        List<NameValuePair> values = new ArrayList<NameValuePair>();
                        BasicNameValuePair value1 = new BasicNameValuePair("countmoney",countmoney);
                        BasicNameValuePair value2 = new BasicNameValuePair("shoppinglist",str);
                        values.add(value1);
                        values.add(value2);
                        try {
                            HttpEntity entity = new UrlEncodedFormEntity(values,"utf-8");
                            post.setEntity(entity);
                            HttpResponse httpResponse = httpClient.execute(post);
                            if (httpResponse.getStatusLine().getStatusCode() == 200){

                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                Intent intent = new Intent(context, PaymentActivity.class);
                intent.putExtra("totalmoney",send);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        String sql = "select distinct line_items.Items_id,line_items.qty,Items.price from line_items,Items,Cart where line_items.Items_id = Items.id and line_items.cart_id = Cart.id";
        Cursor cursor = db.rawQuery(sql,null);
        total = 0;
        if (cursor.getCount() == 0){
            tv_total_money.setText("0.0");
        }else {
            while (cursor.moveToNext()){
                Shoppinglist shoppinglist = new Shoppinglist();
                shoppinglist.setId(cursor.getInt(cursor.getColumnIndex("Items_id")));
                shoppinglist.setPrice(cursor.getString(cursor.getColumnIndex("price")));
                shoppinglist.setQty(cursor.getInt(cursor.getColumnIndex("qty")));
                commodities.add(shoppinglist);
            }

        }
        for (int i = 0;i < commodities.size();i++){
            String t = commodities.get(i).getPrice();
            float p = Float.parseFloat(t);
            int n = commodities.get(i).getQty();
            total += p * n ;
        }
        DecimalFormat fnum = new DecimalFormat("####0.0");
        send = fnum.format(total);
        Message msg = handler.obtainMessage();
        msg.obj = send;
        msg.what = 1;
        handler.sendMessage(msg);
    }

    //当fragment已经被实例化，将其隐藏
    public  void hideFragment(FragmentTransaction ft){
        if (allclass != null){
            ft.hide(allclass);
        }
        if (sales != null){
            ft.hide(sales);
        }
        if (zhineng != null){
            ft.hide(zhineng);
        }
    }

    public void showFragment(int index){
        FragmentTransaction ft = fm.beginTransaction();
        hideFragment(ft);
        switch (index){
            case 1:
                if (allclass != null){
                    ft.show(allclass);
                }else {
                    allclass = new AllClassificationFragment();
                    allclass.setArguments(data);
                    ft.add(R.id.fl_commodity,allclass);
                }
                break;
            case 2:
                if (sales != null){
                    ft.show(sales);
                }else {
                    sales = new SalesPriorityFragment();
                    sales.setArguments(data);
                    ft.add(R.id.fl_commodity,sales);
                }
                break;
            case 3:
                if (zhineng != null){
                    ft.show(zhineng);
                }else {
                    zhineng = new IntellingentSortingFragment();
                    zhineng.setArguments(data);
                    ft.add(R.id.fl_commodity,zhineng);
                }
                break;
        }
        ft.commit();
    }

}
