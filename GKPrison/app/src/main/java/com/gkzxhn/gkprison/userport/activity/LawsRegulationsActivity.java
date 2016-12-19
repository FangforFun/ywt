package com.gkzxhn.gkprison.userport.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.base.BaseActivity;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.Laws;
import com.gkzxhn.gkprison.utils.Log;
import com.keda.sky.app.PcAppStackManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 法律法规页面
 */
public class LawsRegulationsActivity extends BaseActivity {

    private ListView lv_laws_regulations;
    private List<Laws> lawses = new ArrayList<>();
    private String url = "";
    private ProgressDialog dialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String message = (String) msg.obj;
                    if (message.equals("success")) {
                        Bundle bundle = msg.getData();
                        String laws = bundle.getString("result");
                        lawses = analysisLaws(laws);
                        lv_laws_regulations.setAdapter(new MyAdapter());
                        if(dialog != null && dialog.isShowing())
                            dialog.dismiss();
                    } else if (message.equals("error")) {
                        Toast.makeText(getApplicationContext(), "获取数据失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        if(dialog != null && dialog.isShowing())
                            dialog.dismiss();
                    }
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        PcAppStackManager.Instance().pushActivity(this);
        View view = View.inflate(getApplicationContext(), R.layout.activity_laws_regulations, null);
        lv_laws_regulations = (ListView) view.findViewById(R.id.lv_laws_regulations);
        return view;
    }

    @Override
    protected void initData() {
        setTitle("法律法规");
        setBackVisibility(View.VISIBLE);
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        String token = sp.getString("token", "00");
        int jail_id = sp.getInt("jail_id", 0);
        url = Constants.URL_HEAD + "laws?jail_id=" + jail_id + "&access_token=" + token;
        getLaws();
        lv_laws_regulations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = lawses.get(position).getId();
                Intent intent = new Intent(LawsRegulationsActivity.this, LawsDetailActivity.class);
                intent.putExtra("id", i);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        PcAppStackManager.Instance().popActivity(this, false);
        super.onDestroy();
    }


    /**
     * 获取法律法规
     */
    private void getLaws() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = handler.obtainMessage();
                msg.obj = "error";
                msg.what = 1;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msg = handler.obtainMessage();
                msg.obj = "success";
                Bundle bundle = new Bundle();
                String result = response.body().string();
                bundle.putString("result", result);
                Log.i("request result is ", result);
                msg.setData(bundle);
                msg.what = 1;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 解析
     * @param s
     * @return
     */
    private List<Laws> analysisLaws(String s) {
        List<Laws> lawsList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray1 = jsonObject.getJSONArray("laws");
            for (int i = 0; i < jsonArray1.length(); i++) {
                Laws laws = new Laws();
                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                laws.setId(jsonObject1.getInt("id"));
                laws.setTitle(jsonObject1.getString("title"));
                laws.setContents(jsonObject1.getString("contents"));
                laws.setJail_id(jsonObject1.getInt("jail_id"));
                laws.setCreated_at(jsonObject1.getString("created_at"));
                laws.setUpdated_at(jsonObject1.getString("updated_at"));
                lawsList.add(laws);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lawsList;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lawses.size();
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
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.laws_regulations_item, null);
                holder = new ViewHolder();
                holder.tv_laws_regulations_item = (TextView) convertView.findViewById(R.id.tv_laws_regulations_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_laws_regulations_item.setText(Html.fromHtml(lawses.get(position).getTitle()));
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView tv_laws_regulations_item;
    }
}
