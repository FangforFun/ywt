package com.gkzxhn.gkprison.userport.fragment;


import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class
        IntellingentSortingFragment extends BaseFragment {
    private ListView lv_intelling;
    private SalesAdapter adapter;
    private List<Integer> image = new ArrayList<Integer>(){
        {
            add(R.drawable.beizi1);
            add(R.drawable.beizi2);
            add(R.drawable.beizi3);
            add(R.drawable.beizi4);
        }
    };
    private List<String> description = new ArrayList<String>(){
        {
            add("天禧玻璃杯带过滤便携男女士茶杯创意可爱运动情侣透明水杯杯子");
            add("潮牌易拉罐学生保温不锈钢吸管水杯大肚杯创意随行随手情侣杯子");
            add("天禧玻璃杯带过滤便携男女士茶杯创意可爱运动情侣透明水杯杯子");
            add("天禧玻璃杯带过滤便携男女士茶杯创意可爱运动情侣透明水杯杯子");
        }
    };

    private List<String> money = new ArrayList<String>(){
        {
            add("29.8");
            add("41.3");
            add("38.9");
            add("24.1");
        }
    };

    @Override
    protected View initView() {
        view = View.inflate(context,R.layout.fragment_intellingent_sorting,null);
        lv_intelling = (ListView)view.findViewById(R.id.lv_intllingent);
        return view;
    }

    @Override
    protected void initData() {
        adapter = new SalesAdapter();
        lv_intelling.setAdapter(adapter);
    }

    private class SalesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return image.size();
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
                convertView = View.inflate(getActivity(),R.layout.sales_item,null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView)convertView.findViewById(R.id.image_commodity);
                viewHolder.tv_description = (TextView)convertView.findViewById(R.id.tv_description);
                viewHolder.tv_money = (TextView)convertView.findViewById(R.id.tv_money);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.imageView.setImageResource(image.get(position));
            viewHolder.tv_description.setText(description.get(position));
            viewHolder.tv_money.setText(money.get(position));
            return convertView;
        }
    }
    private class ViewHolder{
        ImageView imageView;
        TextView  tv_description;
        TextView  tv_money;

    }
}
