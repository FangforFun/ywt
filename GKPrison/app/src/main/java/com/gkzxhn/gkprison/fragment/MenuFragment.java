package com.gkzxhn.gkprison.fragment;


import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.activity.ApplyBarcodeActivity;
import com.gkzxhn.gkprison.activity.RemittanceRecordActivity;
import com.gkzxhn.gkprison.activity.SettingActivity;
import com.gkzxhn.gkprison.activity.ShoppingRecoderActivity;
import com.gkzxhn.gkprison.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.activity.UserInfoActivity;

/**
 * 左侧侧滑菜单fragment
 */
public class MenuFragment extends BaseFragment {

    private ListView lv_home_menu;
    private final String[] menu_options_tv = {"申请二维码", "个人信息", "我的银行卡", "汇款记录", "购物记录", "系统消息", "设置"};
    private final int[] menu_options_iv = {R.drawable.apply_barcode, R.drawable.user_info, R.drawable.my_bc, R.drawable.remittance_record, R.drawable.shopping_record, R.drawable.system_msg, R.drawable.setting};

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_menu, null);
        lv_home_menu = (ListView) view.findViewById(R.id.lv_home_menu);
        return view;
    }

    @Override
    protected void initData() {
        lv_home_menu.setAdapter(new MenuAdapter());
        lv_home_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView menu_option_tv = (TextView) view.findViewById(R.id.tv_menu_option);
//                showToastMsgShort(menu_option_tv.getText().toString());
                Intent intent;
                switch (position){
                    case 0:
                        intent = new Intent(context, ApplyBarcodeActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(context, UserInfoActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(context, RemittanceRecordActivity.class);
                        context.startActivity(intent);
                        break;
                    case 4:
                        intent = new Intent(context, ShoppingRecoderActivity.class);
                        context.startActivity(intent);
                        break;
                    case 5:
                        intent = new Intent(context, SystemMessageActivity.class);
                        startActivity(intent);
                        break;
                    case 6:
                        intent = new Intent(context, SettingActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private class MenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return menu_options_tv.length;
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
            MenuViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(context, R.layout.home_menu_item, null);
                holder = new MenuViewHolder();
                holder.iv_menu_option = (ImageView) convertView.findViewById(R.id.iv_menu_option);
                holder.tv_menu_option = (TextView) convertView.findViewById(R.id.tv_menu_option);
                convertView.setTag(holder);
            }else {
                holder = (MenuViewHolder) convertView.getTag();
            }
            holder.iv_menu_option.setImageResource(menu_options_iv[position]);
            holder.tv_menu_option.setText(menu_options_tv[position]);
            return convertView;
        }
    }

    private static class MenuViewHolder{
        ImageView iv_menu_option;
        TextView tv_menu_option;
    }
}
