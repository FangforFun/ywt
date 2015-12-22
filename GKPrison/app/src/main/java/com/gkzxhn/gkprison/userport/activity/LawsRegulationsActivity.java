package com.gkzxhn.gkprison.userport.activity;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;

/**
 * 法律法规页面
 */
public class LawsRegulationsActivity extends BaseActivity {

    private ListView lv_laws_regulations;
    private final int[] ITEM_IVS = {R.drawable.laws_item_01, R.drawable.laws_item_02, R.drawable.laws_item_03, R.drawable.laws_item_04, R.drawable.laws_item_05};
    private final String[] ITEM_TVS = {"关于进一步深化狱务公开的意见", "中华人民共和国赔偿法", "中华人民共和国劳动法", "中华人民共和国职业病防治法", "中华人民共和国刑事诉讼法"};

    @Override
    protected View initView() {
        View view = View.inflate(getApplicationContext(), R.layout.activity_laws_regulations, null);
        lv_laws_regulations = (ListView) view.findViewById(R.id.lv_laws_regulations);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("法律法规");
        setBackVisibility(View.VISIBLE);
        lv_laws_regulations.setAdapter(new MyAdapter());
        lv_laws_regulations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showToastMsgShort("哈哈哈" + position);
            }
        });
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return ITEM_IVS.length;
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
            ViewHolder holder;
            if(convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.laws_regulations_item, null);
                holder = new ViewHolder();
                holder.tv_laws_regulations_item = (TextView) convertView.findViewById(R.id.tv_laws_regulations_item);
                holder.iv_to_right = (ImageView) convertView.findViewById(R.id.iv_to_right);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            if(position == 2){
                Drawable drawable = getResources().getDrawable(ITEM_IVS[position]);
                drawable.setBounds(0, 0, 60, 80);
                holder.tv_laws_regulations_item.setCompoundDrawables(drawable, null, null, null);
            }else {
                Drawable drawable = getResources().getDrawable(ITEM_IVS[position]);
                drawable.setBounds(0, 0, 60, 70);
                holder.tv_laws_regulations_item.setCompoundDrawables(drawable, null, null, null);
            }
            holder.tv_laws_regulations_item.setText(ITEM_TVS[position]);
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView tv_laws_regulations_item;
        ImageView iv_to_right;
    }
}
