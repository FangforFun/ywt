package com.gkzxhn.gkprison.prisonport.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gkzxhn.gkprison.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * created by huangzhengneng on 2016/2/2
 *
 */
public class RefreshListView extends ListView implements OnScrollListener {

	private LinearLayout refresh_header_root;
	private LinearLayout refresh_header_view;
	private ProgressBar refresh_header_progressbar;
//	private ImageView refresh_header_imageview;
	private TextView refresh_header_text;
	private TextView refresh_header_time;
	private int headerHeight;
	private View customerView;
	private RotateAnimation animationUp;
	private RotateAnimation animationDown;
	private int downY;
	private int mFirstVisibleItem = -1;

	// 默认是下来刷新状态
	private int currentOption = PULL_REFRESH;
	private OnRefreshListener onRefreshListener;
	// 下拉刷新
	private static final int PULL_REFRESH = 0;
	// 释放状态
	private static final int RELEASE_REFRESH = 1;
	// 正在刷新
	private static final int IS_REFRESH = 2;

//	private Handler handler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//			onRefreshFinished();
//		};
//	};
	private int footHeight;
	private boolean isLoading = false;
	private View viewFoot;

	public RefreshListView(Context context) {
		super(context);
		// 初始化刷新头
		initHeader();
		initFoot();
		this.setOnScrollListener(this);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeader();
		initFoot();
		this.setOnScrollListener(this);
	}

	// 接收对象的操作，调用回调方法
	public void setOnResfreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	public interface OnRefreshListener {
		// 下拉刷新的方法
		public void pullDownRefresh();

		// 上拉加载的方法
		public void pullUpLoadMore();
	}

	/**
	 * 刷新完成后的操作
	 */
	public void onRefreshFinished() {
		if (currentOption == IS_REFRESH) {
			// 刷新完成后隐藏刷新view,显示箭头
			currentOption = PULL_REFRESH;
			refresh_header_progressbar.setVisibility(View.INVISIBLE);
//			refresh_header_imageview.startAnimation(animationUp);
//			refresh_header_imageview.setVisibility(View.VISIBLE);
			refresh_header_view.setPadding(0, -headerHeight, 0, 0);
		}
		//上拉加载完成的操作
		if(isLoading){
			isLoading = false;
			viewFoot.setPadding(0, -footHeight, 0, 0);
		}
	}

	private void initFoot() {
		viewFoot = View.inflate(getContext(), R.layout.refresh_foot, null);
		// 隐藏掉
		viewFoot.measure(0, 0);
		footHeight = viewFoot.getMeasuredHeight();
		viewFoot.setPadding(0, -footHeight, 0, 0);
		this.addFooterView(viewFoot);
	}

	private void initHeader() {
		View viewHeader = View.inflate(getContext(), R.layout.refresh_header,
				null);
		refresh_header_root = (LinearLayout) viewHeader
				.findViewById(R.id.refresh_header_root);
		// 刷新头
		refresh_header_view = (LinearLayout) viewHeader
				.findViewById(R.id.refresh_header_view);
		refresh_header_progressbar = (ProgressBar) viewHeader
				.findViewById(R.id.refresh_header_progressbar);
//		refresh_header_imageview = (ImageView) viewHeader
//				.findViewById(R.id.refresh_header_imageview);
		refresh_header_text = (TextView) viewHeader
				.findViewById(R.id.refresh_header_text);
		refresh_header_time = (TextView) viewHeader
				.findViewById(R.id.refresh_header_time);

		// 测量刷新头
		refresh_header_view.measure(0, 0);
		// 获取测量后的高度
		headerHeight = refresh_header_view.getMeasuredHeight();
		// 隐藏刷新头
		refresh_header_view.setPadding(0, -headerHeight, 0, 0);
		// 添加头操作
		this.addHeaderView(viewHeader);
		// 初始化动画操作
		initAnimation();
	}

