package com.gkzxhn.gkprison.application;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * ActivityStackManager
 * 
 * @author ChenJian
 */
public class AppStackManager {

	private static AppStackManager mActivityStackManager;
	private static Stack<Activity> mActivityStack;

	private AppStackManager() {
		mActivityStack = new Stack<Activity>();
	}

	public static AppStackManager Instance() {
		synchronized (AppStackManager.class) {
			if (mActivityStackManager == null) {
				mActivityStackManager = new AppStackManager();
			}

			if (null == mActivityStack) {
				mActivityStack = new Stack<Activity>();
			}
		}

		return mActivityStackManager;
	}

	public static void relaseActivityStack() {
		if (mActivityStack != null) {
			mActivityStack.clear();
		}

		if (mActivityStackManager != null) {
			mActivityStackManager = null;
		}
	}

	/**
	 * 压入堆栈顶部
	 * 
	 * @param activity
	 */
	public void pushActivity(Activity activity) {
		synchronized (AppStackManager.class) {
			if (mActivityStack == null)
				mActivityStack = new Stack<Activity>();

			if (activity == null)
				return;

			mActivityStack.push(activity);
		}
	}

	/*
	 * 移除堆栈顶部的Activity
	 */
	public void popActivity() {
		synchronized (AppStackManager.class) {
			if (mActivityStack == null || mActivityStack.empty())
				return;

			Activity activity = mActivityStack.peek();
			if (activity != null) {
				activity = mActivityStack.pop();
				activity.finish();
				activity = null;
			}
		}
	}

	/**
	 * 移除堆栈中的Activity
	 * 
	 * @param activity
	 * @param finish
	 *            finish Activity
	 */
	public void popActivity(Activity activity, boolean finish) {
		if (activity == null) {
			return;
		}

		synchronized (AppStackManager.class) {
			if (finish) {
				activity.finish();
			}

			if (mActivityStack != null && !mActivityStack.empty()
					&& mActivityStack.contains(activity)) {
				mActivityStack.remove(activity);
			}

			activity = null;
		}
	}

	/**
	 * 移除堆栈中的Activity，并将Activity Finish
	 * 
	 * @param activity
	 */
	public void popActivity(Object activity) {
		if (null == activity || !(activity instanceof Activity)) {
			return;
		}

		popActivity((Activity) activity, true);
	}

	public void popActivitys(List<Activity> activitys) {
		if (null == activitys || activitys.isEmpty())
			return;

		for (Activity ITBaseActivity : activitys) {
			if (null == ITBaseActivity)
				continue;

			popActivity(ITBaseActivity, true);
		}
	}

	/**
	 * 移除堆栈中的Activity
	 * 
	 * @param activity
	 */
	public void popActivity(Activity activity) {
		if (activity == null) {
			return;
		}
		synchronized (AppStackManager.class) {
			activity.finish();
			if (mActivityStack != null && !mActivityStack.empty()
					&& mActivityStack.contains(activity)) {
				mActivityStack.remove(activity);
			}

			activity = null;
		}
	}

	public void popActivity(int index) {
		synchronized (AppStackManager.class) {
			if (index < 0)
				return;

			if (mActivityStack != null && !mActivityStack.empty()
					&& index < mActivityStack.size()) {
				Activity activity = mActivityStack.get(index);
				if (mActivityStack.contains(activity))
					popActivity(activity);
			}
		}
	}

	/**
	 * get activity from Stack
	 * 
	 * @param index
	 * @return
	 */
	public Activity getActivity(int index) {
		synchronized (AppStackManager.class) {
			Activity activity = null;
			if (index < 0)
				return activity;

			if (mActivityStack != null && !mActivityStack.empty()
					&& index < mActivityStack.size()) {
				activity = mActivityStack.get(index);
			}

			return activity;
		}
	}

