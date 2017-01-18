package com.gkzxhn.gkprison.prisonport.ui.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;
import com.gkzxhn.gkprison.api.rx.SimpleObserver;
import com.gkzxhn.gkprison.constant.Constants;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;
import com.gkzxhn.gkprison.prisonport.requests.ApiService;
import com.gkzxhn.gkprison.utils.Log;
import com.gkzxhn.gkprison.utils.SystemUtil;
import com.gkzxhn.gkprison.utils.ToastUtil;
import com.gkzxhn.gkprison.utils.UIUtils;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.gkzxhn.gkprison.R.layout.cancel_meeting_dialog;

/**
 * Author: Huang ZN
 * Date: 2017/1/18
 * Email:943852572@qq.com
 * Description:会见列表适配器
 */
public class MeetingListAdapter extends BaseAdapter {

    private static final String TAG = MeetingListAdapter.class.getSimpleName();
    private Context mContext;
    private List<MeetingInfo> meetingInfos;

    public MeetingListAdapter(Context context, List<MeetingInfo> meetingInfo, OnCancelSuccessListener listener){
        mContext = context;
        meetingInfos = meetingInfo;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return meetingInfos.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.meeting_list_item, null);
            holder = new ViewHolder();
            holder.tv_meeting_name = (TextView) convertView.findViewById(R.id.tv_meeting_name);
            holder.tv_meeting_time = (TextView) convertView.findViewById(R.id.tv_meeting_time);
            holder.tv_meeting_prison_area = (TextView) convertView.findViewById(R.id.tv_meeting_prison_area);
            holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_meeting_name.setText(meetingInfos.get(position).getName());
        String meeting_time_start = meetingInfos.get(position).getMeeting_started();
        String meeting_time_finished = meetingInfos.get(position).getMeeting_finished();
        holder.tv_meeting_time.setText(meeting_time_start.split(" ")[1].substring(0, meeting_time_start.split(" ")[1].lastIndexOf(":")) + "-" + meeting_time_finished.split(" ")[1].substring(0, meeting_time_finished.split(" ")[1].lastIndexOf(":")));
        holder.tv_meeting_prison_area.setText(meetingInfos.get(position).getPrison_area());
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog(holder, position);
            }
        });
        return convertView;
    }

    /**
     * 取消会见对话框
     * @param holder
     * @param position
     */
    private void showCancelDialog(ViewHolder holder, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View cancel_dialog = View.inflate(mContext, cancel_meeting_dialog, null);
        TextView tv_cancel_name = (TextView) cancel_dialog.findViewById(R.id.tv_cancel_name);
        tv_cancel_name.setText(holder.tv_meeting_name.getText().toString().trim());
        final EditText et_cancel_reason = (EditText) cancel_dialog.findViewById(R.id.et_cancel_reason);
        TextView tv_cancel = (TextView) cancel_dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = (TextView) cancel_dialog.findViewById(R.id.tv_ok);
        final AlertDialog cancel_meeting_dialog = builder.create();
        cancel_meeting_dialog.setView(cancel_dialog);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = et_cancel_reason.getText().toString().trim();
                if (!TextUtils.isEmpty(reason)) {
                    sendCancelMeetingToServer(position, meetingInfos.get(position).getId(), reason);
                    cancel_meeting_dialog.dismiss();
                } else {
                    ToastUtil.showShortToast(mContext.getString(R.string.input_reason));
                }
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel_meeting_dialog.dismiss();
            }
        });
        cancel_meeting_dialog.show();
    }

    private static class ViewHolder {
        TextView tv_meeting_time;
        TextView tv_meeting_name;
        TextView tv_meeting_prison_area;
        ImageView iv_delete;
    }

    /**
     * 发送取消会见至服务器
     */
    private void sendCancelMeetingToServer(final int position, int id, String reason) {
        if (!SystemUtil.isNetWorkUnAvailable()) {
            final ProgressDialog progressDialog = UIUtils.showProgressDialog(mContext);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL_HEAD)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService apiService = retrofit.create(ApiService.class);
            String msg = "{\"accept_apply\":{\"status\":\"cancel\",\"reason\":\"" + reason + "\"}}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), msg);
            apiService.cancelMeeting(id, body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<ResponseBody>() {
                        @Override public void onError(Throwable e) {
                            Log.e(TAG, "cancel meeting failed : " + e.getMessage());
                            ToastUtil.showShortToast(mContext.getString(R.string.cancel_failed));
                        }

                        @Override public void onNext(ResponseBody responseBody) {
                            meetingInfos.remove(meetingInfos.get(position));
                            try {
                                String cancel_result = responseBody.string();
                                JSONObject jsonObject = new JSONObject(cancel_result);
                                int result_code = jsonObject.getInt("code");
                                Log.i(TAG, "cancel meeting : " + cancel_result);
                                if (result_code == 200) {
                                    //成功
                                    progressDialog.setMessage(mContext.getString(R.string.cancel_success));
                                    notifyDataSetChanged();
                                    listener.onSuccess(position);
                                } else {
                                    // 失败 code为500
                                    ToastUtil.showShortToast(mContext.getString(R.string.cancel_failed));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            ToastUtil.showShortToast(mContext.getString(R.string.net_broken));
        }
    }

    private OnCancelSuccessListener listener;

    /**
     * 取消会见名单成功回调
     */
    interface OnCancelSuccessListener{

        void onSuccess(int position);
    }
}
