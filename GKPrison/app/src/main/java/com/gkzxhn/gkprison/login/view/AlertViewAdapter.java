package com.gkzxhn.gkprison.login.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by huangzhengneng on 2016.5.31.
 */
public class AlertViewAdapter extends BaseAdapter{
    private List<String> mDatas;
    private List<String> mDestructive;
    public AlertViewAdapter(List<String> datas,List<String> destructive){
        this.mDatas =datas;
        this.mDestructive =destructive;
    }
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String data= mDatas.get(position);
        Holder holder=null;
        View view =convertView;
        if(view==null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view=inflater.inflate(com.bigkoo.alertview.R.layout.item_alertbutton, null);
            holder=creatHolder(view);
            view.setTag(holder);
        }
        else{
            holder=(Holder) view.getTag();
        }
        holder.UpdateUI(parent.getContext(),data,position);
        return view;
    }
    public Holder creatHolder(View view){
        return new Holder(view);
    }
    class Holder {
        private TextView tvAlert;

        public Holder(View view){
            tvAlert = (TextView) view.findViewById(com.bigkoo.alertview.R.id.tvAlert);
        }
        public void UpdateUI(Context context,String data,int position){
            tvAlert.setText(data);
            if (mDestructive!= null && mDestructive.contains(data)){
                tvAlert.setTextColor(context.getResources().getColor(com.bigkoo.alertview.R.color.textColor_alert_button_destructive));
            }
            else{
                tvAlert.setTextColor(context.getResources().getColor(com.bigkoo.alertview.R.color.textColor_alert_button_others));
            }
        }
    }
}