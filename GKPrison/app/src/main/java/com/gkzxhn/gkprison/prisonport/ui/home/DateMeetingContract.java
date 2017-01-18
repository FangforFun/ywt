package com.gkzxhn.gkprison.prisonport.ui.home;

import com.gkzxhn.gkprison.base.BasePresenter;
import com.gkzxhn.gkprison.base.BaseView;
import com.gkzxhn.gkprison.prisonport.bean.MeetingInfo;

import java.util.List;

/**
 * Author: Huang ZN
 * Date: 2017/1/17
 * Email:943852572@qq.com
 * Description:
 */

public interface DateMeetingContract {

    interface View extends BaseView{

        /**
         * show toast进一步简单封装
         * @param msg
         */
        void showToast(String msg);

        /**
         * 开始请求数据
         */
        void requestDataStart();

        /**
         * 请求数据完成
         * @param meetingInfoList 结果列表
         */
        void requestDataComplete(List<MeetingInfo> meetingInfoList);

        /**
         * 请求数据失败
         */
        void requestDataFailed();
    }

    interface Presenter extends BasePresenter<View>{

        /**
         * 检查云信id状态然后请求列表数据
         */
        void checkStatusAndRequestData();

    }
}
