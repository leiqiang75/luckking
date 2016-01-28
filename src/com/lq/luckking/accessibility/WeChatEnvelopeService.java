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
import com.lq.luckking.utils.WeChatEnvelopeHelper;

/**
 * 微信 红包辅助服务
 * 抢红包场景
 * 1. 微信在后台运行时，通过捕获通知栏事件(TYPE_NOTIFICATION_STATE_CHANGED),判断是否是微信红包，如果是则打开通知，会进入到聊天界面
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
public class WeChatEnvelopeService extends AccessibilityService {
	/**
	 * 当前阶段
	 */
	public String nowStage = StageEnum.fetched.name();
	
	/**
	 * 最后一次拆开过的包
	 */
	public int lastedOpenEnvelope;
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
				getResources().getString(R.string.fore_wechat_tips),
				System.currentTimeMillis());
		notification.setLatestEventInfo(this,
				getResources().getString(R.string.fore_wechat_title), 
				getResources().getString(R.string.fore_wechat_contents), pendingIntent);
		// 1. 注册为前台服务，常驻内存
		startForeground(1, notification);
	}

	@Override
	public void onInterrupt() {
		OnOffHelper.wechatAsHasOpened = false;
		Toast.makeText(this, getResources().getString(R.string.tips_wechat_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		OnOffHelper.wechatAsHasOpened = false;
		Toast.makeText(this, getResources().getString(R.string.tips_wechat_envelope_accessibility_end), Toast.LENGTH_SHORT).show();
	}
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		OnOffHelper.wechatAsHasOpened = true;
		Toast.makeText(this, getResources().getString(R.string.tips_wechat_envelope_accessibility_start), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();

		Log.v("Event", "eventType : " + event.eventTypeToString(eventType));
		Log.v("Event", "className : " + event.getClassName());
		Log.v("Event", "text : " + event.getText());
		
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			// 如果是通知栏事件，则判断是不是红包通知，是则打开通知,进入有红包的聊天界面
			String text = String.valueOf(event.getText());
			if (!text.isEmpty() && text.contains(WeChatEnvelopeHelper.ENVELOPE_TEXT_KEY)) {
				openNotification(event);
			}
		} 
		else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			// 如果是窗口状态变化事件(打开通知，或者切换到微信,或者点开红包时触发)，则在当前窗体扫描红包
			processMessage(event);
		}
		else  if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			processMessage(event);
		}
	}

	/**
	 * 打开通知栏消息
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br>
	 * @param event <br>
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
	 * 识别所处UI，分类处理 
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br>
	 * @param event <br>
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
			if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event
					.getClassName())) {
				nowStage = StageEnum.fetching.name();
				// 点开微信红包后，如果是这个UI，则表示红包未领取
				openEnvelope();
			} else if (OnOffHelper.autoBackFlag 
					&& "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())
					&& nowStage.equalsIgnoreCase(StageEnum.opened.name())) {
				// 点开微信红包后，如果是这个UI，则表示已经领取了，跳转到了红包领取详情界面
				// 执行全局返回，意图是关闭红包详情，有可能误返回
				performGlobalAction(GLOBAL_ACTION_BACK);
			}
			else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
				nowStage = StageEnum.fetching.name();
				// 处于聊天界面
				scanEnvelope();
			}
			else {
				nowStage = StageEnum.fetching.name();
				// 处于聊天界面
				scanEnvelope();
			}
		}
	}

	/**
	 * 扫描到红包，然后点开(此点开没有真的拆开红包)
	 * 为加快效率，只拆当前窗口最下面的包(最新的包)
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br> <br>
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void scanEnvelope() {
		nowStage = StageEnum.fetching.name();
		// 当前聊天窗口节点
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		
		if (rootNode == null) {
			return;
		}
		// 处理普通红包
		// 当前聊天窗口中的红包信息节点集合
		List<AccessibilityNodeInfo> genlist = rootNode
				.findAccessibilityNodeInfosByText(WeChatEnvelopeHelper.ENVELOPE_UNCLICK_TEXT_KEY);
		if (null != genlist && !genlist.isEmpty()) {
			AccessibilityNodeInfo newEnv = genlist.get(0);
			if (null != newEnv.getParent()) {
				Log.v("Temp", "parent hashCode : " + newEnv.getParent().hashCode());
			}
			if (!newEnv.isClickable() 
					&& lastedOpenEnvelope != newEnv.getParent().hashCode()) {
				newEnv.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
				lastedOpenEnvelope = newEnv.getParent().hashCode();
				Log.v("Temp", "lasted hashCode : " + lastedOpenEnvelope);
			}
		}
		nowStage = StageEnum.fetched.name();
	}
	
	/**
	 * 真正的拆开红包
	 * Description: <br> 
	 *  
	 * @author lei.qiang<br>
	 * @taskId <br> <br>
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openEnvelope() {
		nowStage = StageEnum.opening.name();
		// 当前聊天窗口节点
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		
		if (rootNode == null) {
			return;
		}
		// 处理普通红包
		// 当前聊天窗口中的未拆开普通红包信息节点集合
		AccessibilityNodeInfo buttonNode = rootNode.getChild(3);
		if ("android.widget.Button".equalsIgnoreCase(String.valueOf(buttonNode.getClassName()))) {
				// 非textView的才能点击
			buttonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		nowStage = StageEnum.opened.name();
	}
}
