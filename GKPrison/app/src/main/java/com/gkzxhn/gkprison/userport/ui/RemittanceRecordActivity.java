package com.gkzxhn.gkprison.userport.ui;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.rx.RxUtils;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.app.utils.SPKeyConstants;
import com.gkzxhn.gkprison.base.BaseActivityNew;
import com.gkzxhn.gkprison.userport.bean.Remittance;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.StringUtils;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.gkzxhn.gkprison.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 汇款记录
 */
public class RemittanceRecordActivity extends BaseActivityNew {

    private static final java.lang.String TAG = "RemittanceRecordActivity";
    @BindView(R.id.lv_remittance) ListView lv_remittance;
    @BindView(R.id.tv_title) TextView tv_title;
    @BindView(R.id.rl_back) RelativeLayout rl_back;
    @BindView(R.id.iv_recode) ImageView iv_recode;

    private RemittanceAdapter adapter;
    private String prisonernum = "";
    private List<Remittance> remittances = new ArrayList<>();
    private  String prisonname;
    private ProgressDialog progressDialog;
    private Subscription querySubscription;

    @Override
    public int setLayoutResId() {
        return R.layout.activity_remittance;
    }

    @Override
    protected void initUiAndListener() {
        ButterKnife.bind(this);
        tv_title.setText(getString(R.string.remittance_record));
        rl_back.setVisibility(View.VISIBLE);
        prisonernum = (String) getSPValue(SPKeyConstants.PRISONER_NUMBER, "1");
        prisonname = (String) getSPValue(SPKeyConstants.PRISON_NAME,"");
        queryRecordFromDB();
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return true;
    }

    @Override
    protected boolean isApplyTranslucentStatus() {
        return true;
    }

    @OnClick(R.id.rl_back)
    public void onClick(){
        finish();
    }

    @Override
    protected void onDestroy() {
        UIUtils.dismissProgressDialog(progressDialog);
        RxUtils.unSubscribe(querySubscription);
        super.onDestroy();
    }

    /**
     * 从数据库中查询记录
     */
    private void queryRecordFromDB() {
        progressDialog = UIUtils.showProgressDialog(this);
        querySubscription = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                String sql = "select distinct Cart.id,Cart.time,Cart.total_money,Cart.payment_type from Cart,line_items where line_items.Items_id = 9999 and Cart.isfinish = 1 and remittance = 1";
                Cursor cursor = StringUtils.getSQLiteDB(RemittanceRecordActivity.this).rawQuery(sql,null);
                while (cursor.moveToNext()){
                    Remittance remittance = new Remittance();
                    remittance.setPrice(cursor.getString(cursor.getColumnIndex("total_money")));
                    remittance.setTimes(cursor.getString(cursor.getColumnIndex("time")));
                    remittance.setCart_id(cursor.getInt(cursor.getColumnIndex("id")));
                    remittance.setPayment_type(cursor.getString(cursor.getColumnIndex("payment_type")));
                    remittances.add(remittance);
                }
                cursor.close();
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
        }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<Object>(){
            @Override public void onError(Throwable e) {
                Log.e(TAG, "query record failed: " + e.getMessage());
                UIUtils.dismissProgressDialog(progressDialog);
                ToastUtil.showShortToast(getString(R.string.query_failed));
            }

            @Override public void onNext(Object o) {
                UIUtils.dismissProgressDialog(progressDialog);
                iv_recode.setVisibility(remittances.size() == 0 ? View.VISIBLE : View.GONE);
                adapter = new RemittanceAdapter();
                lv_remittance.setAdapter(adapter);
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
