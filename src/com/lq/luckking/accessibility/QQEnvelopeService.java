package com.lq.luckking.accessibility;

import java.util.List;

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

import com.lq.luckking.MainActivity;
import com.lq.luckking.R;
import com.lq.luckking.constant.StageEnum;
import com.lq.luckking.utils.OnOffHelper;
import com.lq.luckking.utils.QQEnvelopeHelper;

/**
 * QQ 红包辅助服务
 * 抢红包场景
 * 1. QQ在后台运行时，通过捕获通知栏事件(TYPE_NOTIFICATION_STATE_CHANGED),判断是否是QQ红包，如果是则打开通知，会进入到聊天界面
 * 2. 通知栏打开的聊天界面，会捕获窗口状态变更事件(TYPE_WINDOW_STATE_CHANGED),扫描消息识别红包，然后打开红包 
 * 3. 本来就处于聊天界面(非通知栏打开), 会捕获内容变更事件(TYPE_WINDOW_CONTENT_CHANGED)，扫描消息识别红包，然后打开红包
 * <Description> <br> 
 *  
 * @author lei.qiang<br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate Jan 27, 2016 <br>
 * @since V1.0<br>
 * @see com.lq.luckking.accessibility <br>
 */
public class QQEnvelopeService extends AccessibilityService {
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
				getResources().getString(R.string.fore_qq_tips),
				System.currentTimeMillis());
		notification.setLatestEventInfo(this,
				getResources().getString(R.string.fore_qq_title), 
				getResources().getString(R.string.fore_qq_contents), pendingIntent);
		// 1. 注册为前台服务，常驻内存
		startForeground(1, notification);
	}

	@Override
	public void onInterrupt() {
		Toast.makeText(this, getResources().getString(R.string.envelope_accessibility_end), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Toast.makeText(this, getResources().getString(R.string.envelope_accessibility_start), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();

		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			// 如果是通知栏事件，则判断是不是红包通知，是则打开通知,进入有红包的聊天界面
			String text = String.valueOf(event.getText());
			if (!text.isEmpty() && text.contains(QQEnvelopeHelper.ENVELOPE_TEXT_KEY)) {
				openNotification(event);
			}
		} 
		else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			// 如果是窗口状态变化事件(打开通知，或者切换到QQ,点开红包时触发)，则在当前窗体扫描红包
			processMessage(event);
		}
		else  if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED 
				&& "android.widget.AbsListView".equalsIgnoreCase(String.valueOf(event.getClassName()))) {
			processMessage(event);
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
	private void processMessage(AccessibilityEvent event) {
		if (nowStage.equalsIgnoreCase(StageEnum.opening.name())) {
			// 有正在拆的包，则将当前包加入队列
			return;
		}
		else {
			/*
			 *  非拆红包阶段，进行下列处理
			 *  1. 正在拆红包，则等待拆红包，不进行后续处理，优化性能
			 *  2. 避免误返回
			 */
			if ("com.tencent.mobileqq.activity.SplashActivity".equals(event
					.getClassName())) {
				nowStage = StageEnum.fetching.name();
				// 聊天窗体，点开红包
				scanAndOpenEnvelope();
			} else if (OnOffHelper.autoBackFlag 
					&& "cooperation.qwallet.plugin.QWalletPluginProxyActivity".equals(event.getClassName())
					&& nowStage.equalsIgnoreCase(StageEnum.opened.name())) {
				// 允许自动返回且处于拆完红包阶段
				// 执行全局返回，意图是关闭红包详情，有可能误返回
				performGlobalAction(GLOBAL_ACTION_BACK);
			}
			else if ("android.widget.AbsListView".equals(event.getClassName())) {
				nowStage = StageEnum.fetching.name();
				// 没有正在拆的包，则拆开当前包
				scanAndOpenEnvelope();
			}			
		}
	}

	/**
	 * 拆包
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void scanAndOpenEnvelope() {
		nowStage = StageEnum.opening.name();
		// 当前聊天窗口节点
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		
		// 查找聊天界面中的发送按钮，应该是全局唯一
		if (null == sendNode) {
			Log.v("Temp", "sendButton");
			List<AccessibilityNodeInfo> sendBtnlist = rootNode
					.findAccessibilityNodeInfosByText(QQEnvelopeHelper.GLOBAL_SENT_TEXT_KEY);
			if (null != sendBtnlist && !sendBtnlist.isEmpty()) {
				for (AccessibilityNodeInfo node : sendBtnlist) {
					if (node.getClassName().toString().equals(QQEnvelopeHelper.GLOBAL_SEND_BUTTON_KEY) 
							&& node.getText().toString().equals(QQEnvelopeHelper.GLOBAL_SENT_TEXT_KEY)) {
						sendNode = node;
						break;
					}
				}
			}
		}
		
		if (rootNode == null) {
			return;
		}
		
		// 当前聊天窗口中的未拆开普通红包信息节点集合
		List<AccessibilityNodeInfo> genlist = rootNode
				.findAccessibilityNodeInfosByText(QQEnvelopeHelper.ENVELOPE_UNCLICK_TEXT_KEY);
		for (AccessibilityNodeInfo node : genlist) {
			if (!node.isClickable()) {
				Log.i("Temp", "普通红包 node name :" + node.getText());
				// 非textView的才能点击
				node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		}
		
		// 当前聊天窗口中的未拆开普通红包信息节点集合
		List<AccessibilityNodeInfo> pdlist = rootNode
				.findAccessibilityNodeInfosByText(QQEnvelopeHelper.PASSWORD_ENVELOPE_UNCLICK_TEXT_KEY);
		for (AccessibilityNodeInfo node : pdlist) {
			if (!node.isClickable() && QQEnvelopeHelper.PASSWORD_ENVELOPE_UNCLICK_TEXT_KEY.equalsIgnoreCase(String.valueOf(node.getText()))) {
				Log.i("Temp", "口令红包 node name :" + node.getText());
				node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
				List<AccessibilityNodeInfo> inputlist = rootNode
						.findAccessibilityNodeInfosByText(QQEnvelopeHelper.INPUT_PASSWORD_BUTTON_KEY);
				if (null != inputlist && !inputlist.isEmpty()) {
					// 点击输入口令
					inputlist.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
					// 点击全局发送按钮
					sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				}
			}
		}
		
		nowStage = StageEnum.opened.name();
	}
}