	private void initAnimation() {
		animationUp = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animationUp.setDuration(500);
		animationUp.setFillAfter(true);

		animationDown = new RotateAnimation(-180, -360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animationDown.setDuration(500);
		animationDown.setFillAfter(true);
	}

	// 暴露一个方法用于添加轮播图
	public void addCustomerView(View view) {
		customerView = view;
		// 添加
		refresh_header_root.addView(customerView);
	}

	/**
	 * 刷新数据
	 */
	public void refreshData(){
		if(onRefreshListener != null) {
			onRefreshListener.pullDownRefresh();
			currentOption = RELEASE_REFRESH;
			setCurrentOption();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 记录手指点下的y坐标
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (downY == -1) {
				downY = (int) ev.getY();
			}
			int moveY = (int) ev.getY();
			int padding = -headerHeight + moveY - downY;
			// 获取listview的Y坐标
			int[] listViewLocation = new int[2];
			// 获取listview在屏幕左上角的坐标
			this.getLocationOnScreen(listViewLocation);
			int listViewLocationY = listViewLocation[1];

			// 获取轮播图对应的Y轴坐标
//			int[] customerViewLocation = new int[2];
//			customerView.getLocationOnScreen(customerViewLocation);
//			int customerViewLocationY = customerViewLocation[1];
//
//			// 如果轮播图没有全显示出来不执行刷新
//			if (customerViewLocationY < listViewLocationY) {
//				break;
//			}
			// 如果正在刷新也不再执行刷新
			if (currentOption == IS_REFRESH) {
				break;
			}

			if (padding > -headerHeight && mFirstVisibleItem == 0) {
				// 向下拖拽，并且最上面可见item索引为0，即是下拉刷新
//				if (padding > 0 && currentOption == PULL_REFRESH) {
//					// 刷新头UI全显示出来了
//					currentOption = RELEASE_REFRESH;
//					setCurrentOption();
//				}
				if (padding < 0 && currentOption == RELEASE_REFRESH) {
					// 没有完全拖出来
					currentOption = PULL_REFRESH;
					setCurrentOption();
				}
				refresh_header_view.setPadding(0, padding, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			downY = -1;
			if (currentOption == PULL_REFRESH) {
				refresh_header_view.setPadding(0, -headerHeight, 0, 0);
			}
//			if (currentOption == RELEASE_REFRESH) {
//				refresh_header_view.setPadding(0, 0, 0, 0);
//				currentOption = IS_REFRESH;
//				setCurrentOption();
//				// 刷新业务逻辑调用的地方,回调
//				if (onRefreshListener != null) {
//					// 刷新业务逻辑处理
//					onRefreshListener.pullDownRefresh();
//				}
////				handler.sendEmptyMessageDelayed(0, 1000);
//			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	public void setCurrentOption() {
		switch (currentOption) {
		case RELEASE_REFRESH:
			refresh_header_text.setText("释放刷新");
//			refresh_header_imageview.startAnimation(animationUp);
			break;
		case PULL_REFRESH:
			refresh_header_text.setText("下拉刷新");
//			refresh_header_imageview.startAnimation(animationDown);
			break;
		case IS_REFRESH:
			refresh_header_text.setText("正在刷新");
			refresh_header_progressbar.setVisibility(View.VISIBLE);
//			refresh_header_imageview.clearAnimation();
//			refresh_header_imageview.setVisibility(View.GONE);
			refresh_header_time.setText(getDataTime());
			break;
		}
	}

	private String getDataTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 监听滑动状态
		// OnScrollListener.SCROLL_STATE_FLING 飞速滚动
		// OnScrollListener.SCROLL_STATE_TOUCH_SCROLL 一直触摸着在滚动
		// OnScrollListener.SCROLL_STATE_IDLE 滚动状态发生改变
		if (scrollState == SCROLL_STATE_FLING
				|| scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			if (getLastVisiblePosition() == getAdapter().getCount() - 1 && !isLoading) {
				// 看见最后一个条目的时候
				//加载操作
				isLoading = true;
				//显示正在加载的脚
				viewFoot.setPadding(0, 0, 0, 0);
				//加载的业务逻辑,回调
				if(onRefreshListener != null){
					onRefreshListener.pullUpLoadMore();
				}
//				handler.sendEmptyMessageDelayed(0, 1000);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = firstVisibleItem;
	}
}
