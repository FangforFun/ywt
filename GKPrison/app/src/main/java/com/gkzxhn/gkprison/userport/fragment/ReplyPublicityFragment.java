package com.gkzxhn.gkprison.userport.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.userport.bean.News;
import com.gkzxhn.gkprison.utils.Utils;

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
        adapter = new MyAdapter();
        url = Constants.URL_HEAD + "news?jail_id=1";
        getNews();
        return view;
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
         return 0;
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
         return null;
     }
 }


}
