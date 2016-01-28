package com.lq.luckking.utils;

/**
 * 所有的开关控制定义，要求给默认值
 * <Description> <br> 
 *  
 * @author lei.qiang<br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate Jan 27, 2016 <br>
 * @since V1.0<br>
 * @see com.lq.luckking.utils <br>
 */
public abstract class OnOffHelper {
	/**
	 * 抢完红包是否自动关闭详情界面的开关
	 */
	public static boolean autoBackFlag = false;
	
	/**
	 * QQ 辅助服务是否开启的标识
	 */
	public static boolean qqAsHasOpened = false;
	
	/**
	 * 微信辅助服务是否开启的标识
	 */
	public static boolean wechatAsHasOpened = false;
}
