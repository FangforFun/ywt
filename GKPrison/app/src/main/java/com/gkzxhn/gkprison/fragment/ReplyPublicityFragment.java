package com.gkzxhn.gkprison.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReplyPublicityFragment extends Fragment {
    private ListView lv_reply;
    private MyAdapter adapter;
    private List<String> openmessge = new ArrayList<String>(){
        {
            add("9月份局长信箱、网上信访答复情况公示");
            add("8月份局长信箱、网上信访答复情况公示");
            add("7月份局长信箱、网上信访答复情况公示");
            add("6月份局长信箱、网上信访答复情况公示");
            add("5月份局长信箱、网上信访答复情况公示");
            add("4月份局长信箱、网上信访答复情况公示");
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
        lv_reply = (ListView)view.findViewById(R.id.lv_prison_warden);
        adapter = new MyAdapter();
        lv_reply.setAdapter(adapter);
        return view;
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return openmessge.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(getActivity(),R.layout.prison_warden_item,null);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView)convertView.findViewById(R.id.tv_openmessge);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.textView.setText(openmessge.get(position));
            return convertView;
        }

        private class ViewHolder{
            TextView textView;
        }
    }

}
