package com.lq.luckking;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * <p>
 * Created by Administrator
 * </p>
 * <p/>
 * 抢红包外挂服务
 */
public class EnvelopeService extends AccessibilityService {

	
	private static final String TAG_EVENT = "Event";
	private static final String TAG_OPENING = "Opening";
	private static final String TAG_FLOW = "Flow";
	private static final String TAG_TEMP = "Temp";

	/**
	 * 红包消息的关键字
	 */
	private static final String ENVELOPE_TEXT_KEY = "[QQ红包]";
	/**
	 * 普通红包未拆开时的关键字
	 */
	private static final String ENVELOPE_CLICK_TEXT_KEY = "点击拆开";
	/**
	 * 口令红包未拆开时的关键字
	 */
	private static final String PASSWORD_ENVELOPE_TEXT_KEY = "口令红包";
	/**
	 * 点击口令红包后的关键字输入按钮名称
	 */
	private static final String INPUT_PASSWORD_TEXT_KEY = "点击输入口令";
	/**
	 * 聊天窗口唯一的发送按钮
	 */
	private static final String GLOBAL_SEND_BUTTON_KEY = "android.widget.Button";
	/**
	 * 全局发送按钮的关键字
	 */
	private static final String GLOBAL_SENT_TEXT_KEY = "发送";
	/**
	 * 口令红包被抢完后的提示信息view关键字
	 */
	private static final String GLOBAL_TIPS_PASSWORD_OVER = "android.widget.TextView";

	public static boolean autoBackFlag = false;
	/**
	 * 当前阶段
	 */
	public String nowStage = StageEnum.fetched.name();
	/**
	 * 待拆的红包队列
	 */
	public BlockingQueue<AccessibilityEvent> noOpenList = new LinkedBlockingQueue<AccessibilityEvent>(30);
	
	/**
	 * 全局的消息发送按钮
	 */
	AccessibilityNodeInfo sendNode;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		
		Notification notification = new Notification(R.drawable.ic_launcher,
				getResources().getString(R.string.fore_tips),
				System.currentTimeMillis());
		notification.setLatestEventInfo(this,
				getResources().getString(R.string.fore_title), 
				getResources().getString(R.string.fore_contents), pendingIntent);
		// 1. 注册为前台服务，常驻内存
		startForeground(1, notification);
	}

	@Override
	public void onInterrupt() {
		Toast.makeText(this, "停止抢红包", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Toast.makeText(this, "开始抢红包", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();

		Log.d(TAG_EVENT, AccessibilityEvent.eventTypeToString(eventType) + "\n" 
				+ event.toString());

		// 开启循环拆包线程
		//loopOpenning();
		
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			// 如果是通知栏事件，则判断是不是红包通知
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence t : texts) {
					String text = String.valueOf(t);
					if (text.contains(ENVELOPE_TEXT_KEY)) {
						Log.d(TAG_FLOW, "open the notification");
						openNotification(event);
						break;
					}
				}
			}
		} 
		else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			Log.d(TAG_FLOW, "open the envelope");
			processMoneyEnvelope(event);
		}
		else  if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			processMoneyEnvelope(event);
		}
	}

	/**
	 * 打开通知栏消息
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openNotification(AccessibilityEvent event) {
		if (event.getParcelableData() == null
				|| !(event.getParcelableData() instanceof Notification)) {
			return;
		}
		// 将通知栏消息打开
		Notification notification = (Notification) event.getParcelableData();
		PendingIntent pendingIntent = notification.contentIntent;
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 处理红包
	 * @param event
	 */
	private void processMoneyEnvelope(AccessibilityEvent event) {
		if ("com.tencent.mobileqq.activity.SplashActivity".equals(event
				.getClassName())) {
			// 拆红包
			if (nowStage.equalsIgnoreCase(StageEnum.opening.name())) {
				// 有正在拆的包，则将当前包加入队列
				noOpenList.add(event);
			}
			else {
				// 没有正在拆的包，则拆开当前包
				openMoneyEnvelope();
			}
		} else if (autoBackFlag && "cooperation.qwallet.plugin.QWalletPluginProxyActivity"
				.equals(event.getClassName())) {
			// 拆完红包后看详细的纪录界面
			// nonething
			boolean flag3 = performGlobalAction(GLOBAL_ACTION_BACK);
			Log.i(TAG_OPENING, "back : " + String.valueOf(flag3));
		}
		else if ("android.widget.AbsListView".equals(event.getClassName())) {
			if (nowStage.equalsIgnoreCase(StageEnum.opening.name())) {
				// 有正在拆的包，则将当前包加入队列
				noOpenList.add(event);
			}
			else {
				// 没有正在拆的包，则拆开当前包
				openMoneyEnvelope();
			}
		}
	}

	/**
	 * 拆包
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openMoneyEnvelope() {
		nowStage = StageEnum.opening.name();
		// 当前聊天窗口节点
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		
		if (null == sendNode) {
			List<AccessibilityNodeInfo> sendBtnlist = nodeInfo
					.findAccessibilityNodeInfosByText(GLOBAL_SENT_TEXT_KEY);
			if (null != sendBtnlist && !sendBtnlist.isEmpty()) {
				for (AccessibilityNodeInfo node : sendBtnlist) {
					if (node.getClassName().toString().equals(GLOBAL_SEND_BUTTON_KEY) 
							&& node.getText().toString().equals(GLOBAL_SENT_TEXT_KEY)) {
						sendNode = sendBtnlist.get(0);
						break;
					}
				}
			}
		}
		if (nodeInfo == null) {
			Log.w(TAG_OPENING, "rootWindow为空");
			return;
		}
		
		// 当前聊天窗口中的未拆开普通红包信息节点集合
		List<AccessibilityNodeInfo> genlist = nodeInfo
				.findAccessibilityNodeInfosByText(ENVELOPE_CLICK_TEXT_KEY);
		Log.i(TAG_OPENING, "General Size:" + genlist.size());
		for (AccessibilityNodeInfo n : genlist) {
			boolean flag2 = n.getParent().performAction(
					AccessibilityNodeInfo.ACTION_CLICK);
			Log.i(TAG_OPENING, "click : " + String.valueOf(flag2));
		}
		
		// 当前聊天窗口中的未拆开普通红包信息节点集合
		List<AccessibilityNodeInfo> pdlist = nodeInfo
				.findAccessibilityNodeInfosByText(PASSWORD_ENVELOPE_TEXT_KEY);
		Log.i(TAG_OPENING, "Password Size:" + pdlist.size());
		for (AccessibilityNodeInfo n : pdlist) {
			if (PASSWORD_ENVELOPE_TEXT_KEY.equalsIgnoreCase(String.valueOf(n.getText()))) {
				n.getParent().performAction(
						AccessibilityNodeInfo.ACTION_CLICK);
				List<AccessibilityNodeInfo> inputlist = nodeInfo
						.findAccessibilityNodeInfosByText(INPUT_PASSWORD_TEXT_KEY);
				if (null != inputlist && !inputlist.isEmpty()) {
					inputlist.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
					sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
			}
		}
		nowStage = StageEnum.fetched.name();
	}
}