	@SuppressWarnings("rawtypes")
	public Activity getActivity(Class cls) {
		synchronized (AppStackManager.class) {
			if (cls == null)
				return null;

			Activity result = null;
			try {
				if (mActivityStack != null && !mActivityStack.empty()) {
					for (Activity a : mActivityStack) {
						if (a == null)
							continue;
						if (a.getClass().equals(cls)) {
							result = a;
							break;
						}
					}
				}
			} catch (Exception e) {
			}

			return result;
		}
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<Activity> getActivityList(Class cls) {
		if (cls == null)
			return null;
		synchronized (AppStackManager.class) {
			ArrayList<Activity> list = new ArrayList<Activity>();
			Activity result = null;
			if (mActivityStack != null && !mActivityStack.empty()) {
				for (Activity a : mActivityStack) {
					if (a == null)
						continue;
					if (a.getClass().equals(cls)) {
						list.add(a);
					}
				}
			}

			return list;
		}
	}

	/**
	 * current Activity
	 * 
	 * @return
	 */
	public Activity currentActivity() {
		synchronized (AppStackManager.class) {
			if (mActivityStack == null || mActivityStack.empty())
				return null;

			Activity activity = mActivityStack.peek();

			return activity != null ? activity : null;
		}
	}

	/**
	 * pre Activity
	 * 
	 * @return
	 */
	public Activity preActivity() {
		synchronized (AppStackManager.class) {
			int index = currentActivityIndex();
			return getActivity(index - 1);
		}
	}

	public int currentActivityIndex() {
		synchronized (AppStackManager.class) {
			Activity activity = currentActivity();
			if (activity == null)
				return 0;

			int index = 0;
			if (mActivityStack != null && !mActivityStack.empty()
					&& mActivityStack.contains(activity)) {
				index = mActivityStack.indexOf(activity);
			}

			return index;
		}
	}

	/**
	 * 移除Activity至某Activity
	 * 
	 * @param cls
	 *            first-activity
	 */
	@SuppressWarnings("rawtypes")
	public void popAllActivityExceptOne(Class cls) {
		synchronized (AppStackManager.class) {
			while (true) {
				Activity activity = currentActivity();
				if (activity == null)
					break;

				if (activity.getClass().equals(cls))
					break;

				popActivity(activity);
			}
		}
	}

	public void popAllActivity() {
		synchronized (AppStackManager.class) {
			if (mActivityStack == null || mActivityStack.isEmpty()) {
				return;
			}

			for (int i = mActivityStack.size() - 1; i >= 0; i--) {
				Activity a = mActivityStack.get(i);
				if (a != null) {
					a.finish();
				}
			}

			mActivityStack.clear();
		}
	}

	/**
	 * 获取的acticvity
	 * 
	 * @return
	 */
	public ArrayList<Activity> getAllActivity() {
		synchronized (AppStackManager.class) {
			ArrayList<Activity> activityList = new ArrayList<Activity>();
			for (Activity activity : mActivityStack) {
				if (activity != null && activityList != null) {
					activityList.add(activity);
				}
			}
			return activityList;
		}
	}

	/**
	 * exit application
	 */
	public void exitApp() {
		// MtcLibManager.closeSusModel();
	}

	/**
	 * 判断某一个activity是否存在activity栈中
	 * 
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean existActivity(Class cls) {
		synchronized (AppStackManager.class) {
			boolean b = false;

			for (int i = 0; i < mActivityStack.size(); i++) {
				Activity currentActivity = mActivityStack.get(i);
				if (currentActivity != null) {
					if (currentActivity.getClass().equals(cls)) {
						b = true;
						break;
					}
				}
			}
			return b;
		}
	}

	/**
	 * 查找堆栈中存在有几个相同的页面类
	 * 
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public int existActivityCount(Class cls) {
		synchronized (AppStackManager.class) {
			int count = 0;
			for (int i = 0; i < mActivityStack.size(); i++) {
				Activity currentActivity = mActivityStack.get(i);
				if (currentActivity != null) {
					if (currentActivity.getClass().equals(cls)) {
						count++;
					}
				}
			}

			return count;
		}
	}

	/**
	 * 清除缓存微博数据以外的所以URL
	 * 
	 * @Description
	 */
	public void clearArticleURL() {
	}

	/**
	 * 释放内存
	 */
	public void relaseRAM() {
		// AtWeiboInfo.clean();
		// KTruetouchSQLiteDatabase.release();
	}
}
