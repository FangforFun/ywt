package com.gkzxhn.gkprison.userport.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseFragment;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.login.LoadingActivity;
import com.gkzxhn.gkprison.userport.activity.RemittanceRecordActivity;
import com.gkzxhn.gkprison.userport.activity.SettingActivity;
import com.gkzxhn.gkprison.userport.activity.ShoppingRecoderActivity;
import com.gkzxhn.gkprison.userport.activity.SystemMessageActivity;
import com.gkzxhn.gkprison.userport.activity.UserInfoActivity;
import com.lidroid.xutils.BitmapUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;

/**
 * 左侧侧滑菜单fragment
 */
public class MenuFragment extends BaseFragment{

    private ListView lv_home_menu;
    private final String[] menu_options_tv = {"个人信息", "汇款记录", "购物记录", "设置"};
    private final int[] menu_options_iv = {R.drawable.user_info, R.drawable.remittance_record, R.drawable.shopping_record, R.drawable.setting};
    private SharedPreferences sp;
    private boolean isRegisteredUser;
    private RelativeLayout rl_header_info;
    private ImageView iv_user_icon;
    private TextView tv_menu_user_name;
    private RelativeLayout ll_root;
    private Button bt_logout;
    private BitmapUtils bitmapUtils;

    @Override
    protected View initView() {
        view = View.inflate(context, R.layout.fragment_menu, null);
        lv_home_menu = (ListView) view.findViewById(R.id.lv_home_menu);
        rl_header_info = (RelativeLayout) view.findViewById(R.id.rl_header_info);
        iv_user_icon = (ImageView) view.findViewById(R.id.iv_user_icon);
        tv_menu_user_name = (TextView) view.findViewById(R.id.tv_menu_user_name);
        ll_root = (RelativeLayout) view.findViewById(R.id.ll_root);
        bt_logout = (Button) view.findViewById(R.id.bt_logout);
        return view;
    }

    @Override
    protected void initData() {
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        isRegisteredUser = sp.getBoolean("isRegisteredUser", false);
        if(!isRegisteredUser){
            iv_user_icon.setImageResource(R.drawable.default_icon);
            tv_menu_user_name.setText("用户名");
        }else {
            tv_menu_user_name.setText(sp.getString("name", ""));
            String ICON_URL = sp.getString("avatar", "");
            if(!TextUtils.isEmpty(ICON_URL)){
                bitmapUtils = new BitmapUtils(context);
                bitmapUtils.display(iv_user_icon, "http://10.93.1.10:3000" + ICON_URL);
            }
        }
        lv_home_menu.setAdapter(new MenuAdapter());
        lv_home_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case 0:
                        if (isRegisteredUser) {
                            intent = new Intent(context, UserInfoActivity.class);
                            startActivity(intent);
                        } else {
                            showToastMsgShort("注册后可用");
                        }
                        break;
                    case 1:
                        if (isRegisteredUser) {
                            intent = new Intent(context, RemittanceRecordActivity.class);
                            context.startActivity(intent);
                        } else {
                            showToastMsgShort("注册后可用");
                        }
                        break;
                    case 2:
                        if (isRegisteredUser) {
                            intent = new Intent(context, ShoppingRecoderActivity.class);
                            context.startActivity(intent);
                        } else {
                            showToastMsgShort("注册后可用");
                        }
                        break;
                    case 3:
                        intent = new Intent(context, SettingActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
        rl_header_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 无响应
            }
        });
        ll_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 无响应
            }
        });
        bt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("确定退出当前账号?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, LoadingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.commit();
                        startActivity(intent);
                        NIMClient.getService(AuthService.class).logout();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    /**
     * 返回view视图
     * @return
     */
    public View getContentView(){
        if(view != null) {
            return view;
        }else {
            return null;
        }
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
