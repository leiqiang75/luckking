package com.lq.luckking.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.lq.luckking.MainActivity;
import com.lq.luckking.R;
import com.lq.luckking.constant.StageEnum;
import com.lq.luckking.utils.OnOffHelper;

/**
 * 支付宝咻红包辅助工具
 * 相对简单
 * <Description> <br> 
 *  
 * @author lei.qiang<br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate Jan 27, 2016 <br>
 * @since V1.0<br>
 * @see com.lq.luckking.accessibility <br>
 */
public class XiuEnvelopeService extends AccessibilityService {
	/**
	 * 当前阶段
	 */
	public String nowStage = StageEnum.fetched.name();
	
	/**
	 * 全局的消息发送按钮
	 */
	AccessibilityNodeInfo sendNode;
	
	/**
	 * 主要将服务注册为前台服务，常驻内存
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br> <br>
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		
		Notification notification = new Notification(R.drawable.ic_launcher,
				getResources().getString(R.string.fore_alipay_tips),
				System.currentTimeMillis());
		notification.setLatestEventInfo(this,
				getResources().getString(R.string.fore_alipay_title), 
				getResources().getString(R.string.fore_alipay_contents), pendingIntent);
		// 1. 注册为前台服务，常驻内存
		startForeground(1, notification);
	}

	@Override
	public void onInterrupt() {
		OnOffHelper.xiuAsHasOpened = false;
		Toast.makeText(this, getResources().getString(R.string.tips_alipay_xiu_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		OnOffHelper.xiuAsHasOpened = false;
		Toast.makeText(this, getResources().getString(R.string.tips_alipay_xiu_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		OnOffHelper.xiuAsHasOpened = true;
		Toast.makeText(this, getResources().getString(R.string.tips_alipay_xiu_envelope_accessibility_start), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();
		if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 
				&& "com.alipay.android.wallet.newyear.activity.MonkeyYearActivity".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
			// 如果窗口切换到咻红包界面
			nowStage = StageEnum.fetched.name();
			cycleXiu(event);
		}
		else  if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED 
				&& "android.widget.TextView".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
			cycleXiu(event);
		}
		else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 
				&& "android.app.Dialog".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
			Log.v("package", "packageName : " + event.getPackageName());
			Log.v("package", "ui : " + event.getClassName());
			nowStage = StageEnum.fetched.name();
			openEnvelope();
		}
	}

	/**
	 * 领取红包
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br> <br>
	 */
	private void openEnvelope() {
		// TODO Auto-generated method stub
		if (StageEnum.fetched.name().equalsIgnoreCase(nowStage)) {
			final AccessibilityNodeInfo parent = getRootInActiveWindow();
			if (null != parent 
					&& parent.getChildCount() > 3) {
				final int lastNodeIndex = parent.getChildCount() - 1;
				if ("android.widget.button".equalsIgnoreCase(String.valueOf(parent.getChild(lastNodeIndex).getClassName()))) {
					nowStage = StageEnum.opening.name();
					parent.getChild(lastNodeIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
					nowStage = StageEnum.opened.name();
					if (OnOffHelper.autoBackFlag) {
						performGlobalAction(GLOBAL_ACTION_BACK);
						nowStage = StageEnum.fetched.name();
					}
				}
			}
			else {
				Log.v("package", "看起来不在咻页面");
				nowStage = StageEnum.fetched.name();
			}	
		}
	}

	/**
	 * 用线程循环咻红包 
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br>
	 * @param event <br>
	 */
	private void cycleXiu(AccessibilityEvent event) {
		if (StageEnum.fetched.name().equalsIgnoreCase(nowStage)) {
			final AccessibilityNodeInfo parent = getRootInActiveWindow();
			if (null != parent 
					&& parent.getChildCount() > 10) {
				final int lastNodeIndex = parent.getChildCount() - 1;
				if ("android.widget.button".equalsIgnoreCase(String.valueOf(parent.getChild(lastNodeIndex).getClassName()))) {
					nowStage = StageEnum.fetching.name();
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							do {
								parent.getChild(lastNodeIndex).performAction(AccessibilityNodeInfo.ACTION_CLICK);
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									Log.v("package", "有异常？");
								}
							} while (StageEnum.fetching.name().equalsIgnoreCase(nowStage));							
						}
					}) .start();
				}
			}
			else {
				Log.v("package", "看起来不在咻页面");
				nowStage = StageEnum.fetched.name();
			}			
		}
	}
}
