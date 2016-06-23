package com.gkzxhn.gkprison.userport.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.activity.NewsDetailActivity;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReplyPublicityFragment extends Fragment {
    private ListView reply_list;
    private MyAdapter adapter;
    private String url = "";
    private int jail_id;
    private SharedPreferences sp;
    private TextView nonotice;
    private List<News> allnews = new ArrayList<>();
    private List<News> replys = new ArrayList<News>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String tag = (String)msg.obj;
                    if (tag.equals("success")){
                        Bundle bundle = msg.getData();
                        String result = bundle.getString("result");
                        allnews = analysisNews(result);
                        for (int i = 0;i < allnews.size();i++){
                            News news = allnews.get(i);
                            if (news.getType_id() == 3){
                                replys.add(news);
                            }
                        }
                        if (replys.size() == 0){
                            nonotice.setVisibility(View.VISIBLE);
                        }else {
                            nonotice.setVisibility(View.GONE);
                        }
                        reply_list.setAdapter(adapter);
                    }else if (tag.equals("error")){
                        Toast.makeText(getActivity().getApplicationContext(), "同步数据失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.fragment_reply_publicity,null);
        reply_list = (ListView)view.findViewById(R.id.reply_list);
        nonotice = (TextView)view.findViewById(R.id.tv_nothing);
        adapter = new MyAdapter();
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        jail_id = sp.getInt("jail_id",0);
        url = Constants.URL_HEAD + "news?jail_id="+jail_id;
        getNews();
        initdate();
        return view;
    }

    /**
     * items点击事件
     */
    private void initdate() {
        reply_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = replys.get(position).getId();
                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
                intent.putExtra("type", 1);// 0是轮播图   1是新闻
                intent.putExtra("id", i);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("ReplyPublicityFragment"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ReplyPublicityFragment");
    }

    private void getNews() {
        if(Utils.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    Message msg = handler.obtainMessage();
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet Get = new HttpGet(url);
                    try {
                        HttpResponse response = httpClient.execute(Get);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                            msg.obj = "success";
                            Bundle bundle = new Bundle();
                            bundle.putString("result", result);
                            msg.setData(bundle);
                            msg.what = 1;
                            handler.sendMessage(msg);
                        } else {
                            msg.obj = "error";
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }else {
           Toast.makeText(getActivity().getApplicationContext(),"没有网络",Toast.LENGTH_SHORT).show();
        }
    }


    private List<News> analysisNews(String s){
        List<News> newses = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0;i < jsonArray.length();i++){
                News news = new News();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                news.setId(jsonObject.getInt("id"));
                news.setTitle(jsonObject.getString("title"));
                news.setContents(jsonObject.getString("contents"));
                news.setJail_id(jsonObject.getInt("jail_id"));
                news.setImage_url(jsonObject.getString("image_url"));
                news.setType_id(jsonObject.getInt("type_id"));
                newses.add(news);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newses;
    }

 private class MyAdapter extends BaseAdapter{

     @Override
     public int getCount() {
         return replys.size();
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
         if (convertView == null){
             convertView = View.inflate(getActivity(),R.layout.laws_regulations_item,null);
             holder = new ViewHolder();
             holder.textView = (TextView)convertView.findViewById(R.id.tv_laws_regulations_item);
             convertView.setTag(holder);
         }else {
             holder = (ViewHolder)convertView.getTag();
         }
         holder.textView.setText(replys.get(position).getTitle());
         return convertView;
     }
 }
    private  static class ViewHolder{
        TextView textView;
    }

}
